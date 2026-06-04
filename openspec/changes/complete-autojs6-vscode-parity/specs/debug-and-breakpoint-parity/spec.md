## ADDED Requirements

### Requirement: Preserve JavaScript Breakpoint Discoverability
The plugin SHALL document and expose how JavaScript breakpoints relate to AutoJs6 script workflows in JetBrains IDEs.

#### Scenario: User opens JavaScript file
- **WHEN** a JavaScript file is open in the editor
- **THEN** AutoJs6 documentation or help explains whether IDE breakpoints are supported, ignored, or require a future debug adapter

### Requirement: Define Debug Boundary
The plugin SHALL distinguish run/save command execution from full debugger attachment and SHALL not imply unsupported breakpoint stepping behavior.

#### Scenario: User invokes debug-related help
- **WHEN** the user asks for AutoJs6 debug capability from plugin UI
- **THEN** the plugin shows the current supported debug boundary and next steps

### Requirement: Prepare Future Debug Extension Point
The plugin SHALL keep protocol and action architecture extensible for a future AutoJs6 debug adapter or debugger integration.

#### Scenario: Debug protocol is added later
- **WHEN** a future change adds debugger support
- **THEN** it can reuse existing device connection and command dispatch services without rewriting action parity
