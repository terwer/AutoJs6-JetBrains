# debug-and-breakpoint-parity Specification

## Purpose
TBD - created by archiving change complete-autojs6-vscode-parity. Update Purpose after archive.
## Requirements
### Requirement: 保留 JavaScript breakpoint 可发现性
插件 MUST记录并暴露 JavaScript breakpoints 与 AutoJs6 script workflows 在 JetBrains IDEs 中的关系。

#### Scenario: 用户打开 JavaScript file
- **WHEN** JavaScript file 在 editor 中打开
- **THEN** AutoJs6 documentation 或 help 说明 IDE breakpoints 当前是 supported、ignored，还是需要 future debug adapter

### Requirement: 定义 Debug Boundary
插件 MUST区分 run/save command execution 与 full debugger attachment，且不得暗示不受支持的 breakpoint stepping behavior。

#### Scenario: 用户调用 debug-related help
- **WHEN** 用户从 plugin UI 请求 AutoJs6 debug capability
- **THEN** 插件显示当前 supported debug boundary 和 next steps

### Requirement: 准备未来 Debug Extension Point
插件 MUST保持 protocol 和 action architecture 可扩展，以便未来接入 AutoJs6 debug adapter 或 debugger integration。

#### Scenario: 未来加入 debug protocol
- **WHEN** 后续 change 增加 debugger support
- **THEN** 它可以复用现有 device connection 和 command dispatch services，而不需要重写 action parity

