# AutoJs6 发布兼容矩阵

本矩阵用于约束 Marketplace 发布时的“JetBrains 全系列 IDE”口径。插件的目标是支持 JetBrains IDE family（build `242+` / 2024.2+），但每个公开版本必须记录具体证据；没有 verifier 或手工 smoke 记录的产品只能标记为 `Target / 待验证`，不能宣传为“已验证”。

## 基线配置

```text
IntelliJ Platform baseline: IC 2024.2
sinceBuild: 242
untilBuild: unset（patched plugin descriptor 不设置上限）
Required module: com.intellij.modules.platform
JDK toolchain: 21
Distributable: build/distributions/AutoJs6-JetBrains-<version>.zip
Repository: https://github.com/terwer/AutoJs6-JetBrains
Vendor: terwer <youweics@163.com>, https://terwer.space
```

## JetBrains 全系列目标矩阵

| IDE family | 发布目标 | 必需证据 | 备注 |
|---|---|---|---|
| IntelliJ IDEA Community / Ultimate 2024.2+ | Target | Plugin Verifier + 手工安装 / Tools 菜单 / Action Search / Run Config smoke | 基线 IC 2024.2 已用于构建；IU 仍需按版本 smoke |
| IntelliJ IDEA Ultimate 2026.1 (`IU-261.25134.95`) | Target / smoke required | 描述符必须为 `<idea-version since-build="242" />`；Plugin Verifier + 手工导入运行 | 用于回归曾经的 `242.* or older` 导入阻断 |
| WebStorm 2024.2+ | Target | Plugin Verifier + `.js` 文件 run/save smoke | JS 文件体验重点验证 |
| PyCharm 2024.2+ | Target | Plugin Verifier + `.js` 文件 run/save smoke | 不同 edition 的 JS 支持差异需记录 |
| PhpStorm 2024.2+ | Target | Plugin Verifier + 菜单/Project View smoke | 检查快捷键冲突 |
| GoLand 2024.2+ | Target | Plugin Verifier + 菜单/Project View smoke | 检查快捷键冲突 |
| RubyMine 2024.2+ | Target | Plugin Verifier + 菜单/Project View smoke | 检查快捷键冲突 |
| CLion 2024.2+ | Target | Plugin Verifier + 菜单/Project View smoke | 检查工具栏/Project View 可见性 |
| Rider 2024.2+ | Target | Plugin Verifier + 菜单/Project View smoke | Rider 平台差异需单独记录 |
| DataGrip 2024.2+ | Target | Plugin Verifier + Action Search smoke | 数据库 IDE 中 `.js` 项目体验需 smoke |
| DataSpell 2024.2+ | Target | Plugin Verifier + Action Search smoke | 文件/Project View 行为需 smoke |
| RustRover 2024.2+ | Target | Plugin Verifier + Action Search smoke | 新产品版本需额外关注 verifier warnings |
| Aqua 2024.2+ | Target | Plugin Verifier + Action Search smoke | 如 Marketplace 产品列表可选，需单独验证 |
| Android Studio | Non-JetBrains / separate claim | 单独安装与 smoke | 不是 JetBrains IDE family 口径；如要声明需单独证据 |
| IDE builds before 242 | Unsupported | N/A | 当前 `sinceBuild=242`，不支持 2024.2 之前版本 |

## 资源与打包策略

| 资源 | 路径 | 策略 |
|---|---|---|
| 项目模板 | `src/main/resources/assets/template` | 始终内置；New Project 从模板复制并替换占位符 |
| Windows ADB fallback | `src/main/resources/tools/adb.exe`, `AdbWinApi.dll`, `AdbWinUsbApi.dll` | Windows 上配置 ADB / PATH ADB 不可用时使用 |
| 非 Windows ADB | 不默认内置 | 使用用户配置路径或 PATH 中的 `adb` |
| 发布文档 | `docs/release-guide.md` | 覆盖账号注册、Marketplace 上传、签名、Verifier、回滚和故障排查 |
| Marketplace 图标/截图 | 待准备 | 公开发布前建议补真实 UI 截图；不得使用 mock UI 或第三方商标 |

## 回归矩阵

| Area | 自动化证据 | 发布前手工 / 集成证据 |
|---|---|---|
| Frame codec | `frameCodecUsesBigEndianLengthAndType` | replay fixture review |
| JSON payload | `jsonRoundTripPreservesCommandPayload`, fixture parse test | 无额外要求 |
| Command/action registration | `pluginXmlRegistersAllVscodeParityActionsAndToolWindow` | runIde Action Search、Tools 菜单、Project View、Toolbar smoke |
| Project diff / md5 | `projectSyncBuildsZipMd5IgnoresAndTracksDeletedFiles` | 真项目修改/删除/ignore 文件同步 smoke |
| Run Configuration | script/project configuration tests | AutoJs6 Script 与 AutoJs6 Project 创建、保存、运行 smoke |
| Command whitelist / HTTP gate | `commandWhitelistAndHttpGateRejectUnknownCommands` | runIde `/exec?cmd=run...` 与安全默认值 smoke |
| LAN connection | Host parser unit test | 真实 AutoJs6 Server/Client 至少一种路径 smoke |
| ADB connection | `parsesAdbDevicesOutput`; live manual protocol replay | 插件 UI ADB selection/forward smoke |
| Tool Window/logs | registration/implementation tests | 设备连接、日志、断开、状态栏切换 smoke |
| IDE family | build against IC 2024.2 | 每个目标产品 Plugin Verifier + 手工 smoke |
| Descriptor compatibility | `verifyPatchedPluginXmlCompatibility` | ZIP 内层 JAR 必须只有 `<idea-version since-build="242" />`，不得有 `until-build` |

## 手工脚本

| Script | Covers |
|---|---|
| `scripts/manual/runide-smoke.ps1` | runIde launch、Tools/Action Search/menu/Tool Window smoke checklist |
| `scripts/manual/http-bridge-replay.ps1` | `/exec?cmd=run&path=...` 与 `/exec?cmd=rerunProject&path=...` |
| `scripts/manual/adb-project-replay.py` | ADB forward、AutoJs6 hello、project zip bytes、`save_project` / `run_project` `bytes_command` replay |
| `scripts/manual/ide-family-plugin-verifier.ps1` | 对已安装 IDE 目录循环运行 Plugin Verifier |

## 当前发布门禁状态

- 自动化测试：按发布前最新 `clean check buildPlugin` 输出为准。
- 基线 Plugin Verifier：`.\gradlew.bat verifyPlugin --no-daemon` 覆盖 IC 2024.2；其他 IDE family 仍需按上表补证据。
- IDEA 2026 导入阻断：已通过 descriptor gate 防止 `until-build="242.*"` 回归；仍需 IDEA 2026 Plugin Verifier + 手工 smoke 后才能写“已验证 2026”。
- 全系列支持声明：目标支持 JetBrains IDE family；公开页如写“全系列”，必须附带本矩阵或 release notes 中的验证说明。
- HTTP Bridge successful dispatch：仍以 runIde 手工 `/exec` dispatch 记录为准。
