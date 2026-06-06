## ADDED Requirements

### Requirement: 定义 Supported JetBrains IDE Matrix
插件 MUST定义并测试 support matrix，覆盖用于 VSCode replacement 的 JetBrains IDE family 和 versions；默认目标是每个包含所需 platform modules 的 JetBrains IntelliJ Platform IDE。

#### Scenario: Release candidate is prepared
- **WHEN** 准备 release candidate
- **THEN** 它已针对 declared IDE/version matrix 验证，且任何 unsupported IDE 都进入 exception matrix，并附 evidence、reason、impact 和 workaround

### Requirement: 打包资源与 ADB Strategy
插件 MUST定义 icons、templates、docs 和 ADB binaries 在各 operating systems 下的打包方式。

#### Scenario: Plugin runs on Windows
- **WHEN** ADB 不在 PATH 中且 bundled Windows ADB 已打包
- **THEN** 插件可使用 bundled executable 作为 fallback

#### Scenario: Plugin runs on non-Windows OS
- **WHEN** 需要 ADB
- **THEN** 插件使用 configured 或 PATH ADB，除非提供明确的 platform bundle

### Requirement: 维护 Regression Parity Matrix
插件 MUST维护 regression matrix，将每个 VSCode parity command 和 protocol behavior 映射到 automated 或 manual verification steps。

#### Scenario: Parity release is validated
- **WHEN** 声称所有 parity tasks 已完成
- **THEN** 每个 matrix row 都有 passing evidence 或 documented accepted difference

### Requirement: 准备用户自行发布文档
插件 MUST提供完整的 user-managed publication 文档，包括 local ZIP distribution、Marketplace publication by the user、signing、Plugin Verifier、versioning、changelog、rollback 和 troubleshooting steps。

#### Scenario: 用户准备 Marketplace release
- **WHEN** 用户想亲自发布插件
- **THEN** 文档提供 build、verify、sign、upload、submit 和 monitor release 的逐步命令与 checklist items

#### Scenario: 用户准备 private ZIP release
- **WHEN** 用户想在不通过 Marketplace 的情况下分发插件
- **THEN** 文档提供 plugin ZIP build、local installation、private sharing、rollback 和 compatibility verification steps

### Requirement: 执行四条不可妥协规则
插件 MUST执行四条 non-negotiable release rules：runtime/protocol compatibility plus built-in-template project scaffolding、preserved user habits with JetBrains best practices、additive feature policy，以及 no mock/fake/speculative completion。

#### Scenario: Release candidate is audited
- **WHEN** 准备 parity release candidate
- **THEN** compatibility ledger 证明没有 runtime/protocol breakage、New Project scaffolding 不依赖 existing-project、没有 disruptive workflow rewrite、没有 missing claimed feature，且没有 mock/fake/speculative task completion

### Requirement: 保留 existing project Runtime Compatibility
插件 MUST让 existing AutoJs6 projects 在无需 incompatible migration、project format conversion、command renaming、path semantic changes、payload field changes 或 protocol format changes 的情况下继续可运行。

#### Scenario: 使用 existing AutoJs6 project
- **WHEN** 用户打开一个可配合 VSCode 扩展工作的 existing AutoJs6 project
- **THEN** 等价 JetBrains project actions 使用兼容的 project layout、ignore rules、diff semantics、command names 和 device payloads 运行
