# 发现与决策

## 需求
- 用户希望把 `AutoJs6-VSCode-Extension` 做成一个 IDEA/JetBrains 版本。
- 本轮明确约束：不要开始写代码，只在 `D:\Users\Administrator\Documents\myproject\AutoJs6-JetBrains` 使用文件规划系统做探索。

## 研究发现
- 待探索源项目结构、`package.json`、README、`src/`、assets、tools。

## 技术决策
| 决策 | 理由 |
|------|------|
| 暂不创建 JetBrains 插件业务源码 | 用户明确要求不要开始写代码 |
| 先从 VSCode `package.json` 和 `src` 入口开始 | VSCode 扩展清单通常定义命令、贡献点、激活事件和入口 |

## 遇到的问题
| 问题 | 解决方案 |
|------|---------|
| `New-Item -LiteralPath` 参数不可用 | 使用 `New-Item -Path` |

## 资源
- 源项目：`D:\Users\Administrator\Documents\myproject\AutoJs6-VSCode-Extension`
- 目标计划目录：`D:\Users\Administrator\Documents\myproject\AutoJs6-JetBrains\.planning\2026-06-05-autojs6-jetbrains-explore`

## 视觉/浏览器发现
- 暂无。

---
*每执行2次查看/浏览器/搜索操作后更新此文件*
*防止视觉信息丢失*

## 阶段 1 增量发现：源项目初扫
- `src/` 主要文件：`extension.ts` 约 56KB，是核心入口；另有 `device.ts`、`adb.ts`、`project.ts`、`diff.ts`、`util.ts`。
- `package.json`：扩展名 `autojs6-vscode-ext`，版本 `1.0.13`，入口 `./out/extension.js`，激活事件 `onStartupFinished`。
- 已注册命令约 18 个：连接/断开、运行/停止/重跑脚本、保存文件/项目到设备、新建文件/项目、查看在线文档等。
- 核心通信迹象：`device.ts` 使用 TCP `net.createServer`/`net.Socket`；`extension.ts` 使用 `http.createServer`；`adb.ts` 封装 ADB 命令；存在 AutoJs6 客户端/服务端两类连接模式与 ADB 转发。
- 迁移初步判断：JetBrains 版本不是简单 UI 迁移，核心是“设备连接 + 协议通信 + 编辑器/项目文件操作 + 命令入口”的平台适配。

## 阶段 1 增量发现：贡献点与核心代码
- `package.json` 只有 `breakpoints`、`commands`、`menus`、`keybindings` 贡献点；没有语言语法、grammar、configuration 贡献点。
- 菜单入口集中在 VSCode `editor/title` 与 `explorer/context`，JetBrains 可对应为编辑器工具栏/主菜单 Action、Project View 右键 Action、快捷键 Keymap。
- 快捷键重点：F6 运行、F8 命令层级、Ctrl+F6 停止、Ctrl+Shift+F6 停止全部、Ctrl+Alt+F6 连接等。
- `Extension` 构造流程：初始化当前编辑器、ADB、设备客户端、注册命令，并将 deactivate 绑定到断开连接。
- `Devices` 构造时直接监听 `LISTENING_PORT`；`Device` 使用 8 字节头：前 4 字节 big-endian payload 长度，后 4 字节类型，payload 为 JSON/bytes。
- 连接握手：设备发送 `hello`，插件校验 AutoJs6 app version，回发 hello，然后 attach 设备。
- 项目模板/项目配置相关代码在 `project.ts`，新建项目使用 `assets/template` 并替换 `project.json` 中的项目名/包名占位符。

