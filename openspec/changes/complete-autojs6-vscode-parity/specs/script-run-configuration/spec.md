## ADDED Requirements

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

## REMOVED Requirements

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
