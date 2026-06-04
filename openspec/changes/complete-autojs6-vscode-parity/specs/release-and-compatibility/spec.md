## ADDED Requirements

### Requirement: Define Supported JetBrains IDE Matrix
The plugin SHALL define and test a support matrix covering the JetBrains IDE family and versions for VSCode replacement use, defaulting to every JetBrains IntelliJ Platform IDE that contains the required platform modules.

#### Scenario: Release candidate is prepared
- **WHEN** a release candidate is built
- **THEN** it is validated against the declared IDE/version matrix and any unsupported IDE is listed in an exception matrix with evidence, reason, impact, and workaround

### Requirement: Package Resources And ADB Strategy
The plugin SHALL define how icons, templates, docs, and ADB binaries are packaged across operating systems.

#### Scenario: Plugin runs on Windows
- **WHEN** ADB is not available on PATH and bundled Windows ADB is packaged
- **THEN** the plugin can use the bundled executable as fallback

#### Scenario: Plugin runs on non-Windows OS
- **WHEN** ADB is needed
- **THEN** the plugin uses configured or PATH ADB unless an explicit platform bundle is provided

### Requirement: Maintain Regression Parity Matrix
The plugin SHALL maintain a regression matrix mapping every VSCode parity command and protocol behavior to automated or manual verification steps.

#### Scenario: Parity release is validated
- **WHEN** all parity tasks are claimed complete
- **THEN** every matrix row has passing evidence or a documented accepted difference

### Requirement: Prepare User-Published Release Documentation
The plugin SHALL provide complete release documentation for user-managed publication, including local ZIP distribution, Marketplace publication by the user, signing, Plugin Verifier, versioning, changelog, rollback, and troubleshooting steps.

#### Scenario: User prepares a Marketplace release
- **WHEN** the user wants to publish the plugin personally
- **THEN** the documentation provides step-by-step commands and checklist items for building, verifying, signing, uploading, submitting, and monitoring the release

#### Scenario: User prepares a private ZIP release
- **WHEN** the user wants to distribute the plugin without Marketplace publication
- **THEN** the documentation provides plugin ZIP build, local installation, private sharing, rollback, and compatibility verification steps

### Requirement: Enforce Four Non-Negotiable Rules

The plugin SHALL enforce the four non-negotiable release rules: runtime/protocol compatibility plus built-in-template project scaffolding, preserved user habits with JetBrains best practices, additive feature policy, and no mock/fake/speculative completion.

#### Scenario: Release candidate is audited

- **WHEN** a parity release candidate is prepared
- **THEN** the compatibility ledger demonstrates no runtime/protocol breakage and no existing-project dependency in New Project scaffolding, no disruptive workflow rewrite, no missing claimed feature, and no mock/fake/speculative task completion

### Requirement: Preserve existing project Runtime Compatibility

The plugin SHALL keep existing AutoJs6 projects runnable without incompatible migration, project format conversion, command renaming, path semantic changes, payload field changes, or protocol format changes.

#### Scenario: Existing AutoJs6 project is used

- **WHEN** a user opens an existing AutoJs6 project that works with the VSCode extension
- **THEN** equivalent JetBrains project actions operate with compatible project layout, ignore rules, diff semantics, command names, and device payloads


