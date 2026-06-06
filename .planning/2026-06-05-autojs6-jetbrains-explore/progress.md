# 进度日志

## 会话：2026-06-05

### 阶段 1：需求与发现
- **状态：** in_progress
- **开始时间：** 2026-06-05 Asia/Shanghai
- 执行的操作：
  - 读取 `planning-with-files-zh` 技能说明。
  - 创建目标目录 `D:\Users\Administrator\Documents\myproject\AutoJs6-JetBrains`。
  - 创建规划目录 `.planning/2026-06-05-autojs6-jetbrains-explore/`。
  - 写入 `.planning/.active_plan`、`task_plan.md`、`findings.md`、`progress.md`。
- 创建/修改的文件：
  - `.planning/.active_plan`
  - `.planning/2026-06-05-autojs6-jetbrains-explore/task_plan.md`
  - `.planning/2026-06-05-autojs6-jetbrains-explore/findings.md`
  - `.planning/2026-06-05-autojs6-jetbrains-explore/progress.md`

## 测试结果
| 测试 | 输入 | 预期结果 | 实际结果 | 状态 |
|------|------|---------|---------|------|
| 规划目录创建 | 目标路径 | 出现 `.planning` 和三份计划文件 | 已创建 | 通过 |

## 错误日志
| 时间戳 | 错误 | 尝试次数 | 解决方案 |
|--------|------|---------|---------|
| 2026-06-05 | `New-Item -LiteralPath` 参数不可用 | 1 | 改用 `New-Item -Path` |

## 五问重启检查
| 问题 | 答案 |
|------|------|
| 我在哪里？ | 阶段 1：需求与发现 |
| 我要去哪里？ | 探索源 VSCode 扩展并形成 JetBrains 迁移计划 |
| 目标是什么？ | 不写业务代码，仅探索并规划 IDEA/JetBrains 插件版本 |
| 我学到了什么？ | 见 findings.md |
| 我做了什么？ | 见上方记录 |

---
*每个阶段完成后或遇到错误时更新此文件*

### 阶段 1 增量：源项目初扫
- **状态：** in_progress
- 执行的操作：
  - 读取活跃计划文件以恢复目标。
  - 列出源项目 `src/` 文件。
  - 解析 `package.json` 的基本字段和命令贡献点。
  - 使用 `rg` 检索网络、ADB、命令注册和 AutoJs6 相关入口。
- 发现已写入 `findings.md`。

### 阶段 1 增量：贡献点与核心代码切片
- **状态：** in_progress
- 执行的操作：
  - 用 Node 解析 `package.json` 的 menus/keybindings/languages/grammars/configuration。
  - 查看 `extension.ts`、`device.ts` 关键区间：构造、命令注册、用户命令、激活入口、设备协议。
- 发现已写入 `findings.md`。

### 阶段 1/2 增量：资源、同步协议和功能映射
- **状态：** in_progress
- 执行的操作：
  - 查看 `assets/`、`tools/` 资源。
  - 查看 `project.ts` 与 `diff.ts` 的项目模板、项目配置、差量 zip 同步逻辑。
  - 检索协议端口、帧头、AutoJs6 最低版本、`sendProjectCommand`。
  - 查阅 JetBrains 官方 SDK 文档用于高层技术映射。
- 发现和映射草案已写入 `findings.md`。

### 阶段状态更新
- 阶段 1 已完成：源项目结构、清单、命令、配置、源码入口已初步检查。
- 阶段 2 已完成：已形成 VSCode → JetBrains 功能映射与风险草案。
- 阶段 3 进入进行中：已给出技术栈/MVP 草案，尚未细化依赖和构建策略；未开始写业务代码。

### Opspec 提案生成
- **状态：** complete
- 执行的操作：
  - 评估当前探索信息是否足够进入 MVP 提案阶段。
  - 生成 `opspec_proposal.md`，仅包含规划/需求/架构/验收标准，不包含业务代码。
  - 更新 `task_plan.md` 阶段状态。
- 创建/修改的文件：
  - `.planning/2026-06-05-autojs6-jetbrains-explore/opspec_proposal.md`
  - `.planning/2026-06-05-autojs6-jetbrains-explore/task_plan.md`
  - `.planning/2026-06-05-autojs6-jetbrains-explore/progress.md`

