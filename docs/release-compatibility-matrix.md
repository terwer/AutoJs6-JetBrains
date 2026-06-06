# AutoJs6 Release Compatibility Matrix

This matrix defines the target JetBrains IDE family/version scope and the evidence required before a release can claim support. Rows may remain `Open` until Plugin Verifier and manual smoke tests are run; open rows are not full-support claims.

## Target IDE family matrix

Baseline platform in `build.gradle.kts`:

```text
IntelliJ Platform: IC 2024.2
sinceBuild: 242
untilBuild: unset (no upper IDE build cap in the patched plugin descriptor)
Required module: com.intellij.modules.platform
JDK toolchain: 21
```

| IDE family | Target status | Verification evidence | Exception / notes |
|---|---|---|---|
| IntelliJ IDEA Community / Ultimate 2024.2+ | Target | Build against IC 2024.2; runIde/manual smoke required before release | Current automated tests do not replace runIde smoke |
| IntelliJ IDEA Ultimate 2026.1 (`IU-261.25134.95`) | Install target / smoke required | Descriptor must be `<idea-version since-build="242" />`; Plugin Verifier + manual import/run smoke required | Added after the 2026 import blocker caused by `until-build="242.*"` |
| WebStorm 2024.2+ | Target if platform action/toolwindow APIs available | Plugin Verifier + manual JS file action smoke required | JavaScript language features are IDE-provided; plugin does not ship JS language service |
| PyCharm 2024.2+ | Target if platform action/toolwindow APIs available | Plugin Verifier + manual `.js` file smoke required | JS support varies by edition/plugins; AutoJs6 actions operate on local `.js` files by extension |
| PhpStorm / RubyMine / GoLand / CLion / Rider 2024.2+ | Target if platform module compatibility passes | Plugin Verifier required | Product-specific menu/keymap conflicts must be documented |
| Android Studio | Open / exception candidate | Not built from JetBrains Marketplace IDE baseline in this project | Verify separately before claiming support |
| IDE builds before 242 | Unsupported by current `sinceBuild` | N/A | Users need an IDE compatible with build 242+ |

## Resource and ADB packaging strategy

| Resource | Packaging path | Strategy |
|---|---|---|
| Project template | `src/main/resources/assets/template` | Always bundled; New Project copies from this template and replaces placeholders |
| ADB Windows fallback | `src/main/resources/tools/adb.exe`, `AdbWinApi.dll`, `AdbWinUsbApi.dll` | Used when configured ADB and PATH ADB are unavailable on Windows |
| ADB non-Windows | Not bundled by default | Use configured path or PATH `adb`; document platform-specific installation |
| Docs | `docs/*.md`, README files | Included in repository and release notes; key usage is also exposed via IDE actions/help |
| Icons | Open | No custom icon package is required for current platform-only UI; add icon assets before Marketplace branding if desired |

## Regression matrix

| Area | Automated evidence | Manual / integration evidence required for release |
|---|---|---|
| Frame codec | `frameCodecUsesBigEndianLengthAndType` | None beyond replay fixture review |
| JSON payloads | `jsonRoundTripPreservesCommandPayload`, fixture parse test | None |
| Command/action registration | `pluginXmlRegistersAllVscodeParityActionsAndToolWindow` | runIde Action Search and menu smoke |
| Project diff / md5 | `projectSyncBuildsZipMd5IgnoresAndTracksDeletedFiles` | Real project with ignored files and deletion |
| Package suffix | `packageSuffixIsPackageSafe` | New Project smoke with non-ASCII/number-leading names |
| Command whitelist / HTTP gate | `commandWhitelistAndHttpGateRejectUnknownCommands` | runIde `/exec?cmd=rerunProject&path=...` and normal `/exec?cmd=run&path=...` |
| LAN connection | Host parser unit test | Real LAN Server mode and Client mode smoke |
| ADB connection | `parsesAdbDevicesOutput`; live manual protocol replay on `emulator-5560` | Plugin UI ADB selection/forward smoke |
| Run/save/stop | Socket replay tests | Real AutoJs6 device smoke |
| Project sync | Unit diff test; live manual `save_project`/`run_project` replay | Plugin UI background task + cancellation smoke |
| Tool Window/logs | Implementation registration test | Device connect/log/disconnect UI smoke |
| IDE family | Build against IC 2024.2 | Plugin Verifier per IDE family row |
| Descriptor compatibility | `verifyPatchedPluginXmlCompatibility` and `buildPlugin` check patched descriptor | ZIP inner JAR must contain `<idea-version since-build="242" />` and no `until-build` |

## Manual scripts

| Script | Covers |
|---|---|
| `scripts/manual/runide-smoke.ps1` | runIde launch, Tools/Action Search/menu/Tool Window smoke checklist |
| `scripts/manual/http-bridge-replay.ps1` | `/exec?cmd=run&path=...` and `/exec?cmd=rerunProject&path=...` against a runIde HTTP bridge |
| `scripts/manual/adb-project-replay.py` | Raw ADB forward, AutoJs6 hello, project zip bytes, `save_project` and `run_project` `bytes_command` replay |
| `scripts/manual/ide-family-plugin-verifier.ps1` | Plugin Verifier execution loop for declared IDE family targets |

## Current release gate status

- Automated tests: passing as of the latest `./gradlew.bat test --no-daemon` run.
- Baseline Plugin Verifier: `.\gradlew.bat verifyPlugin --no-daemon` passes against IC 2024.2; IDEA 2026-specific verifier/manual smoke remains required before claiming full 2026 support.
- Distributable ZIP: must be regenerated for each release by `buildPlugin`.
- Full IDE-family support claim: **Open** until Plugin Verifier/manual matrix is recorded.
- Full HTTP successful dispatch claim: **Open** until runIde manual `/exec` dispatch is recorded.
