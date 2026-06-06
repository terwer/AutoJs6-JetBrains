# 任务计划：JetBrains 原生 Run 输出接入 AutoJs6 日志探索

## 目标
探索并验证：当通过 JetBrains Run/Toolbar/Action 运行 AutoJs6 文件或项目时，能否把设备侧 `logger.*` / `console.*` / 协议 `log` 消息同步打印到 JetBrains 原生 Run 输出窗口，而不是只显示“命令已发送”。

## 当前阶段
阶段 4

## 各阶段

### 阶段 1：现象与运行链路确认
- [x] 记录用户截图中的现象与缺口
- [x] 定位 Run Configuration / Action 当前向 Run 窗口输出的代码路径
- [x] 定位设备协议 `log` 消息当前进入插件的位置
- **状态：** complete

### 阶段 2：JetBrains 原生输出接入点探索
- [x] 查明当前 Run Configuration 使用的 `ProcessHandler` / `ExecutionConsole` 类型
- [x] 判断是否可以把设备日志写入 `ProcessHandler.notifyTextAvailable(...)`
- [x] 判断 Action/Toolbar 非 Run Configuration 入口是否有原生输出窗口可复用或需要创建 RunContentDescriptor
- **状态：** complete

### 阶段 3：最小方案设计
- [x] 设计“本次运行会话”与“设备 log”关联方式
- [x] 设计终止/重跑/多设备/多 run 配置的边界
- [x] 明确不改变 AutoJs6 协议、不 mock、不伪造成功
- **状态：** complete

### 阶段 4：可行性结论与下一步
- [x] 给出是否有办法、推荐实现方案、风险
- [x] 方案足够小，已实现 Run Configuration 原生输出桥接
- **状态：** complete

## 关键问题
1. AutoJs6 设备是否已经通过现有 TCP 协议把 `logger.info(...)` 之类日志发回 IDE？
2. 目前日志是否只写到了 Tool Window / notification / internal log，而没有写进 Run console？
3. JetBrains Run console 是否能在“远端设备脚本仍在运行”的生命周期内保持打开并追加日志？

## 已做决策
| 决策 | 理由 |
|------|------|
| 新建独立探索计划，不覆盖前一个发布文档计划内容 | 用户要求“重启新的plan 探索”，本问题是独立调试体验问题 |
| 优先读源码和当前运行链路，不先猜 JetBrains API | 需以 live/runtime 代码路径为准 |

## 遇到的错误
| 错误 | 尝试次数 | 解决方案 |
|------|---------|---------|
| 暂无 | 0 | - |

## 备注
- 用户截图显示：JetBrains Run 窗口只捕获到 `AutoJs6 Project: sent command=run_project...`，AutoJs6 设备端 Toast 显示脚本确实执行了 `auto_fgcq hello1`，但 `logger.info(...)` 未出现在 Run 输出。
- 目标输出位置是 JetBrains 原生 Run/Debug 输出，不只是插件 Tool Window。