## 阶段 1 增量发现：项目资源、同步和官方迁移参考
- `assets/` 包含文档、图标、图片和 `assets/template` 项目模板；模板内有 `main.js`、`project.json`、`package.json`、`tsconfig.json`、`.internal`、`modules`。
- `tools/` 包含 Windows ADB：`adb.exe`、`AdbWinApi.dll`、`AdbWinUsbApi.dll`。JetBrains 版本可复用这些二进制资源，但需要注意跨平台分发策略。
- 项目同步逻辑：`ProjectObserver.diff()` 遍历项目文件，按 mtime 计算 modified/deleted，打 zip，算 md5；`Devices.sendProjectCommand()` 先 `sendBytes(zip)`，再发送 `bytes_command` JSON，携带 md5、deletedFiles、override、command。
- 关键常量：监听端口 `6347`，AutoJs6 服务端默认端口 `7347`，ADB 服务端端口 `20347`，HTTP 命令入口端口 `10347`，协议头 8 字节，JSON 类型 `1`、bytes 类型 `2`。
- `package.json` 要求 AutoJs6 客户端版本至少 `6.7.0` / code `3591`。
- 官方 JetBrains 参考：当前 Gradle 构建应使用 IntelliJ Platform Gradle Plugin 2.x；Action/Tool Window/状态持久化分别有官方 SDK 文档。用于规划，不在本轮落地代码。

## JetBrains 功能映射草案
| VSCode 能力 | 源证据 | JetBrains 对应建议 | 风险 |
|---|---|---|---|
| 命令注册与菜单/快捷键 | `package.json` commands/menus/keybindings，`Extension.commands` | `AnAction` + `plugin.xml` actions + Keymap | 低 |
| 编辑器运行当前 JS | `run()`/`runFileOn()` 读取 active editor 文本 | `FileEditorManager`/`Editor`/`Document` + Action | 中 |
| Project View 右键运行/保存 | `explorer/context` | Project View Popup Action，基于 `VirtualFile` | 中 |
| 设备连接选择 UI | VSCode QuickPick/InputBox | `Messages`/`DialogWrapper`/Tool Window 列表 | 中 |
| TCP 设备协议 | `device.ts` | Kotlin/Java NIO socket 服务 + 协议类 | 中 |
| ADB 连接/端口转发 | `adb.ts`、`tools/adb.exe` | `ProcessBuilder` 调用 ADB + 设置项配置路径 | 中 |
| 保存/运行项目差量 zip | `ProjectObserver`、`sendProjectCommand()` | Kotlin zip + md5 + 文件 watcher/遍历 | 中高 |
| 新建 AutoJs6 项目 | `assets/template` + placeholder 替换 | New Project Action 或普通 Action 创建模板 | 低中 |
| HTTP 远程命令入口 | `AJHttpServer` 10347 | 内置 HTTP server 或延后实现 | 中高 |
| 语法/补全 | package 无 languages/grammars | MVP 可不做；后续可接 JS/TS 平台能力 | 低 |

## MVP 建议
1. 第一版优先：连接/断开设备、运行当前文件、停止/停止全部、保存当前文件、新建项目、查看文档。
2. 第二版：指定设备运行/保存、项目保存/运行差量同步、设备列表 Tool Window。
3. 第三版：HTTP 远程命令入口、命令层级 UI、跨平台 ADB 打包、更多 IDE 集成。

## 官方参考链接
- IntelliJ Platform Gradle Plugin 2.x：`https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin.html`
- Creating Actions：`https://plugins.jetbrains.com/docs/intellij/creating-actions-tutorial.html`
- Actions：`https://plugins.jetbrains.com/docs/intellij/plugin-actions.html`
- Tool Windows：`https://plugins.jetbrains.com/docs/intellij/tool-windows.html`
- Persisting State of Components：`https://plugins.jetbrains.com/docs/intellij/persisting-state-of-components.html`
- Execution API：`https://plugins.jetbrains.com/docs/intellij/execution.html`