### OpenSpec 正式提案生成
- **状态：** complete
- 执行的操作：
  - 确认 `openspec` CLI 已安装。
  - 确认项目已有 `openspec/`、`openspec/changes/`、`openspec/specs/`、`openspec/config.yaml`。
  - 使用 `openspec new change add-autojs6-jetbrains-mvp` 创建正式 OpenSpec change。
  - 生成 `proposal.md`、`design.md`、`tasks.md`。
  - 生成三个 capability delta spec：`device-connection`、`script-command-actions`、`autojs6-project-template`。
  - 第一次 validate 失败：部分 Requirement 文本使用 SHOULD，不符合 OpenSpec 校验要求。
  - 修改为 SHALL 后重新执行 `openspec validate add-autojs6-jetbrains-mvp`，结果通过。
- 创建/修改的 OpenSpec 文件：
  - `openspec/changes/add-autojs6-jetbrains-mvp/.openspec.yaml`
  - `openspec/changes/add-autojs6-jetbrains-mvp/proposal.md`
  - `openspec/changes/add-autojs6-jetbrains-mvp/design.md`
  - `openspec/changes/add-autojs6-jetbrains-mvp/tasks.md`
  - `openspec/changes/add-autojs6-jetbrains-mvp/specs/device-connection/spec.md`
  - `openspec/changes/add-autojs6-jetbrains-mvp/specs/script-command-actions/spec.md`
  - `openspec/changes/add-autojs6-jetbrains-mvp/specs/autojs6-project-template/spec.md`

### 本轮审视 add-autojs6-jetbrains-mvp
- **状态：** in_progress
- 执行的操作：
  - 读取现有 OpenSpec MVP 提案、design、tasks 和 delta specs。
  - 重新解析源 VSCode 扩展的 `package.json` 贡献点。
  - 抽取 `extension.ts`、`device.ts`、`project.ts`、`diff.ts`、`adb.ts` 关键功能证据。
- 初步结论：MVP 对“第一版可运行”合理，但距离“100% 替代 VSCode 插件 + 自有增强能力”差距较大，应新建 post-MVP 全面对齐提案。

### OpenSpec post-MVP 全面对齐提案生成
- **状态：** complete
- 执行的操作：
  - 新建 `openspec/changes/complete-autojs6-vscode-parity`。
  - 写入 post-MVP 提案，目标为 MVP 后 100% 替代 VSCode 插件。
  - 写入设计文档，明确 parity 矩阵、协议 core、项目同步、Tool Window、HTTP 安全和发布/兼容边界。
  - 写入 8 个 capability delta specs：
    - `vscode-parity-actions`
    - `project-diff-sync`
    - `device-tool-window-and-logs`
    - `advanced-connection-experience`
    - `remote-command-bridge`
    - `debug-and-breakpoint-parity`
    - `release-and-compatibility`
  - 写入 `tasks.md`，共 9 个任务组、61 项可追踪任务。
  - 执行 `openspec validate complete-autojs6-vscode-parity`，结果通过。
- 结论：`add-autojs6-jetbrains-mvp` 适合作为第一阶段，但不是 100% 替代方案；新的 `complete-autojs6-vscode-parity` 用于 MVP 后全面追齐和增强。


### 阶段 7：四规则固化
- **状态：** complete
- 执行的操作：
  - 读取并审视 `add-autojs6-jetbrains-mvp` 与 `complete-autojs6-vscode-parity` 的 proposal/design/tasks/specs。
  - 在两份 proposal/design/tasks 中加入不可违背规则、compatibility ledger、no-mock gate 和 release audit。
  - 在 specs 中追加约束：真实协议兼容、真实命令完成、历史模板兼容、项目同步历史兼容、禁止假项目同步成功、release 四规则门禁。
  - 执行 OpenSpec 校验。
