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

### Requirement: Defer Project Run Configuration

插件 SHALL NOT 在本变更中注册、展示或暗示 `AutoJs6 Project` Run Configuration 支持。

#### Scenario: User opens run configuration list

- **WHEN** 用户查看可创建的 AutoJs6 运行配置类型
- **THEN** 只出现单文件 `AutoJs6 Script` 配置
- **AND** 不出现 `AutoJs6 Project` 配置

#### Scenario: Project run support is requested

- **WHEN** 用户需要运行整个 AutoJs6 项目
- **THEN** 插件文档或提示说明项目 Run Configuration 需要等待项目运行/项目同步能力完成后另行支持
- **AND** 插件不得用单文件 run command、空 zip、假 md5 或假 `bytes_command` 冒充项目运行

