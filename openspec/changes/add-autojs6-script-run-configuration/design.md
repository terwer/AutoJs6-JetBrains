## Context

当前插件已经具备 AutoJs6 单文件运行 Action：从当前编辑器读取本地 `.js` 文件路径、文件名和文本内容，并向已连接设备发送 `run` command。用户已经验证单文件 Action 路径可用。

但 JetBrains 用户通常通过 Run Configuration、顶部绿色 Run 按钮、最近运行记录和 `Shift+F10` 来重复运行任务。仅有 Action 会让 AutoJs6 单文件运行游离于 IDE 标准运行体系之外，也不利于保存常用脚本路径。

本设计只处理“单个 JS 文件”的 Run Configuration。项目运行涉及 `bytes_command`、zip、md5、deletedFiles、override 和项目同步状态，目前尚未实现，因此不能在本变更中注册或暗示项目运行配置。

## Goals / Non-Goals

**Goals:**

- 新增 `AutoJs6 Script` Run Configuration 类型。
- 支持保存一个明确的本地 `.js` 文件路径作为运行目标。
- 支持从当前编辑器或 Project View 中的 `.js` 文件创建运行配置。
- 点击 JetBrains Run 后复用现有单文件 `run` command 行为。
- 与现有 Run Current File Action 的 payload 完全一致。
- 没有连接设备或文件无效时明确失败，不显示假成功。

**Non-Goals:**

- 不实现 `AutoJs6 Project` Run Configuration。
- 不实现 Run Project / Save Project。
- 不实现项目 zip、md5、deletedFiles、override 或 `bytes_command`。
- 不新增设备选择 UI 的复杂能力；MVP 可以默认发送到所有已连接设备。
- 不改变设备侧协议。

## Decisions

1. **Run Configuration 只绑定单个 `.js` 文件**
   - 决策：配置项保存 `scriptPath`，执行时读取该文件内容发送 `run` command。
   - 理由：单文件运行链路已验证，且不会触及项目同步协议。
   - 替代方案：直接用当前编辑器运行。该方案不适合作为持久 Run Configuration，因为配置应能重复运行固定目标。

2. **复用现有单文件 command payload**
   - 决策：Run Configuration 执行时发送 `{ command: "run", id: path, name: fileName, script: text }`。
   - 理由：避免产生第二套单文件运行协议，也保证与已通过测试的 Action 行为一致。

3. **连接服务必须使用全局连接列表**
   - 决策：Run Configuration 执行时读取当前全局 AutoJs6 连接服务中的设备列表。
   - 理由：Run Configuration 可能从不同项目上下文触发；不能因为 Project Service 分裂导致“已连接但运行配置看不到设备”。

4. **项目运行配置明确 deferred**
   - 决策：本变更不注册 `AutoJs6 Project` 配置类型，不显示项目运行入口。
   - 理由：项目运行未实现前提供入口会造成假支持；必须等项目同步协议完成后另行提案。

## Risks / Trade-offs

| 风险 | 处理 |
|---|---|
| 用户误以为 Run Configuration 支持项目运行 | 配置类型命名为 `AutoJs6 Script`，文档和错误提示明确只支持单文件 |
| 配置保存的文件被删除或移动 | 运行时校验文件存在；不存在则报错并不发送命令 |
| 没有已连接设备 | 运行时失败并提示连接设备，不显示成功 |
| Action 和 Run Configuration 拼接 payload 不一致 | 抽取或复用同一段单文件 run payload 构造逻辑 |
| 后续支持项目运行时命名冲突 | 预留单独 `AutoJs6 Project` Run Configuration，由后续项目运行提案实现 |
