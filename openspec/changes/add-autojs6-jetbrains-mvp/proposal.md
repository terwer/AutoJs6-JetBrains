## Why

AutoJs6 目前已有 VSCode 扩展，但用户希望在 JetBrains IDE 全家桶中获得同类工作流：从 IDE 连接 AutoJs6 设备，运行当前脚本、保存脚本、停止脚本，并创建 AutoJs6 项目。

已探索的 VSCode 扩展显示，核心价值不在 VSCode 专属语言能力，而在设备连接、TCP/ADB 协议、编辑器文件命令和项目模板。因此应先定义一个 JetBrains MVP，避免一开始尝试完整等价迁移导致范围过大。

## What Changes



### Non-Negotiable Compatibility Rules

These rules apply to the MVP and every later high-level capability. MVP scope may be smaller, but implemented behavior MUST NOT violate these rules:

1. **Historical project compatibility first.** The plugin SHALL remain 100% compatible with existing AutoJs6 project runtime behavior, project layout, protocol payloads, command names, ports, template semantics, and device expectations observed from the VSCode extension. No incompatible migration or silent behavior change is allowed.
2. **Preserve user habits while adopting JetBrains best practices.** The plugin SHALL keep existing AutoJs6/VSCode-extension user workflows reachable through familiar actions, names, shortcuts or close equivalents, while presenting them through JetBrains-native Action, Tool Window, Settings, Notification, and background-task patterns.
3. **Feature set can grow but must not shrink.** A capability claimed as implemented SHALL be at least behavior-equivalent to the corresponding VSCode extension feature unless an accepted, explicitly documented compatibility exception is approved.
4. **No mock, fake, or speculative implementation.** Unimplemented or uncertain behavior SHALL be labeled as deferred, blocked, or requiring verification; it MUST NOT be mocked, faked, guessed, or presented as working without evidence.
新增 AutoJs6 JetBrains 插件 MVP 能力：

- 在 JetBrains IDE 中注册 AutoJs6 操作入口。
- 支持 AutoJs6 设备连接、断开和握手。
- 支持 IDE 监听 AutoJs6 客户端连接。
- 支持通过 IP 主动连接 AutoJs6 服务端。
- 支持通过 ADB 发现设备并进行端口转发连接。
- 支持运行当前编辑器 JS 文件。
- 支持保存当前编辑器 JS 文件到设备。
- 支持停止当前脚本和停止所有脚本。
- 支持从 AutoJs6 模板创建新项目。

不在本次变更中实现完整项目差量同步、HTTP 远程命令入口、自定义语言语法/补全、Marketplace 发布流程。

## Capabilities

### New Capabilities

- `device-connection`: AutoJs6 设备发现、TCP 连接、ADB 连接、握手、断开和连接状态管理。
- `script-command-actions`: JetBrains Action 入口，以及针对当前编辑器文件的 run/save/stop/stopAll 命令发送。
- `autojs6-project-template`: 从内置模板创建 AutoJs6 项目，并替换项目名与包名占位符。

### Modified Capabilities

无。当前 `openspec/specs` 下没有既有 capability，本变更为首次引入。

## Impact

- 新增 JetBrains 插件项目的架构需求，但本提案不写业务代码。
- 后续实现会涉及 IntelliJ Platform：Action System、Editor/Document API、VirtualFile、PersistentStateComponent、Dialog/Notification、Application/Project Service 生命周期。
- 后续实现会涉及本地网络 socket、ADB 外部进程调用、端口占用处理和 IDE dispose 清理。
- MVP 协议需兼容源 VSCode 扩展已观察到的 AutoJs6 通信格式：8 字节 header、JSON/bytes payload、hello 握手、command JSON；不得以 mock/fake 替代真实协议实现。
