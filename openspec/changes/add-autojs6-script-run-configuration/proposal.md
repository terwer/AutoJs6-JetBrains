## Why

当前 MVP 已经验证单文件 `run` 命令链路可用，但它只通过 JetBrains Action 触发，无法进入 JetBrains 标准 Run/Debug 工作流。用户在 IDEA/WebStorm/PyCharm 等 IDE 中习惯通过 Run Configuration、绿色运行按钮、最近运行记录和 `Shift+F10` 运行任务，因此需要为“单个 JS 文件运行”补齐 JetBrains 原生 Run Configuration。

本变更只覆盖**单文件运行配置**。项目运行配置不在本变更中实现，必须等 `Run Project` / 项目同步协议 / `bytes_command` 能力完成并验证后另行提案。

## What Changes

- 新增 `AutoJs6 Script` Run Configuration 类型，用于运行一个明确指定的 `.js` 文件。
- 支持从当前编辑器或 Project View 中的 `.js` 文件创建/保存运行配置。
- Run Configuration 执行时复用现有单文件 `run` 命令语义：发送 `command=run`、文件路径 `id`、文件名 `name`、文件内容 `script`。
- 运行前必须检查：脚本文件存在、文件为本地文件、至少有一个真实已连接 AutoJs6 设备。
- 未连接设备、文件不存在、选择目录或非 JS 文件时必须报错，不允许假成功。
- 明确不支持项目运行配置；不注册 `AutoJs6 Project` Run Configuration；不发送 `bytes_command`；不生成空 zip、假 md5 或假同步结果。
- 后续项目运行支持完成后，再单独新增 `AutoJs6 Project` Run Configuration 提案。

## Capabilities

### New Capabilities

- `script-run-configuration`: 定义 AutoJs6 单文件 Run Configuration 的创建、保存、执行、校验和错误处理行为。

### Modified Capabilities

- `script-command-actions`: 补充要求：Run Configuration 执行单文件时必须与现有 Run Current File 的 command payload 行为一致，不引入新的单文件运行协议。

## Impact

- 影响 IntelliJ Platform Run Configuration 相关代码：`ConfigurationType`、`RunConfiguration`、`RunConfigurationProducer`、settings editor、executor/program runner 或 run profile state。
- 影响现有脚本命令发送服务：需要暴露可复用的“按文件发送 run command”逻辑，避免 Action 和 Run Configuration 各自拼 payload 导致不一致。
- 不影响项目模板、新建项目、项目同步、HTTP 入口或设备侧协议。
- 不引入项目运行能力；项目 Run Configuration 作为明确 deferred 项。
