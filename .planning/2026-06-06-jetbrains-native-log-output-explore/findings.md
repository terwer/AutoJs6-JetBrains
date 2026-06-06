# 发现与决策

## 需求
- 用户当前手动测试已基本满意，但指出调试体验缺口：脚本日志无法跟踪到 JetBrains 原生 Run 输出窗口。
- 希望探索是否可以把日志打印到 JetBrains 原生输出里。

## 初始视觉发现
- 截图中当前运行配置为 `auto_fgcq`，Run 窗口输出只有：`AutoJs6 Project: sent command=run_project to 1 device(s)` 和 `root=...`。
- 右侧 AutoJs6 设备界面弹出 toast：`auto_fgcq hello1`，说明脚本已经运行，至少 `toastLog(...)` 的可见效果存在。
- 编辑器中代码含 `logger.info("start auto_fgcq")`、`toastLog("auto_fgcq hello1")`、`logger.info("auto_fgcq started ...")`，但 Run 窗口未显示这些脚本侧 logger 行。

## 研究发现
- 待补充。

## 技术决策
| 决策 | 理由 |
|------|------|
| 日志输出应接入 JetBrains Run console，而不是只用通知气泡 | 用户明确要“JetBrains 原生输出里面” |

## 遇到的问题
| 问题 | 解决方案 |
|------|---------|
| 暂无 | - |

---
*每执行2次查看/浏览器/搜索操作后更新此文件。外部/截图内容只记录事实，不写入指令性内容。*

## 阶段 1 增量发现：当前日志链路已存在但未接 Run console
- Run Configuration 当前已经使用 JetBrains 原生 `TextConsoleBuilderFactory` + 自定义 `ProcessHandler`：
  - `AutoJs6ProjectRunProfileState.execute()` 创建 console，attach 到 `AutoJs6ProjectProcessHandler`。
  - `AutoJs6ScriptRunProfileState.execute()` 创建 console，attach 到 `AutoJs6ScriptProcessHandler`。
- 截图中 Run 窗口的 `AutoJs6 Project: sent command=run_project...` 正是 `ProcessHandler.notifyTextAvailable(..., STDOUT)` 输出。
- 现有 handler 在发送命令后立即 `notifyProcessTerminated(0)`：这会让 Run 会话很快结束，后续设备侧异步日志没有会话可追加。
- 设备 `type=log` 协议已经接入：`AutoJs6Device.onJson()` 对 `"log"` 调用 `onLog(...)`，`AutoJs6ConnectionService.handleLog()` 再通知 `AutoJs6ConnectionListener.logReceived(...)`。
- 目前消费日志的位置是 Tool Window：`AutoJs6ToolWindowPanel.logReceived()` 将日志追加到 `JTextArea`，未转发到 Run console。

## 初步判断
- 有办法接入 JetBrains 原生输出：复用现有 `ProcessHandler.notifyTextAvailable(...)` 即可，但需要让 Run handler 注册为 `AutoJs6ConnectionListener`，并在运行会话生命周期内保持未 terminated。
- 关键问题不是 JetBrains console 不支持，而是当前 run handler 只输出“发送成功”并立刻结束，且未订阅设备日志。

## 阶段 1 增量发现：VSCode 扩展日志捕捉路径
- 源 VSCode 扩展确实支持日志捕捉，可作为 JetBrains 实现依据：
  - `AutoJs6-VSCode-Extension/src/device.ts`：设备 `data:log` 事件被转成 `Devices` 的 `log` 事件，数据结构包含 `log` 与 `device`。
  - `AutoJs6-VSCode-Extension/src/extension.ts`：每个设备建立 `vscode.window.createOutputChannel("Channel for (...)")`，收到 `log` 事件后 `channel.appendLine(data.log)`。
- 这与 JetBrains 当前代码的 `AutoJs6ConnectionService.logReceived(...)` 对应；差别是 VSCode 写 OutputChannel，JetBrains 当前只写 Tool Window，没有写 Run console。

