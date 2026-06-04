## ADDED Requirements

### Requirement: Provide HTTP Exec Endpoint
The plugin SHALL provide a configurable HTTP `/exec` endpoint compatible with VSCode extension query parameters `cmd` and `path`.

#### Scenario: Valid exec command arrives
- **WHEN** an HTTP request is received at `/exec` with a supported `cmd`
- **THEN** the plugin dispatches the corresponding AutoJs6 action with the provided path parameter

### Requirement: Protect HTTP Bridge By Default
The plugin SHALL keep the HTTP bridge disabled or loopback-bound by default and SHALL require explicit user configuration for wider network binding.

#### Scenario: Fresh plugin install
- **WHEN** the plugin starts for the first time
- **THEN** the HTTP bridge is not exposed on all network interfaces without explicit user consent

### Requirement: Handle Device Reverse Commands
The plugin SHALL process device-originated command payloads only when the command is in the supported parity action list.

#### Scenario: Device sends supported command
- **WHEN** a connected device sends a command payload with a supported command name
- **THEN** the plugin dispatches that command through the same action path as user invocation

#### Scenario: Device sends unknown command
- **WHEN** a connected device sends a command payload with an unknown command name
- **THEN** the plugin rejects the command and reports an error without executing arbitrary behavior