- 创建/修改的文件：
  - `openspec/changes/add-autojs6-jetbrains-mvp/proposal.md`
  - `openspec/changes/add-autojs6-jetbrains-mvp/design.md`
  - `openspec/changes/add-autojs6-jetbrains-mvp/tasks.md`
  - `openspec/changes/add-autojs6-jetbrains-mvp/specs/device-connection/spec.md`
  - `openspec/changes/add-autojs6-jetbrains-mvp/specs/script-command-actions/spec.md`
  - `openspec/changes/add-autojs6-jetbrains-mvp/specs/autojs6-project-template/spec.md`
  - `openspec/changes/complete-autojs6-vscode-parity/proposal.md`
  - `openspec/changes/complete-autojs6-vscode-parity/design.md`
  - `openspec/changes/complete-autojs6-vscode-parity/tasks.md`
  - `openspec/changes/complete-autojs6-vscode-parity/specs/release-and-compatibility/spec.md`
  - `openspec/changes/complete-autojs6-vscode-parity/specs/project-diff-sync/spec.md`
- 测试结果：
  - `openspec validate add-autojs6-jetbrains-mvp`：通过。
  - `openspec validate complete-autojs6-vscode-parity`：通过。

### 阶段 8：发布与兼容目标落盘
- **状态：** complete
- 执行的操作：
  - 将 parity 提案范围统一为 VSCode full parity。
  - 移除非 parity capability spec 及空目录。
  - 新增 `openspec/changes/complete-autojs6-vscode-parity/release-guide.md`。
  - 修改 MVP 与 parity 的 IDE 兼容策略：默认 JetBrains 全家桶通用支持，个别例外必须列矩阵。
  - 修改 release-and-compatibility spec，要求用户自行发布文档和完整步骤。
- 测试结果：
  - `openspec validate add-autojs6-jetbrains-mvp`：通过。
  - `openspec validate complete-autojs6-vscode-parity`：通过。

### 阶段 9：范围表述清理
- **状态：** complete
- 执行的操作：
  - 移除 OpenSpec 与规划文件中可能导致后续实现误读的历史范围说明。
  - 将对应任务改为“只保留 VSCode full parity 与批准的 JetBrains-native 展示/诊断辅助”。
  - 执行全局关键词扫描，确认无残留混淆词。
- 测试结果：
  - `openspec validate add-autojs6-jetbrains-mvp`：通过。
  - `openspec validate complete-autojs6-vscode-parity`：通过。

### 阶段 10：测试失败修复启动
- **状态：** in_progress
- 执行的操作：
  - 恢复活跃规划上下文。
  - 新增阶段 10，用于处理 `docs/error.txt` 中记录的测试失败。
- 创建/修改的文件：
  - `.planning/2026-06-05-autojs6-jetbrains-explore/task_plan.md`
  - `.planning/2026-06-05-autojs6-jetbrains-explore/progress.md`

### 阶段 10 增量：读取失败日志
- **状态：** in_progress
- 执行的操作：
  - 读取 `docs/error.txt`。
  - 识别到与本插件直接相关的异常：`ConfigurationTypeUtil.findConfigurationType` 找不到 `AutoJs6ScriptConfigurationType`，触发点在 run configuration producer。
- 发现已写入 `findings.md`。

### 阶段 10 增量：定位注册点根因
- **状态：** in_progress
- 执行的操作：
  - 检查 `plugin.xml`、run configuration type 与 producer 源码。
  - 搜索本地 IntelliJ 2024.2 bundled 插件描述符，确认运行配置类型扩展点标签应为 `configurationType`。
- 结论：准备将 `runConfigurationType` 改为 `configurationType`，保留 `runConfigurationProducer`。

### 阶段 10 增量：最小修复已编辑
- **状态：** in_progress
- 执行的操作：
  - 将 `src/main/resources/META-INF/plugin.xml` 中的 run configuration type 注册标签从 `runConfigurationType` 改为 IntelliJ Platform 使用的 `configurationType`。
  - 在 `src/test/kotlin/org/autojs/autojs6/jetbrains/MvpUnitTest.kt` 添加回归测试，校验 `plugin.xml` 使用正确扩展点且仍注册 producer。
- 创建/修改的文件：
  - `src/main/resources/META-INF/plugin.xml`
  - `src/test/kotlin/org/autojs/autojs6/jetbrains/MvpUnitTest.kt`