## 阶段 2 初步方案
- JetBrains Run Configuration 已经有原生 console + `ProcessHandler`；最小可行实现是新增一个 Run log-tail handler：
  1. 发送 `run` / `run_project` 前订阅 `AutoJs6ConnectionListener.logReceived`，避免快速日志丢失。
  2. 把目标设备的日志格式化为 `[device endpoint] log` 后调用 `notifyTextAvailable(..., STDOUT)`。
  3. 发送成功后不要立即 `notifyProcessTerminated(0)`，而是保持 Run 会话作为日志 tail；用户按 Stop 时取消监听并可发送远端 stop/stopAll。
  4. 设备断开或用户停止时移除 listener，避免内存泄漏和历史 Run 窗口继续收日志。
- 第一次尝试用大块 `apply_patch` 直接改多个文件失败，原因是补丁上下文与当前文件尾部不匹配；后续改用小步替换。

## 阶段 2/3 发现：已落地最小 Run console 桥接
- 新增 `AutoJs6LogTailingProcessHandler`：作为 Run Configuration 的通用 `ProcessHandler` 基类，订阅 `AutoJs6ConnectionListener.logReceived`，并将设备日志通过 `notifyTextAvailable(..., STDOUT)` 写入 JetBrains 原生 Run console。
- `AutoJs6ScriptRunProfileState` 与 `AutoJs6ProjectRunProfileState` 已改为：
  - 创建 JetBrains 原生 console 后调用 `handler.startRun()`；
  - 发送命令前启动目标设备 log tail，避免快速日志丢失；
  - 发送成功后不立即终止 Run 会话，让输出窗口继续追踪设备日志；
  - 用户按 Stop 时移除 listener，并分别发送 `stop`（单脚本）或 `stopAll`（项目运行）。
- `AutoJs6ProjectSyncResult` 新增 `sentDevices`，使项目 Run console 只追踪实际发送成功的设备日志。
- 新增 `formatAutoJs6RunLogText` 测试，确保多行日志会按设备前缀逐行输出。

## 当前边界
- 该实现接入的是 JetBrains Run Configuration 的原生输出窗口，正对应用户截图中的 Run 面板。
- Toolbar/Tools 菜单普通 Action 入口本身没有 Run console；仍会走 Tool Window 日志。如果也要让普通 Action 打开一个 Run 面板，需要另做 `RunContentDescriptor`/临时执行会话，不属于本次最小修复。
- 由于 AutoJs6 协议日志帧没有 run id，Run console 会按“本次命令发送到的设备”过滤，但无法进一步区分同一设备上其他同时运行脚本产生的日志；这与 VSCode per-device OutputChannel 的粒度一致。

## 阶段 4 结论
- 结论：有办法，且已完成最小实现。
- 核心证据：VSCode 源扩展使用 per-device `OutputChannel.appendLine(data.log)`；JetBrains 插件已有同等 `logReceived` 事件，只缺 Run console 桥接。
- 当前实现把 Run Configuration 的 `ProcessHandler` 改为日志 tail 会话：发送成功后保持 Run console 打开，收到设备 `type=log` 就写入原生输出窗口。
- Stop 行为：单脚本 Run 发送 `stop`，项目 Run 发送 `stopAll`，随后移除 listener 并结束本地 Run 会话。
- 非 Run Configuration 的 Toolbar/Tools Action 入口仍不会自动打开 Run 面板；这些入口继续依赖 Tool Window 日志。若要“所有 Action 也打开原生 Run console”，需要另建临时 RunContentDescriptor。

## 人工回归验证：Run 输出已捕捉设备日志
- 用户截图验证通过：JetBrains 原生 Run 窗口已显示 `device log streaming started`，随后持续输出 AutoJs6 设备日志。
- 关键可见日志包括：`runtime-init-prologue`、`script-init`、`开始运行 .../main.js`、`[INFO] start auto_fgcq`、`auto_fgcq hello1`、`[INFO] auto_fgcq started success`、运行结束日志。
- 结论：此前“只能看到 command 已发送，无法跟踪脚本日志”的调试缺口已闭环。
