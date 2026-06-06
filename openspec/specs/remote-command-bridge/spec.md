# remote-command-bridge Specification

## Purpose
TBD - created by archiving change complete-autojs6-vscode-parity. Update Purpose after archive.
## Requirements
### Requirement: 提供 HTTP Exec Endpoint
插件 MUST提供 configurable HTTP `/exec` endpoint，并兼容 VSCode 扩展 query parameters `cmd` 和 `path`。

#### Scenario: Valid exec command arrives
- **WHEN** HTTP request 到达 `/exec`，且包含 supported `cmd`
- **THEN** 插件使用提供的 path parameter 分发对应 AutoJs6 action

### Requirement: 默认保护 HTTP Bridge
插件 MUST默认保持 HTTP bridge disabled 或 loopback-bound，并且 wider network binding 必须要求明确 user configuration。

#### Scenario: Fresh plugin install
- **WHEN** 插件首次启动
- **THEN** HTTP bridge 不会在没有 explicit user consent 的情况下暴露到所有 network interfaces

### Requirement: 处理 Device Reverse Commands
插件 MUST 只允许处理 command 在 supported parity action list 中的 device-originated command payloads。

#### Scenario: Device sends supported command
- **WHEN** connected device 发送包含 supported command name 的 command payload
- **THEN** 插件通过与用户调用相同的 action path 分发该 command

#### Scenario: Device sends unknown command
- **WHEN** connected device 发送包含 unknown command name 的 command payload
- **THEN** 插件拒绝该 command 并报告错误，不执行任意行为