### 阶段 10 增量：清理验证配置警告
- **状态：** in_progress
- 执行的操作：
  - `test` 第二次使用 `--no-daemon --console=plain --rerun-tasks` 成功通过。
  - `verifyPluginProjectConfiguration` 暴露两项配置警告：`IC 2024.2` 需要 Java 21 sourceCompatibility；Kotlin stdlib 默认依赖可能与 IDE 自带版本冲突。
  - 将 `build.gradle.kts` 的 Kotlin toolchain 从 17 调整为 21。
  - 在 `gradle.properties` 增加 `kotlin.stdlib.default.dependency=false`。
- 创建/修改的文件：
  - `build.gradle.kts`
  - `gradle.properties`

### 阶段 10 增量：处理 GradleJvmSupportMatrix sandbox 污染
- **状态：** in_progress
- 执行的操作：
  - 反查本地 IntelliJ `gradle.jar`，确认 Gradle 兼容矩阵由 registry key `gradle.compatibility.config.url` / `gradle.compatibility.update.interval` 控制。
  - 在 `build.gradle.kts` 中给 `runIde` 与 `buildSearchableOptions` JavaExec 任务设置：`gradle.compatibility.config.url=`、`gradle.compatibility.update.interval=0`。
  - 删除已污染的 sandbox cache：`build/idea-sandbox/IC-2024.2/config/app-internal-state.db`（删除前验证路径在工作区内）。
- 创建/修改的文件：
  - `build.gradle.kts`
- 清理的生成物：
  - `build/idea-sandbox/IC-2024.2/config/app-internal-state.db`

### 阶段 10 完成：测试与打包验证通过
- **状态：** complete
- 修复摘要：
  - `AutoJs6ScriptConfigurationType` 注册标签改为 `configurationType`，解决 `ConfigurationTypeUtil.findConfigurationType` 找不到类型导致的 AutoJs6 blame 异常。
  - 添加 `pluginXmlRegistersRunConfigurationTypeWithPlatformExtensionPoint` 回归测试。
  - 构建配置调整为 IntelliJ 2024.2 要求的 Java 21 toolchain，并禁用 Kotlin stdlib 默认依赖，消除 `verifyPluginProjectConfiguration` 的项目配置警告。
  - 禁用 sandbox/headless Gradle compatibility 远程矩阵更新，清理已污染的 `app-internal-state.db`；禁用 searchable options 生成，避免无配置页插件触发 IDE headless KeymapManager 严重日志。
- 验证结果：
  - `./gradlew.bat --no-daemon --console=plain test --rerun-tasks`：通过。
  - `./gradlew.bat --no-daemon --console=plain buildPlugin`：通过，`buildSearchableOptions SKIPPED`。
  - `./gradlew.bat --no-daemon --console=plain test`：通过/UP-TO-DATE。
  - `./gradlew.bat --no-daemon --console=plain check`：通过。
- 仍有非阻断提示：
  - IntelliJ platform 解包阶段输出 `emojipicker.jar` classPath 警告。
  - Gradle 9.3 输出 Gradle 10 兼容性弃用提示。
  - `AutoJs6Actions.kt` 使用已 deprecated 的 `Messages.showChooseDialog`，当前只是编译警告，未阻塞本轮测试。

### 阶段 10 补充验证
- **状态：** complete
- 执行的操作：
  - 验证 sandbox 插件 jar：`build/idea-sandbox/IC-2024.2/plugins/AutoJs6-JetBrains/lib/AutoJs6-JetBrains-0.1.0.jar` 内的 `META-INF/plugin.xml` 已包含 `configurationType` 与 `runConfigurationProducer`。
  - 第一次直接查 `build/idea-sandbox/IC-2024.2/plugins/AutoJs6-JetBrains/META-INF/plugin.xml` 失败，因为 sandbox 中插件描述符被打入 `lib/*.jar`，不是展开在插件根目录；随后改为检查 jar 内容。
  - `git diff --check` 第一次发现规划文件 EOF 多余空行；已修正后复跑通过（仅剩 CRLF 提示）。

### 阶段 10 IDE 构建验证
- **状态：** complete
- 执行的操作：
  - 使用 JetBrains IDE build_project 对 `D:\Users\Administrator\Documents\myproject\AutoJs6-JetBrains` 执行构建验证。
- 验证结果：
  - `build_project`：通过，`problems=[]`。

