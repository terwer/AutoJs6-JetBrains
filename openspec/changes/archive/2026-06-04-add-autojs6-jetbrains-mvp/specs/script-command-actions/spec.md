## ADDED Requirements

### Requirement: Register AutoJs6 Actions

The plugin SHALL register JetBrains actions for the MVP AutoJs6 commands.

#### Scenario: Plugin is installed

- **WHEN** the IDE loads the plugin
- **THEN** actions for View Document, Connect, Disconnect All, Run Current File, Save Current File, Stop Current Script, Stop All Scripts, and New AutoJs6 Project are available from JetBrains action infrastructure

### Requirement: Run Current File

The plugin SHALL send the current editor file to connected AutoJs6 devices using a `run` command.

#### Scenario: Current editor has a file and device is connected

- **WHEN** the user invokes Run Current File
- **THEN** the plugin sends a command payload containing command `run`, file path as `id`, file name as `name`, and editor text as `script`

#### Scenario: No device is connected

- **WHEN** the user invokes Run Current File without connected devices
- **THEN** the plugin reports that no device is connected and does not send a command

#### Scenario: No editor file is active

- **WHEN** the user invokes Run Current File without an active editor file
- **THEN** the plugin reports that an active file is required and does not send a command

### Requirement: Save Current File

The plugin SHALL send the current editor file to connected AutoJs6 devices using a `save` command.

#### Scenario: Current editor has a file and device is connected

- **WHEN** the user invokes Save Current File
- **THEN** the plugin sends a command payload containing command `save`, file path as `id`, file name as `name`, and editor text as `script`

### Requirement: Stop Current Script

The plugin SHALL send a `stop` command for the current editor file.

#### Scenario: Current editor has a file and device is connected

- **WHEN** the user invokes Stop Current Script
- **THEN** the plugin sends a command payload containing command `stop` and the current file path as `id`

### Requirement: Stop All Scripts

The plugin SHALL broadcast a `stopAll` command to connected AutoJs6 devices.

#### Scenario: One or more devices are connected

- **WHEN** the user invokes Stop All Scripts
- **THEN** the plugin sends a command payload containing command `stopAll` to each connected device

### Requirement: Open Online Documentation

The plugin SHALL provide an action to open the AutoJs6 online documentation.

#### Scenario: User invokes View Document

- **WHEN** the user invokes View Document
- **THEN** the plugin opens `https://docs.autojs6.com/` in the system browser

### Requirement: Preserve Existing Script Workflow Habits

The plugin SHALL keep run, save, stop, stopAll, connect, disconnect, document, and project creation workflows recognizable to users of the existing AutoJs6 VSCode extension while exposing them through JetBrains-native actions and UI.

#### Scenario: User invokes a familiar AutoJs6 action

- **WHEN** a user searches JetBrains actions or opens an applicable editor context
- **THEN** the action naming, intent, and command payload behavior match the corresponding existing workflow without requiring a project migration or a new runtime convention

### Requirement: Forbid Mock Command Completion

The plugin SHALL NOT mark script command actions successful unless a real command was sent to a real connected device or a verified protocol replay fixture during tests.

#### Scenario: No real target exists

- **WHEN** no connected device or verified replay target exists
- **THEN** the plugin reports that no target is available and does not show a fake success result
