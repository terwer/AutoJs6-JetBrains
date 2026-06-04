# autojs6-project-template Specification

## ADDED Requirements

### Requirement: Create New AutoJs6 Project From Plugin Template

The plugin SHALL create a brand-new AutoJs6 project from the plugin-bundled AutoJs6 template. This workflow is not an existing-project copy, migration, import, or sync operation.

#### Scenario: User enters project name and selects parent directory

- **WHEN** the user invokes New AutoJs6 Project, selects a target project directory
- **THEN** the plugin uses the selected directory as the project root
- **AND** the plugin copies bundled template files directly into the selected directory
- **AND** the plugin reports the created path to the user

### Requirement: Replace Project Name Placeholder

The plugin SHALL replace `%PROJECT_NAME_PLACEHOLDER%` in generated template files with the selected project directory name.

#### Scenario: Template contains project name placeholder

- **WHEN** the project is generated
- **THEN** all `%PROJECT_NAME_PLACEHOLDER%` occurrences are replaced with the new project name

### Requirement: Replace Package Suffix Placeholder

The plugin SHALL replace `%PACKAGE_SUFFIX_PLACEHOLDER%` with a lower-case ASCII package-safe suffix derived from the new project name.

#### Scenario: Project name contains non-package characters

- **WHEN** the project name contains spaces, punctuation, leading digits, or non-ASCII characters
- **THEN** the package suffix is normalized to a lower-case package-safe value
- **AND** a leading digit is prefixed with `app_`

### Requirement: Avoid Accidental Overwrite

The plugin SHALL avoid overwriting existing files in the generated target directory unless overwrite behavior is explicitly requested by implementation or future UX.

#### Scenario: Target file already exists

- **WHEN** a template file would overwrite an existing file
- **THEN** the plugin skips the existing file by default

### Requirement: No existing project Dependency

The plugin SHALL NOT require an existing AutoJs6 project to create a new project.

#### Scenario: User creates a new project from an empty parent directory

- **WHEN** no existing AutoJs6 project is present
- **THEN** the plugin still creates a complete new project from the bundled template



