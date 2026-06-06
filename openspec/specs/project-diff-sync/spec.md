# project-diff-sync Specification

## Purpose
TBD - created by archiving change complete-autojs6-vscode-parity. Update Purpose after archive.
## Requirements
### Requirement: 检测 AutoJs6 Project
插件 MUST把包含 `project.json` 的目录视为 AutoJs6 project；当文件缺失时，必须拒绝 project commands。

#### Scenario: Project JSON 缺失
- **WHEN** 用户在没有 `project.json` 的目录上调用 runProject 或 saveProject
- **THEN** 插件报告需要 AutoJs6 project，且不发送 project command

### Requirement: 加载 Project Configuration
插件 MUST解析 `project.json`，并使用其中的 `ignore` entries 在 project synchronization 中过滤文件。

#### Scenario: 存在 ignored file
- **WHEN** 文件路径匹配 `project.json` 中的 ignore entry
- **THEN** 插件从 project diff payload 中排除该文件

### Requirement: 计算 Project Diff
插件 MUST维护 per-project/per-device file modification state，并用 file modification timestamps 计算 modified 和 deleted relative paths。

#### Scenario: 首次同步后文件发生变化
- **WHEN** project file modification time 在 previous sync 后发生变化
- **THEN** 下一次 project diff 在 modified files 中包含该 relative path

#### Scenario: 首次同步后文件被删除
- **WHEN** previously synced file 不再存在
- **THEN** 下一次 project diff 在 deletedFiles 中包含该 relative path

### Requirement: 发送 Project Bytes Command
插件 MUST zip modified files，计算 zip bytes 的 md5，先发送 bytes payload，再发送包含 id、name、deletedFiles、override 和 command 的 JSON `bytes_command` payload。

#### Scenario: 用户运行项目
- **WHEN** 用户对有效 AutoJs6 project 调用 runProject
- **THEN** 插件发送 zip bytes，随后发送 command 为 `run_project` 的 `bytes_command` JSON payload

#### Scenario: 用户保存项目
- **WHEN** 用户对有效 AutoJs6 project 调用 saveProject
- **THEN** 插件发送 zip bytes，随后发送 command 为 `save_project` 的 `bytes_command` JSON payload

### Requirement: 报告 Project Sync Progress
插件 MUST在 background task 中执行 project zip 和 md5 工作，并提供可见 progress、cancellation handling 和 error reporting。

#### Scenario: Large project sync starts
- **WHEN** 用户对 non-trivial project 调用 project sync command
- **THEN** 插件显示 progress，并保持 IDE UI responsive

### Requirement: 保持 Project Sync 历史兼容
插件 MUST保持 project synchronization 行为与 VSCode 扩展语义兼容，包括 project detection、ignore filtering、mtime diff state、zip relative paths、md5、deletedFiles、override、bytes payload ordering，以及 run_project/save_project command names。

#### Scenario: Existing project is synchronized
- **WHEN** 从 JetBrains 运行或保存 existing AutoJs6 project
- **THEN** 生成的 bytes payload 和 JSON bytes_command 与现有 device-side expectations 兼容

### Requirement: 禁止 Fake Project Sync Success
当 zip creation、md5 calculation、bytes transfer、JSON bytes_command dispatch 或 device/replay verification 失败时，插件 MUST NOT报告 project sync success。

#### Scenario: Sync implementation incomplete
- **WHEN** 任一必需 project sync step 未实现或未验证
- **THEN** 插件将该步骤报告为 failed、deferred、blocked 或 requiring verification，而不是假装项目已同步