### 阶段 11：complete-autojs6-vscode-parity 基线任务启动
- **状态：** complete
- 执行的操作：
  - 使用 OpenSpec apply 流程读取 `complete-autojs6-vscode-parity` 的 proposal、design、tasks 和 specs。
  - 新增 `docs/vscode-parity-matrix.md`，从 VSCode `package.json` 建立 18 个 command 的 parity matrix，并记录 HTTP/keymap/Run Configuration/New Project accepted differences。
  - 扩展 `docs/compatibility-ledger.md`，加入 full-parity 四规则、compatibility ledger 要求和 no-mock gate。
  - 新增 `src/test/resources/protocol-fixtures/` 协议 replay fixtures，覆盖 JSON frame、bytes frame、hello、command、log、bytes_command、device reverse command。
  - 新增 `protocolReplayFixturesAreValidJson` 测试，第一次编译因 Kotlin Stream lambda 中 `jsonFiles += it` 失败，改为 `jsonFiles.add(it)` 后通过。
  - 勾选 `openspec/changes/complete-autojs6-vscode-parity/tasks.md` 中 0.1～1.4。
- 创建/修改的文件：
  - `docs/vscode-parity-matrix.md`
  - `docs/compatibility-ledger.md`
  - `src/test/resources/protocol-fixtures/README.md`
  - `src/test/resources/protocol-fixtures/*.json`
  - `src/test/kotlin/org/autojs/autojs6/jetbrains/MvpUnitTest.kt`
  - `openspec/changes/complete-autojs6-vscode-parity/tasks.md`
  - `.planning/2026-06-05-autojs6-jetbrains-explore/task_plan.md`
  - `.planning/2026-06-05-autojs6-jetbrains-explore/progress.md`
- 验证结果：
  - `openspec validate complete-autojs6-vscode-parity --strict`：通过。
  - `openspec validate add-autojs6-script-run-configuration --strict`：通过。
  - `./gradlew.bat --no-daemon --console=plain test`：通过。

### 阶段 12：人工测试反馈深度审计
- **状态：** complete
- **时间：** 2026-06-06 11:39-11:55 Asia/Shanghai
- 执行的操作：
  - 恢复活跃规划上下文与 git 状态；初始业务代码工作区干净。
  - 审计 `plugin.xml`、`AutoJs6Actions.kt`、`AutoJs6CommandDispatcher.kt`、`AutoJs6ProjectSyncService.kt`、`AutoJs6ConnectionService.kt`、Run Configuration 相关类。
  - 对照源 VSCode `package.json`、`device.ts`、`extension.ts`、`project.ts`、`diff.ts` 的项目运行/保存与菜单入口。
  - 更新 `openspec/changes/complete-autojs6-vscode-parity/tasks.md`：重新打开证据不足或被人工测试推翻的任务，并新增 2026-06-06 阻断项。
  - 更新 `docs/vscode-parity-matrix.md` 与 `docs/compatibility-ledger.md`：记录 Toolbar 缺口、CommandsHierarchy 端到端缺口、Project Run Configuration 缺口。
  - 更新 `task_plan.md` 阶段 12 为 complete。
- 验证结果：
  - `openspec validate complete-autojs6-vscode-parity --strict`：通过。
- 结论：用户反馈基本正确，`complete-autojs6-vscode-parity` 不能归档。
- 错误日志：
  - 第一次 PowerShell `ShowRange` 函数输出 `$path:$start` 因冒号解析失败；改用 `-f` 格式化字符串后继续。
  - 第一次尝试在 PowerShell 使用 Bash 风格 `python - <<'PY'` 失败；改为 PowerShell here-string pipe 给 Python。
  - 第一次用 PowerShell 双引号 here-string 记录 Markdown 代码反引号，导致 `` `e`` / `` `b`` 被解释成控制字符；已用 `git show HEAD:<path>` 重建规划文件并重新追加干净记录。

