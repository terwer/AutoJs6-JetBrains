## Why

`add-autojs6-jetbrains-mvp` 适合作为第一版可运行插件，但它主动排除了大量 VSCode 扩展能力：项目差量同步、指定设备操作、HTTP 远程命令、设备日志/反向命令、完整连接向导、快捷命令层级、重新运行、新建未命名脚本、调试/断点与发布体验。若目标是让 `AutoJs6-JetBrains` 在 MVP 后 100% 替代 VSCode 插件，需要一个独立的 post-MVP 全面对齐提案。

## What Changes



### Non-Negotiable Compatibility Rules

This change exists to enforce the same rules at full-parity level:

1. **Historical project compatibility first.** Existing AutoJs6 projects and VSCode-extension-compatible workflows SHALL continue to run without migration, format conversion, incompatible path rules, or protocol changes.
2. **Preserve user habits while adopting JetBrains best practices.** JetBrains-native UX must improve discoverability and reliability without removing familiar commands, shortcut intent, connection flows, project behavior, or run/save/stop mental models.
3. **Feature set can grow but must not shrink.** Parity means every VSCode extension command, menu intent, protocol behavior, project sync behavior, connection path, and visible workflow has passing evidence or an explicitly approved documented exception; JetBrains-only enhancements are additive.
4. **No mock, fake, or speculative implementation.** Claimed support requires real implementation and verification. Device protocol details, HTTP behavior, ADB provider behavior, debugger limits, publishing steps, and IDE compatibility must be verified or marked clearly before implementation, not guessed.
新增 MVP 之后的全面对齐路线：

- 补齐 VSCode 扩展 18 个命令及其菜单/快捷键/Project View/Editor 入口。
- 补齐指定设备 run/save、重新运行脚本、运行/保存项目、新建未命名脚本等动作。
- 实现 AutoJs6 项目识别、`project.json` 读取、ignore 过滤、mtime 差量、zip、md5、deletedFiles、override 与 `bytes_command` 协议。
- 实现设备 Tool Window / 状态栏 / 通知 / 输出控制台，展示连接设备、日志、连接类型、错误与操作结果。
- 实现完整连接体验：本机网卡选择、AutoJs6 客户端连接提示、最近 IP 记录、清理记录、LAN 主动连接、ADB provider 查询与 forward、可选二维码提示。
- 实现 HTTP `/exec` 远程命令入口及设备侧反向 command，保持与 VSCode 扩展行为兼容并增加安全开关。
- 建立 parity 验收矩阵：以 VSCode 扩展 `package.json` 命令、菜单、快捷键和源码协议为基准逐项验证。
- 建立面向替代 VSCode 插件的发布、兼容、JetBrains IDE 全家桶测试和回归基线。
- 提供 `release-guide.md`，覆盖用户自行 Marketplace 发布、私有 ZIP 分发、签名、Plugin Verifier、回滚、兼容矩阵和发布阻断条件。

## Capabilities

### New Capabilities

- `vscode-parity-actions`: 覆盖 VSCode 扩展全部命令、菜单入口、快捷键建议与上下文约束。
- `project-diff-sync`: AutoJs6 项目识别、配置读取、ignore 过滤、差量 zip、md5、deletedFiles、run_project/save_project。
- `device-tool-window-and-logs`: 设备列表、连接状态、日志输出、断开事件、操作结果和错误诊断 UI。
- `advanced-connection-experience`: 本机网卡提示、最近 IP 管理、LAN/ADB/可选 QR 连接向导、ADB provider/forward 细节。
- `remote-command-bridge`: HTTP `/exec` 远程命令入口、设备侧反向 command 与安全控制。
- `debug-and-breakpoint-parity`: JavaScript breakpoint 贡献等价、脚本调试入口规划和后续调试协议扩展边界。
- `release-and-compatibility`: JetBrains IDE 全家桶兼容、用户自行发布文档、Marketplace/ZIP 发布步骤、资源/ADB 打包、回归测试矩阵。

### Modified Capabilities

无。当前 `openspec/specs` 尚无已归档 capability；本提案作为 MVP 之后的新 capability 集合。实现时应以 `add-autojs6-jetbrains-mvp` 为前置基线。

## Impact

- 影响 JetBrains 插件 Action、Tool Window、Status Bar、Settings、PersistentState、Editor/VirtualFile、Project View、Notifications、BrowserUtil、Disposer 生命周期。
- 影响设备协议层：JSON/bytes frame、`bytes_command`、设备反向 command、日志事件、HTTP server。
- 影响本地文件系统：项目遍历、ignore 过滤、zip、md5、文件 watcher、模板/片段资源。
- 影响外部进程：ADB 执行、provider 查询、端口 forward、跨平台工具定位。
- 影响质量体系：协议单元测试、集成测试、设备回放、runIde 验证、JetBrains IDE 全家桶兼容验证与发布检查。


