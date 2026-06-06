# script-run-configuration Specification

## Purpose
TBD - created by archiving change add-autojs6-script-run-configuration. Update Purpose after archive.
## Requirements
### Requirement: Register AutoJs6 Script Run Configuration

插件 SHALL 注册 JetBrains Run Configuration 类型 `AutoJs6 Script`，用于运行单个本地 JavaScript 文件。

#### Scenario: Run configuration type is available

- **WHEN** 用户打开 `Run | Edit Configurations...`
- **THEN** 用户可以创建 `AutoJs6 Script` 运行配置
- **AND** 该配置名称和说明明确表示只运行单个脚本文件

### Requirement: Configure Single Script File

`AutoJs6 Script` 运行配置 SHALL 保存一个本地 `.js` 文件路径作为运行目标。

#### Scenario: User selects a local JS file

- **WHEN** 用户在运行配置中选择本地 `.js` 文件
- **THEN** 配置保存该文件路径
- **AND** 后续运行使用该文件路径读取脚本内容

#### Scenario: User selects directory or non-JS file

- **WHEN** 用户选择目录、非本地文件或非 `.js` 文件
- **THEN** 配置校验失败并显示错误
- **AND** 插件不得发送运行命令

### Requirement: Create Configuration From Current JS File

插件 SHALL 支持从当前编辑器或 Project View 中的本地 `.js` 文件创建 `AutoJs6 Script` 运行配置。

#### Scenario: Current editor is a local JS file

- **WHEN** 用户在打开的本地 `.js` 文件中创建或运行 AutoJs6 Script 配置
- **THEN** 配置默认使用当前文件路径

#### Scenario: Selected file in Project View is a local JS file

- **WHEN** 用户从 Project View 选择本地 `.js` 文件并创建 AutoJs6 Script 配置
- **THEN** 配置默认使用所选文件路径

### Requirement: Execute Script Run Configuration

`AutoJs6 Script` 运行配置 SHALL 读取配置中的脚本文件，并向所有已连接 AutoJs6 设备发送单文件 `run` command。

#### Scenario: Script file exists and device is connected

- **WHEN** 用户运行有效的 AutoJs6 Script 配置
- **THEN** 插件读取脚本文件内容
- **AND** 插件发送 command `run`
- **AND** payload 包含文件路径作为 `id`、文件名作为 `name`、文件文本作为 `script`

#### Scenario: No device is connected

- **WHEN** 用户运行 AutoJs6 Script 配置但没有已连接设备
- **THEN** 插件报告未发现已连接设备
- **AND** 插件不得显示运行成功
- **AND** 插件不得发送命令

#### Scenario: Script file no longer exists

- **WHEN** 用户运行 AutoJs6 Script 配置但配置文件不存在
- **THEN** 插件报告脚本文件不存在
- **AND** 插件不得发送命令

### Requirement: Integrate With JetBrains Run UX

`AutoJs6 Script` 运行配置 SHALL 可通过 JetBrains 标准运行入口执行。

#### Scenario: User clicks green Run button

- **WHEN** 用户选择 AutoJs6 Script 运行配置并点击绿色 Run 按钮
- **THEN** 插件执行该配置对应的单文件 run command

#### Scenario: User invokes recent run action

- **WHEN** 用户通过最近运行记录或 `Shift+F10` 重新运行 AutoJs6 Script 配置
- **THEN** 插件再次执行同一个脚本文件配置

### Requirement: Register AutoJs6 Project Run Configuration

插件 SHALL 注册 JetBrains Run Configuration 类型 `AutoJs6 Project`，用于运行包含 `project.json` 的 AutoJs6 项目。

#### Scenario: Project run configuration type is available

- **WHEN** 用户打开 `Run | Edit Configurations...`
- **THEN** 用户可以创建 `AutoJs6 Project` 运行配置
- **AND** 该配置名称和说明明确表示运行 AutoJs6 项目而非单个脚本

### Requirement: Configure AutoJs6 Project Root

`AutoJs6 Project` 运行配置 SHALL 保存一个包含 `project.json` 的本地项目根目录。

#### Scenario: User selects a valid AutoJs6 project

- **WHEN** 用户在运行配置中选择包含 `project.json` 的目录
- **THEN** 配置保存该目录路径
- **AND** 后续运行使用该目录计算 project diff

#### Scenario: User selects invalid project root

- **WHEN** 用户选择不存在、非目录或缺少 `project.json` 的路径
- **THEN** 配置校验失败并显示错误
- **AND** 插件不得发送 project bytes_command

### Requirement: Create Project Configuration From Context

插件 SHALL 支持从当前文件、Project View 文件夹或 `project.json` 所在上下文创建 `AutoJs6 Project` 运行配置。

#### Scenario: Current context belongs to AutoJs6 project

- **WHEN** 用户从 AutoJs6 项目内的文件或目录创建运行配置
- **THEN** 配置默认使用最近的包含 `project.json` 的项目根目录

### Requirement: Execute Project Run Configuration

`AutoJs6 Project` 运行配置 SHALL 使用与 `Run Project` action 相同的 project sync 语义运行项目。

#### Scenario: Project exists and device is connected

- **WHEN** 用户运行有效的 `AutoJs6 Project` 配置
- **THEN** 插件计算 project diff、zip、md5
- **AND** 插件先发送 bytes payload
- **AND** 随后发送 command 为 `run_project` 的 JSON `bytes_command`

#### Scenario: No device is connected

- **WHEN** 用户运行 `AutoJs6 Project` 配置但没有已连接设备
- **THEN** 插件报告未发现已连接设备
- **AND** 插件不得显示运行成功
- **AND** 插件不得发送 project command