### 阶段 13：实现人工审计阻断项
- **状态：** complete
- **时间：** 2026-06-06 12:00-12:15 Asia/Shanghai
- 执行的操作：
  - 使用 `openspec-apply-change` 读取 `complete-autojs6-vscode-parity` status、apply instructions 和全部 context files。
  - 修改 `plugin.xml`，注册 `AutoJs6ProjectConfigurationType` / `AutoJs6ProjectConfigurationProducer`，并将 `RunProject` / `SaveProject` 加入 Toolbar group。
  - 新增项目 Run Configuration 相关代码：`AutoJs6ProjectConfigurationType`、`AutoJs6ProjectRunConfiguration`、`AutoJs6ProjectConfigurationProducer`、`AutoJs6ProjectSettingsEditor`、`AutoJs6ProjectRunProfileState`、`AutoJs6ProjectConfigurationSerializer`。
  - 调整 `AutoJs6ProjectSyncService`，抽出同步可测的 `sendProjectCommand`，供后台 action 与项目 Run Configuration 复用。
  - 更新 `AutoJs6ScriptSettingsEditor` 文案，指向新的 `AutoJs6 Project` Run Configuration。
  - 增加 `MvpUnitTest` 覆盖项目配置序列化、Project Run Configuration 注册、Toolbar project actions、project bytes frame + `bytes_command` run/save replay、HTTP rerunProject gate。
  - 新增 OpenSpec delta `openspec/changes/complete-autojs6-vscode-parity/specs/script-run-configuration/spec.md`，移除 deferred 项并新增 project run configuration 要求。
  - 更新 `docs/vscode-parity-matrix.md`、`docs/compatibility-ledger.md`、`docs/usage-and-testing.md` 与 `tasks.md`；OpenSpec apply progress 达到 68/68。
- 验证结果：
  - 第一次 `test --rerun-tasks` 120s 超时，无有效输出；改用 300s 超时继续。
  - 第二次编译失败，根因为 `AutoJs6ProjectSyncResult` 插入位置打断 `commandData()` 结束括号；已修复。
  - `./gradlew.bat --no-daemon --console=plain test --rerun-tasks`：通过。
  - `openspec validate complete-autojs6-vscode-parity --strict`：通过。
  - `openspec instructions apply --change complete-autojs6-vscode-parity --json`：68/68 complete，state=all_done。
  - `./gradlew.bat --no-daemon --console=plain check`：通过。
  - `./gradlew.bat --no-daemon --console=plain buildPlugin`：通过，ZIP `build/distributions/AutoJs6-JetBrains-0.1.0.zip` 已生成。
  - IDE `build_project`：通过，`problems=[]`。
  - `git diff --check`：通过。
- 说明：对“项目已同步但设备没有可见执行效果”的情况，当前保持 VSCode-compatible diff/bytes_command 语义，不强制改成 full sync；如设备端不支持无变更触发可见执行，应记录为设备行为而不是改协议。

### 阶段 14：状态栏设备显示/切换优化探索
- **状态：** complete
- **时间：** 2026-06-06 Asia/Shanghai
- 执行的操作：
  - 读取活跃规划与 `openspec list --json`，确认 `complete-autojs6-vscode-parity` 当前 68/68 complete。
  - 审计 `AutoJs6ConnectionService.kt`、`AutoJs6ToolWindowFactory.kt`、`AutoJs6Actions.kt`、`plugin.xml` 和现有单测。
  - 搜索本地 IntelliJ 2024.2 平台包，确认状态栏扩展点和 API 签名。
- 结论：状态栏显示已连接设备并点击切换当前设备是高可行、低风险优化；关键是增加“选中设备变化”的实时通知，并避免用设备 name 做选择键。
- 错误日志：
  - `jar tf` 不可用：当前 PATH 没有 `jar.exe`；改用 `.NET ZipFile` 搜索 jar 内容，并用 `C:\Program Files\Java\jdk-21\bin\javap.exe` 查看 API。

### 阶段 15：状态栏设备显示/切换实现
- **状态：** complete
- **时间：** 2026-06-06 Asia/Shanghai
- 执行的操作：
  - 新增状态栏实现文件：`AutoJs6DeviceStatusBarWidget.kt`、`AutoJs6DeviceStatusBarWidgetFactory.kt`、`AutoJs6DeviceStatusText.kt`。
  - 修改 `AutoJs6ConnectionService.kt`，增加 `selectedDeviceChanged` 实时通知与 `selectedDeviceKey()` 读取入口。
  - 修改 `AutoJs6ToolWindowFactory.kt`，用 device key 维护表格选择，和状态栏共享 selected device。
  - 修改 `plugin.xml` 注册 `statusBarWidgetFactory`。
  - 修改 `MvpUnitTest.kt`，覆盖状态栏注册和设备文本/tooltip 状态。
  - 更新 `openspec/changes/complete-autojs6-vscode-parity` tasks/spec 与 docs 台账。
