## ADDED Requirements

### Requirement: Register All VSCode Parity Actions
The plugin SHALL provide JetBrains actions corresponding to all user-visible VSCode extension commands: viewDocument, connect, disconnectAll, run, runWithoutArguments, commandsHierarchy, runOnDevice, stop, stopAll, rerun, save, saveToDevice, newUntitledFile, newProject, saveProject, saveProjectWithoutArguments, runProject, and runProjectWithoutArguments.

#### Scenario: Plugin actions are loaded
- **WHEN** the IDE loads the plugin
- **THEN** every VSCode parity command is available through JetBrains action infrastructure

### Requirement: Preserve Contextual Entry Points
The plugin SHALL expose script actions from editor and Project View contexts and project actions from folder/project contexts.

#### Scenario: JavaScript file context is opened
- **WHEN** the user opens the context menu for a JavaScript file
- **THEN** run, runOnDevice, stop, stopAll, rerun, saveToDevice, and save actions are available

#### Scenario: Folder context is opened
- **WHEN** the user opens the context menu for a folder or AutoJs6 project
- **THEN** runProject, saveProject, and newProject actions are available

### Requirement: Support Suggested Keymap Parity
The plugin SHALL register suggested keybindings or keymap metadata matching the VSCode extension intent while allowing JetBrains users to override conflicts.

#### Scenario: User searches keymap
- **WHEN** the user searches for AutoJs6 actions in Keymap settings
- **THEN** the actions show recommended shortcuts equivalent to the VSCode extension shortcuts where possible

### Requirement: Rerun Current Script
The plugin SHALL implement rerun by stopping the current script and then running the current script again.

#### Scenario: User invokes rerun
- **WHEN** a current editor file and device connection exist and the user invokes rerun
- **THEN** the plugin sends stop for the current file and then sends run for the same file

### Requirement: Create New Untitled Script
The plugin SHALL provide an action that creates a new unsaved editor document for quick AutoJs6 script drafting.

#### Scenario: User invokes newUntitledFile
- **WHEN** the user invokes New Untitled File
- **THEN** the IDE opens a new unsaved editor document ready for script editing
