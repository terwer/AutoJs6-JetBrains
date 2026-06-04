## ADDED Requirements

### Requirement: Show Connected Devices
The plugin SHALL provide an AutoJs6 Tool Window listing connected devices, device names, connection type, host or ADB identifier, and connection status.

#### Scenario: Device connects
- **WHEN** a device completes hello handshake
- **THEN** the device appears in the AutoJs6 Tool Window with connected status

### Requirement: Show Device Logs
The plugin SHALL display `log` events received from AutoJs6 devices in a dedicated output console or Tool Window log panel.

#### Scenario: Device sends log data
- **WHEN** a connected device sends a log payload
- **THEN** the plugin appends the log text with device attribution

### Requirement: Reflect Disconnect Events
The plugin SHALL update UI state when a device disconnects normally or unexpectedly.

#### Scenario: Device socket closes
- **WHEN** a connected device socket closes
- **THEN** the Tool Window marks the device disconnected or removes it and records the event

### Requirement: Provide Device-Scoped Actions
The Tool Window SHALL provide device-scoped run, save, stop, disconnect, and diagnostics actions when a device is selected.

#### Scenario: User selects one device
- **WHEN** the user selects a connected device in the Tool Window
- **THEN** device-scoped actions target only that selected device