- 验证结果：
  - `./gradlew.bat --no-daemon --console=plain test --rerun-tasks`：通过。
  - `openspec validate complete-autojs6-vscode-parity --strict`：通过。
  - `openspec instructions apply --change complete-autojs6-vscode-parity --json`：69/69 complete，state=all_done。
  - `./gradlew.bat --no-daemon --console=plain check`：通过。
  - `./gradlew.bat --no-daemon --console=plain buildPlugin`：通过。
  - IDE `build_project`：通过，`problems=[]`。
  - `git diff --check`：通过。

### 阶段 16：OpenSpec 归档
- **状态：** complete
- **时间：** 2026-06-06 Asia/Shanghai
- 执行的操作：
  - `openspec list --json`：确认仅有 `complete-autojs6-vscode-parity` active，69/69 complete。
  - `openspec status --change complete-autojs6-vscode-parity --json`：proposal/design/specs/tasks 全部 done。
  - `openspec archive complete-autojs6-vscode-parity --yes`：同步 specs 并归档。
  - 修复 `openspec/specs/autojs6-project-template/spec.md` 的主规格标题格式。
- 验证结果：
  - `openspec validate --all --strict`：11 passed, 0 failed。
  - `openspec list --json`：`changes=[]`。
- 归档位置：`openspec/changes/archive/2026-06-06-complete-autojs6-vscode-parity/`。

### 阶段 17：IDE 2026 兼容上限问题启动
- **状态：** in_progress
- **开始时间：** 2026-06-06 13:22:23 +08:00 Asia/Shanghai
- 用户反馈：插件功能沙箱测试通过，但导入 IDEA 2026 (IU-261.25134.95) 报
equires build 242.* or older，说明产物兼容上限严重错误。
- 已恢复活跃计划并执行 session catchup；git diff --stat 当前无输出，说明工作区暂无未提交差异。
- 下一步：直接审计 Gradle 配置和产物 plugin.xml 的 idea-version。

### 阶段 17 增量：定位兼容上限来源
- **状态：** in_progress
- **时间：** 2026-06-06 13:23:48 +08:00 Asia/Shanghai
- 读取 `build.gradle.kts`、`gradle.properties`、`settings.gradle.kts`、`src/main/resources/META-INF/plugin.xml`。
- 确认源码 `plugin.xml` 没有手写 `<idea-version>`；生成产物 `build/tmp/patchPluginXml/plugin.xml` 自动变为 `since-build="242" until-build="242.*"`。
- 一次 ZIP 直接查找 `META-INF/plugin.xml` 失败，因为发行 ZIP 内大概率是嵌套 JAR，需要改为先列 ZIP/JAR 结构再读取内层描述符。
- 错误记录：第一次写入发现时 PowerShell 双引号 here-string 将 Markdown 反引号解析为转义导致 ParserError；已改用单引号 here-string。

### 阶段 17 增量：修复 patchPluginXml 上限
- **状态：** in_progress
- **时间：** 2026-06-06 13:28:41 +08:00 Asia/Shanghai
- 修改 `build.gradle.kts`：在 `patchPluginXml` 中设置 `untilBuild.set(provider { null })`，保留 `sinceBuild.set("242")`。
- 新增 `verifyPatchedPluginXmlCompatibility`，并挂到 `check` 与 `buildPlugin`，防止未来产物重新带上 `until-build`。
- 第一次运行 `patchPluginXml verifyPatchedPluginXmlCompatibility` 失败：Gradle configuration cache 无法序列化被任务闭包捕获的 `TaskContainerScope`。
- 改为使用 `patchedPluginXmlOutput` provider 后，`patchPluginXml verifyPatchedPluginXmlCompatibility` 已通过。
- 产物检查：`build/tmp/patchPluginXml/plugin.xml` 现在显示 `<idea-version since-build="242" />`。

