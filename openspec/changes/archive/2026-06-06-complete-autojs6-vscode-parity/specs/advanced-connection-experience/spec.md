## ADDED Requirements

### Requirement: 显示本地网络连接提示
插件 MUST列出合适的 local IPv4 network interfaces，并引导用户让 AutoJs6 client mode 连接到 IDE listening address。

#### Scenario: 用户选择 client LAN connection
- **WHEN** 存在合适的 local IPv4 interfaces
- **THEN** 插件显示可选 interface addresses 和 connection instructions

### Requirement: 管理最近 server IP 记录
插件 MUST持久化最近使用的 AutoJs6 server IP 或 host records 及其 timestamps，并允许用户清理这些记录。

#### Scenario: LAN server connection 成功
- **WHEN** 插件通过 host 或 IP 连接到 AutoJs6 server
- **THEN** 该 host 被保存或更新为带 timestamp 的 recent record

#### Scenario: 用户清理记录
- **WHEN** 用户确认清理 recent records
- **THEN** 插件移除所有已持久化的 recent connection records

### Requirement: 支持 ADB provider 与 forward 流程
插件 MUST发现 ADB devices，在可用时查询或使用 AutoJs6 debug provider ports，创建所需 TCP forwards，并通过 forwarded local port 建立连接。

#### Scenario: 选择 ADB device
- **WHEN** 用户选择一个 ADB device 用于 AutoJs6 connection
- **THEN** 插件创建所需 port forwards，并尝试通过 loopback 建立连接

### Requirement: 诊断连接失败
插件 MUST为 unavailable ADB、unreachable host、duplicate connection、handshake timeout、unsupported AutoJs6 version 和 occupied local port 提供可操作 diagnostics。

#### Scenario: Host connection 失败
- **WHEN** LAN connection attempt 失败
- **THEN** 插件显示包含下一步建议的 diagnostic message

### Requirement: 支持可选 QR 连接提示
当实现能够安全编码 IDE address 时，插件 MUST为 AutoJs6 client mode 支持 optional QR 或 copyable connection hint。

#### Scenario: 用户请求 QR hint
- **WHEN** 已选择 local interface 且 QR support 已启用
- **THEN** 插件显示可扫描或可复制的 selected address connection hint
