## 1. 范围与口径确认

- [x] 1.1 确认本变更只实现 `AutoJs6 Script` 单文件 Run Configuration。
- [x] 1.2 明确项目 Run Configuration、Run Project、Save Project、项目 zip/md5/bytes_command 均为 deferred。
- [x] 1.3 更新用户文档，说明单文件 Run Configuration 与项目运行配置的边界。

## 2. Run Configuration 注册

- [x] 2.1 新增 `AutoJs6 Script` ConfigurationType。
- [x] 2.2 新增单文件 RunConfiguration 数据模型，保存 `scriptPath`。
- [x] 2.3 新增 SettingsEditor，允许选择本地 `.js` 文件。
- [x] 2.4 注册 JetBrains Run Configuration 扩展点，确保 `Run | Edit Configurations...` 可见。

## 3. 配置创建体验

- [x] 3.1 支持从当前编辑器本地 `.js` 文件创建 AutoJs6 Script 配置。
- [x] 3.2 支持从 Project View 中选中的本地 `.js` 文件创建 AutoJs6 Script 配置。
- [x] 3.3 对目录、非本地文件、非 `.js` 文件给出校验错误。

## 4. 单文件执行逻辑

- [x] 4.1 抽取或复用现有单文件 `run` payload 构造逻辑，避免 Action 与 Run Configuration 行为分叉。
- [x] 4.2 Run Configuration 执行时读取配置文件路径、文件名和文本内容。
- [x] 4.3 向所有已连接 AutoJs6 设备发送 `command=run`。
- [x] 4.4 无连接设备时失败并提示，不显示假成功。
- [x] 4.5 文件不存在或不可读取时失败并提示，不发送命令。

## 5. JetBrains Run UX 集成

- [x] 5.1 支持绿色 Run 按钮执行 AutoJs6 Script 配置。
- [x] 5.2 支持最近运行记录和 `Shift+F10` 重新运行同一配置。
- [x] 5.3 确保运行结果/错误通过 JetBrains 原生 Run/Notification 体验呈现。

## 6. 项目运行明确延后

- [x] 6.1 确保不注册 `AutoJs6 Project` Run Configuration。
- [x] 6.2 确保不发送 `bytes_command`、不创建空 zip、不生成假 md5、不假装项目运行成功。
- [x] 6.3 在文档中写明项目 Run Configuration 待 Run Project/项目同步能力完成后另行提案。

## 7. 验证

- [x] 7.1 单元测试：配置保存/读取 `scriptPath`。
- [x] 7.2 单元测试：`.js` 文件校验通过，目录/非 JS/缺失文件校验失败。
- [x] 7.3 协议回放测试：Run Configuration 发送 payload 与 Run Current File 一致。
- [ ] 7.4 IDE 手工验证：`Run | Edit Configurations...` 可创建 `AutoJs6 Script`。
- [ ] 7.5 IDE 手工验证：绿色 Run 按钮可运行已连接设备上的单个 JS 文件。
- [ ] 7.6 IDE 手工验证：无设备时 Run Configuration 报错且不显示假成功。