### 阶段 17 完成：兼容上限修复与验证
- **状态：** complete
- **时间：** 2026-06-06 13:40:36 +08:00 Asia/Shanghai
- 修改 `build.gradle.kts`：取消 `until-build`，新增 descriptor 兼容门禁，配置 baseline `verifyPlugin` IDE (`IC 2024.2`)。
- 修改 `docs/release-compatibility-matrix.md`：记录 `untilBuild: unset`、IDEA 2026 import target、descriptor compatibility gate、baseline verifier 状态。
- 修改 `docs/release-guide.md`：明确 descriptor gate、说明默认 `verifyPlugin` 不等于所有未来 IDE 的完整验证，并补充 IDEA 2026 验证方式。
- 验证通过：`clean check buildPlugin`、`verifyPlugin`、`check buildPlugin`、ZIP 内层 JAR 描述符检查、`git diff --check`。
- 注意：`verifyPlugin` 过程中曾因访问 JetBrains API changes 文档超时打印 ERROR，但任务继续完成并判定 IC-242.20224.300 Compatible；该网络错误不影响本次 descriptor 修复结论。

### 阶段 17 追加验证：IDE build
- **状态：** complete
- **时间：** 2026-06-06 13:42:02 +08:00 Asia/Shanghai
- 执行 JetBrains IDE uild_project：成功，problems 为空。

### 阶段 18：补全 JetBrains Marketplace 发布文档
- **状态：** in_progress
- **开始时间：** 2026-06-06 13:59:54 +08:00 Asia/Shanghai
- 用户补充：测试已通过，准备发布到 IDEA/JetBrains 插件中心；需要补全账号注册、上传发布完整步骤，并沉淀到文档。
- 约束：插件地址使用 https://github.com/terwer/AutoJs6-JetBrains；维护邮箱 youweics@163.com；官网 https://terwer.space；发布口径需支持 JetBrains 全系列，不能只写 IDEA。


### 阶段 18 增量：官方发布资料与本地文档审计
- **时间：** 2026-06-06 14:04:09 Asia/Shanghai
- 查阅 JetBrains 官方发布、上传、签名、兼容性资料，确认首次发布必须手动、后续可 Gradle 自动发布。
- 审计 `docs/release-guide.md`、`docs/release-compatibility-matrix.md`、`build.gradle.kts`、`plugin.xml`、README，定位需要补全发布步骤和替换仓库地址的位置。
- 错误记录：前两次用 PowerShell here-string 写入带 Markdown 反引号的 url= 片段时触发 Unicode escape 解析错误；已改用 Python 追加写入。


### 阶段 18 完成：发布文档与发布配置落地
- **时间：** 2026-06-06 14:16:35 Asia/Shanghai
- 修改 `docs/release-guide.md`：补全 JetBrains Account 注册、Vendor profile、Marketplace 上传、元数据、签名、Verifier、全系列 IDE smoke matrix、回滚和阻断条件。
- 修改 `docs/release-compatibility-matrix.md`：明确 JetBrains 全系列 IDE 目标矩阵和每个产品的证据要求。
- 修改 `src/main/resources/META-INF/plugin.xml`：vendor 邮箱保持 `youweics@163.com`，新增 `url="https://terwer.space"`，vendor 名称为 `terwer`，描述中加入仓库地址。
- 修改 `build.gradle.kts`：接入仅从环境变量读取的 signing/publishing 配置，避免提交发布凭据。
- 修改 `README.md` / `README_zh_CN.md`：插件仓库替换为 `https://github.com/terwer/AutoJs6-JetBrains`，补充 release guide 链接，修正 JDK/JVM 21 与真实 Project Run Configuration 描述。
- 验证：
  - 第一次 `clean check buildPlugin` 因 120s 工具超时中断；使用更长超时重新执行后通过。
  - `.\gradlew.bat --no-daemon --console=plain clean check buildPlugin`：通过。
  - `.\gradlew.bat --no-daemon --console=plain verifyPlugin`：通过，IC-242.20224.300 Compatible，报告 6 个 deprecated API usages。
  - ZIP 内层 `META-INF/plugin.xml`：`<idea-version since-build="242" />`，无 `until-build`，包含 `url="https://terwer.space"` 和 `https://github.com/terwer/AutoJs6-JetBrains`。
  - `git diff --check`：通过（仅有 CRLF 提示，不是 whitespace error）。
- 阶段 18 状态：complete。
