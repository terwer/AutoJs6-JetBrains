## ADDED Requirements

### Requirement: 显示已连接设备
插件 MUST提供 AutoJs6 Tool Window，列出 connected devices、device names、connection type、host 或 ADB identifier，以及 connection status。

#### Scenario: Device connects
- **WHEN** 设备完成 hello handshake
- **THEN** 该设备以 connected status 出现在 AutoJs6 Tool Window 中

### Requirement: 显示设备日志
插件 MUST在 dedicated output console 或 Tool Window log panel 中显示从 AutoJs6 devices 收到的 `log` events。

#### Scenario: Device sends log data
- **WHEN** connected device 发送 log payload
- **THEN** 插件追加带 device attribution 的 log text

### Requirement: 反映断开事件
当设备正常或异常断开时，插件 MUST更新 UI state。

#### Scenario: Device socket closes
- **WHEN** connected device socket 关闭
- **THEN** Tool Window 将该设备标记为 disconnected 或移除该设备，并记录该事件

### Requirement: 提供 Device-Scoped Actions
当选中设备时，Tool Window MUST提供 device-scoped run、save、stop、disconnect 和 diagnostics actions。

#### Scenario: 用户选择一个设备
- **WHEN** 用户在 Tool Window 中选择一个 connected device
- **THEN** device-scoped actions 只作用于该 selected device

### Requirement: 提供状态栏设备显示与切换
插件 MUST在 JetBrains Status Bar 中显示 AutoJs6 当前连接设备状态，并允许用户在多个 connected devices 之间切换 selected device；该能力 MUST保持实时更新，且不得改变 VSCode-compatible all-devices command 语义。

#### Scenario: 多个设备已连接
- **WHEN** 用户点击状态栏 AutoJs6 device widget
- **THEN** 插件显示 connected device 列表
- **AND** 用户选择设备后，该设备成为 selected device
- **AND** Tool Window 与 selected-device actions 使用同一 selected device

#### Scenario: 当前设备断开
- **WHEN** selected device socket closes 或用户断开所有设备
- **THEN** 状态栏实时更新为剩余设备或无设备状态
