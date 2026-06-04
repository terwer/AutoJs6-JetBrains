# device-connection Specification

## Purpose
TBD - created by archiving change add-autojs6-jetbrains-mvp. Update Purpose after archive.
## Requirements
### Requirement: Accept AutoJs6 Client Connections

The plugin SHALL listen for AutoJs6 client-mode devices on the configured IDE listening port, defaulting to `6347`.

#### Scenario: AutoJs6 app connects to IDE

- **WHEN** AutoJs6 opens a TCP connection to the IDE listening port
- **THEN** the plugin accepts the connection and starts protocol handshake

#### Scenario: Listening port is unavailable

- **WHEN** the plugin cannot bind the listening port
- **THEN** the plugin reports the failure to the user and does not leave a partially initialized server socket

### Requirement: Connect To AutoJs6 Server By IP

The plugin SHALL allow the user to connect to an AutoJs6 server-mode device by host/IP, defaulting to port `7347`.

#### Scenario: User enters a reachable AutoJs6 server IP

- **WHEN** the user submits a host/IP and the TCP connection succeeds
- **THEN** the plugin starts protocol handshake and records the host as a recent connection

#### Scenario: User enters an unreachable AutoJs6 server IP

- **WHEN** the TCP connection fails
- **THEN** the plugin shows a connection failure message and does not add the host as a connected device

### Requirement: Connect Through ADB

The plugin SHALL support connecting through ADB by discovering Android devices and forwarding a local TCP port to the AutoJs6 server port.

#### Scenario: ADB device is selected

- **WHEN** the user selects an ADB device
- **THEN** the plugin forwards a local TCP port to the AutoJs6 device server port and connects through the forwarded port

#### Scenario: ADB executable is unavailable

- **WHEN** the plugin cannot find a configured, PATH, or bundled ADB executable
- **THEN** the plugin reports that ADB is unavailable and suggests configuring an ADB path

### Requirement: Perform Hello Handshake

The plugin SHALL implement the AutoJs6 hello handshake and reject unsupported client versions.

#### Scenario: Supported AutoJs6 version connects

- **WHEN** the connected device sends hello data with versionCode greater than or equal to `3591`
- **THEN** the plugin sends its hello response and marks the device as connected

#### Scenario: Unsupported AutoJs6 version connects

- **WHEN** the connected device sends hello data with versionCode lower than `3591`
- **THEN** the plugin sends an error response if possible, closes the connection, and informs the user

### Requirement: Disconnect Devices

The plugin SHALL allow the user to disconnect all connected AutoJs6 devices.

#### Scenario: User invokes Disconnect All

- **WHEN** one or more devices are connected and the user invokes Disconnect All
- **THEN** the plugin closes all device sockets, clears the connected device list, and reports completion

### Requirement: Preserve VSCode-Compatible Device Protocol

The plugin SHALL implement the real AutoJs6 device protocol compatible with the existing VSCode extension and SHALL NOT replace socket, ADB, frame, or handshake behavior with mock or fake success behavior.

#### Scenario: Device protocol is validated

- **WHEN** a supported AutoJs6 device or verified protocol replay sends frames using the VSCode-compatible format
- **THEN** the plugin processes the real 8-byte header, payload type, JSON payload, bytes payload, and hello handshake without incompatible field or encoding changes

#### Scenario: Protocol behavior is not yet verified

- **WHEN** a protocol branch is not implemented or not verified
- **THEN** the plugin reports the capability as unavailable, deferred, blocked, or requiring verification instead of reporting success