## 审视 `add-autojs6-jetbrains-mvp` 的增量发现
- 当前 MVP 提案覆盖：设备连接、IP/ADB 连接、hello 握手、当前文件 run/save、stop/stopAll、新建项目模板、在线文档入口。
- 与 VSCode 扩展 18 个命令相比，MVP 明确遗漏：指定设备 run/save、rerun、newUntitledFile、runProject/saveProject、命令层级、HTTP `/exec` 远程命令入口、设备侧反向 command、日志展示/调试输出、连接向导细节、IP 记录清理、二维码入口（源码中存在但当前注释/未启用）、breakpoints 贡献点。
- 项目同步差距较大：源扩展通过 `ProjectObserver.diff()` 维护 mtime 差量，zip modified 文件，携带 md5/deletedFiles/override，再用 `bytes_command` 发送 `run_project`/`save_project`。
- 连接 UX 差距较大：源扩展有四类连接入口（AutoJs6 客户端连 IDE LAN/QR、IDE 连 AutoJs6 服务端 LAN、IDE 经 ADB 连接），并维护最近 IP、清理记录、网卡列表提示、ADB provider 查询/forward、错误引导。
- 后续高阶提案必须聚焦 VSCode 扩展全量功能对齐；任何 JetBrains-native helper 只能作为真实 parity 能力的展示或诊断增强，不能替代 parity。

## 阶段 7 发现：四条规则审视与提案修正
- 用户补充的底线应作为 MVP 和高阶 parity 都不可违背的全局规则，而不是 post-MVP 才执行的目标。
- 规则整理为四条：
  1. 历史项目 100% 运行兼容：不改项目格式、运行方式、协议字段、路径/编码语义、端口和命令名。
  2. 保留用户习惯但采用 JetBrains 最佳实践：命令/快捷键意图/连接方式/运行保存停止心智模型保持熟悉，UI 用 JetBrains Action、Tool Window、Settings、Notification、Background Task、Disposer 等最佳实践承载。
  3. 功能只增不减：MVP 可以范围小，但已声明实现的能力必须等价或更强；完整 parity 阶段必须用矩阵证明每个 VSCode 功能都有证据或明确批准的例外。
  4. 禁止 mock/fake/推测：未实现、未验证或含义不清的能力必须标为 deferred/blocked/requires verification，不能用假成功、空实现、静态数据、猜测协议冒充完成。
- 发现的潜在冲突：MVP 原先的 “不做完整迁移/不做项目同步/不做 HTTP” 容易被误读为可以缺功能；已改为“明确 deferred，不能破坏兼容，不能 mock”。
- 两个 OpenSpec change 修改后均通过 validate。

## 阶段 8 发现：发布与兼容目标已关闭
- 用户确认目标是“要全量和 VSCode 功能对齐”。因此移除非 parity capability，不能扩展成额外功能范围来替代 parity。
- 用户确认需要发布，但由用户自己发布。已新增 `openspec/changes/complete-autojs6-vscode-parity/release-guide.md` 草案，要求覆盖本地 ZIP、Marketplace、签名、Plugin Verifier、版本/changelog、回滚、私有分发、故障排查。
- 用户确认需要 JetBrains 全家桶通过，禁止绑定某一个 IDE；如果确实有个别例外，必须“全量兼容 + 个别例外矩阵”，且例外需要证据、原因、影响和替代方案。
- MVP 原先偏向 IntelliJ IDEA 的措辞已改成：不得绑定某一个 JetBrains IDE；实现应基于通用 IntelliJ Platform API，未验证 IDE 不得宣传为支持。
- `complete-autojs6-vscode-parity` 修改后仍通过 OpenSpec validate。

## 阶段 9 发现：范围表述清理完成
- OpenSpec 正文现在只保留三个明确目标：VSCode 扩展全量功能对齐、用户自行发布文档、JetBrains 全家桶兼容。
- 任何 JetBrains-native helper 只允许作为真实 parity 能力的展示、诊断或安全增强，不能替代 VSCode parity。
- 关键词扫描已确认没有残留会导致后续实现误解的历史范围词。
