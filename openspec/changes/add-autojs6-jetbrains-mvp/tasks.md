## 1. Scope Confirmation



- [ ] 0.1 建立并确认四条不可违背规则：历史项目 100% 运行兼容、保留用户习惯且采用 JetBrains 最佳实践、功能只增不减、禁止 mock/fake/推测。
- [ ] 0.2 为 MVP 输出 compatibility ledger：每个已实现能力都要列出对应 VSCode 行为、真实实现证据、验证方式和是否存在 deferred 项。
- [ ] 0.3 检查所有 deferred 项，确保没有 UI 假入口、假成功提示、空实现或未验证即宣称支持的行为。
- [ ] 1.1 确认 MVP 目标为 JetBrains IDE 全家桶通用支持：不得只绑定 IntelliJ IDEA；如有个别 IDE 例外，必须进入兼容例外矩阵并说明原因、影响和替代方案。
- [ ] 1.2 确认 ADB 策略：PATH 优先、Windows bundled fallback，还是必须跨平台内置。
- [ ] 1.3 确认 MVP 是否允许没有 Tool Window，仅使用 Action、Dialog、Notification。

## 2. JetBrains Plugin Skeleton

- [ ] 2.1 创建 Gradle-based IntelliJ Platform 插件项目。
- [ ] 2.2 配置插件元信息、platform module 依赖和兼容 IDE 版本范围，默认面向所有满足依赖的 JetBrains IDE。
- [ ] 2.3 注册 AutoJs6 Action 分组和菜单入口。
- [ ] 2.4 配置建议快捷键，避免与 JetBrains 默认快捷键硬冲突。

## 3. Device Connection Capability

- [ ] 3.1 实现 8 字节 frame codec：length + type，big-endian。
- [ ] 3.2 实现 JSON payload 编解码。
- [ ] 3.3 实现 IDE 监听 6347 端口并接受 AutoJs6 客户端连接。
- [ ] 3.4 实现通过 IP 连接 AutoJs6 服务端 7347 端口。
- [ ] 3.5 实现 hello 握手和最低版本校验。
- [ ] 3.6 实现连接列表和 disconnect all。
- [ ] 3.7 实现 IDE dispose 时释放 socket。

## 4. ADB Connection Capability

- [ ] 4.1 实现 ADB 可执行文件解析：用户配置路径、PATH、Windows bundled fallback。
- [ ] 4.2 实现 `adb devices -l` 解析。
- [ ] 4.3 实现 AutoJs6 服务端端口查询或约定端口连接策略。
- [ ] 4.4 实现 ADB forward 到本地端口后连接设备。

## 5. Script Command Actions

- [ ] 5.1 实现读取当前编辑器文件路径、文件名、文本内容。
- [ ] 5.2 实现 Run Current File，发送 `run` command。
- [ ] 5.3 实现 Save Current File，发送 `save` command。
- [ ] 5.4 实现 Stop Current Script，发送 `stop` command。
- [ ] 5.5 实现 Stop All Scripts，发送 `stopAll` command。
- [ ] 5.6 处理无活动编辑器、无连接设备、未保存文件等错误状态。

## 6. Project Template Capability

- [ ] 6.1 将 `assets/template` 作为插件资源纳入设计。
- [ ] 6.2 实现选择目标目录的 UX。
- [ ] 6.3 复制模板文件并避免覆盖用户已有文件。
- [ ] 6.4 替换 `%PROJECT_NAME_PLACEHOLDER%`。
- [ ] 6.5 替换 `%PACKAGE_SUFFIX_PLACEHOLDER%`，保持与源扩展相同规范。

## 7. Validation

- [ ] 7.1 验证 AutoJs6 客户端模式连接 IDE 监听端口。
- [ ] 7.2 验证 IDE 主动连接 AutoJs6 服务端 IP。
- [ ] 7.3 验证 ADB 转发连接。
- [ ] 7.4 验证 run/save/stop/stopAll 命令在设备侧生效。
- [ ] 7.5 验证新建项目模板内容正确。
- [ ] 7.6 验证 IDE 关闭后 socket 不遗留。

## 8. Deferred Work

- [ ] 8.1 后续提案：设备 Tool Window；MVP 不得用假面板或静态数据冒充真实设备状态。
- [ ] 8.2 后续提案：项目差量 zip 同步；MVP 不得用空 zip、全量误报、假 md5 或假成功替代真实 `bytes_command`。
- [ ] 8.3 后续提案：HTTP 远程命令入口；MVP 不得开放半成品 HTTP `/exec` 或未验证命令分发。
- [ ] 8.4 后续提案：用户自行发布文档、Marketplace/ZIP 发布步骤和 JetBrains 全家桶兼容验证；MVP 不得推测未验证 IDE 为已支持。
