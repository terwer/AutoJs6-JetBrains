## ADDED Requirements

### Requirement: Show Local Network Connection Hint
The plugin SHALL list suitable local IPv4 network interfaces and guide the user to connect AutoJs6 client mode to the IDE listening address.

#### Scenario: User chooses client LAN connection
- **WHEN** suitable local IPv4 interfaces exist
- **THEN** the plugin shows selectable interface addresses and connection instructions

### Requirement: Manage Recent Server IP Records
The plugin SHALL persist recent AutoJs6 server IP or host records with timestamps and SHALL allow users to clear the records.

#### Scenario: Successful LAN server connection
- **WHEN** the plugin connects to an AutoJs6 server by host or IP
- **THEN** the host is saved or updated as a recent record with timestamp

#### Scenario: User clears records
- **WHEN** the user confirms clearing recent records
- **THEN** the plugin removes all persisted recent connection records

### Requirement: Support ADB Provider And Forward Flow
The plugin SHALL discover ADB devices, query or use AutoJs6 debug provider ports where available, create required TCP forwards, and connect through the forwarded local port.

#### Scenario: ADB device is selected
- **WHEN** the user selects an ADB device for AutoJs6 connection
- **THEN** the plugin creates the required port forwards and attempts connection through loopback

### Requirement: Diagnose Connection Failures
The plugin SHALL provide actionable diagnostics for unavailable ADB, unreachable host, duplicate connection, handshake timeout, unsupported AutoJs6 version, and occupied local port.

#### Scenario: Host connection fails
- **WHEN** a LAN connection attempt fails
- **THEN** the plugin displays a diagnostic message with next-step suggestions

### Requirement: Support Optional QR Connection Hint
The plugin SHALL support an optional QR or copyable connection hint for AutoJs6 client mode when the implementation can encode the IDE address safely.

#### Scenario: User requests QR hint
- **WHEN** a local interface is selected and QR support is enabled
- **THEN** the plugin displays a scannable or copyable connection hint for the selected address
