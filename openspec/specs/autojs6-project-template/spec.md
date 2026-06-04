# autojs6-project-template Specification

## Purpose
TBD - created by archiving change add-autojs6-jetbrains-mvp. Update Purpose after archive.
## Requirements
### Requirement: Create AutoJs6 Project From Template

The plugin SHALL create a new AutoJs6 project by copying an internal project template into a user-selected directory.

#### Scenario: User selects an empty or suitable target directory

- **WHEN** the user invokes New AutoJs6 Project and selects a target directory
- **THEN** the plugin copies template files into that directory without overwriting existing user files unexpectedly

### Requirement: Replace Project Name Placeholder

The plugin SHALL replace `%PROJECT_NAME_PLACEHOLDER%` in generated template files with the selected project directory name.

#### Scenario: Template contains project name placeholder

- **WHEN** the project is generated
- **THEN** all `%PROJECT_NAME_PLACEHOLDER%` occurrences are replaced with the project name

### Requirement: Replace Package Suffix Placeholder

The plugin SHALL replace `%PACKAGE_SUFFIX_PLACEHOLDER%` using the same normalization intent as the VSCode extension.

#### Scenario: Project name contains non-package characters

- **WHEN** the project is generated from a name containing spaces, punctuation, leading digits, or Chinese characters
- **THEN** the package suffix is normalized to a lower-case package-safe value compatible with AutoJs6 expectations

### Requirement: Avoid Accidental Overwrite

The plugin SHALL avoid overwriting existing files in the target directory unless the user explicitly confirms overwriting.

#### Scenario: Target file already exists

- **WHEN** a template file would overwrite an existing file
- **THEN** the plugin skips the file or asks for explicit confirmation before overwriting

### Requirement: Preserve Historical Template Runtime Compatibility

The plugin SHALL generate AutoJs6 projects whose layout, placeholder replacement, package suffix normalization intent, and runtime expectations remain compatible with existing AutoJs6 projects created by the VSCode extension.

#### Scenario: User opens generated project in AutoJs6 workflow

- **WHEN** a project is generated from the JetBrains template
- **THEN** its files and metadata remain compatible with the historical AutoJs6 project runtime and do not require an incompatible migration step

