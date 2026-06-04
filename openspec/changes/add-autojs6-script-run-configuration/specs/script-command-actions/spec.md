## MODIFIED Requirements

### Requirement: Run Current File

The plugin SHALL send the current editor file to connected AutoJs6 devices using a `run` command. JetBrains Action-based Run Current File and AutoJs6 Script Run Configuration SHALL use the same single-file command payload semantics and SHALL NOT introduce separate single-file run protocols.

#### Scenario: Current editor has a file and device is connected

- **WHEN** the user invokes Run Current File
- **THEN** the plugin sends a command payload containing command `run`, file path as `id`, file name as `name`, and editor text as `script`

#### Scenario: AutoJs6 Script Run Configuration has a file and device is connected

- **WHEN** the user runs an AutoJs6 Script Run Configuration
- **THEN** the plugin sends a command payload containing command `run`, configured file path as `id`, configured file name as `name`, and configured file text as `script`
- **AND** the payload shape matches Run Current File

#### Scenario: No device is connected

- **WHEN** the user invokes Run Current File without connected devices
- **THEN** the plugin reports that no device is connected and does not send a command

#### Scenario: No editor file is active

- **WHEN** the user invokes Run Current File without an active editor file
- **THEN** the plugin reports that an active file is required and does not send a command
