# vscode-parity-actions Specification

## Purpose
TBD - created by archiving change complete-autojs6-vscode-parity. Update Purpose after archive.
## Requirements
### Requirement: 注册全部 VSCode Parity Actions
插件 MUST提供与所有用户可见 VSCode extension commands 对应的 JetBrains actions：viewDocument、connect、disconnectAll、run、runWithoutArguments、commandsHierarchy、runOnDevice、stop、stopAll、rerun、save、saveToDevice、newUntitledFile、newProject、saveProject、saveProjectWithoutArguments、runProject 和 runProjectWithoutArguments。

#### Scenario: Plugin actions are loaded
- **WHEN** IDE 加载插件
- **THEN** 每个 VSCode parity command 都可通过 JetBrains action infrastructure 使用

### Requirement: 保留上下文入口
插件 MUST从 editor 和 Project View contexts 暴露 script actions，并从 folder/project contexts 暴露 project actions。

#### Scenario: JavaScript file context is opened
- **WHEN** 用户打开 JavaScript file 的 context menu
- **THEN** run、runOnDevice、stop、stopAll、rerun、saveToDevice 和 save actions 可用

#### Scenario: Folder context is opened
- **WHEN** 用户打开 folder 或 AutoJs6 project 的 context menu
- **THEN** runProject、saveProject 和 newProject actions 可用

### Requirement: 支持 Suggested Keymap Parity
插件 MUST注册 suggested keybindings 或 keymap metadata，使其匹配 VSCode 扩展意图，同时允许 JetBrains 用户覆盖冲突。

#### Scenario: User searches keymap
- **WHEN** 用户在 Keymap settings 中搜索 AutoJs6 actions
- **THEN** actions 显示与 VSCode extension shortcuts 意图等价的 recommended shortcuts（在可行范围内）

### Requirement: Rerun Current Script
插件 MUST通过先 stop 当前脚本再 run 当前脚本的方式实现 rerun。

#### Scenario: User invokes rerun
- **WHEN** 存在 current editor file 和 device connection，且用户调用 rerun
- **THEN** 插件为同一文件先发送 stop，再发送 run

### Requirement: 创建 New Untitled Script
插件 MUST提供一个 action，用于创建新的 unsaved editor document，方便快速起草 AutoJs6 script。

#### Scenario: User invokes newUntitledFile
- **WHEN** 用户调用 New Untitled File
- **THEN** IDE 打开一个新的 unsaved editor document，准备进行 script editing

