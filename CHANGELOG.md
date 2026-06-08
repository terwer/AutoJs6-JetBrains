# Changelog

## v0.1.2 - macOS ADB Resolution Fix

- Fixed macOS ADB discovery when JetBrains IDEs are launched outside a shell and do not inherit the user's zsh/PATH environment.
- Added ADB resolution through IntelliJ environment PATH plus common Android SDK and Homebrew locations, while keeping the Windows bundled `adb.exe` fallback unchanged.

## v0.1.1 - Project Run Sync Fix

- Fixed JetBrains project Run/Save becoming invalid after the first project execution.
- Project commands now save pending IDE document changes before syncing, so project runs use the latest edited code.
- Second-and-later project syncs now send a complete current project archive when using override mode, preventing AutoJs6 remote project cache errors such as missing `main.js`.

## v0.1.0 - Initial Release

AutoJs6 JetBrains v0.1.0 is the first public release of the AutoJs6 plugin for JetBrains IDEs. It brings the AutoJs6 VSCode workflow to the IntelliJ Platform with native IDE actions, Run Configurations, device connectivity, project sync, logs, diagnostics, and release-ready metadata.

---

## Features

### Native JetBrains IDE workflow

- AutoJs6 commands are available from **Tools -> AutoJs6**, Action Search, editor context menus, Project View context menus, and toolbar entries.
- Native Run Configurations are available for both single scripts and AutoJs6 projects:
  - `AutoJs6 Script`
  - `AutoJs6 Project`
- The AutoJs6 Tool Window shows connected devices and device logs.
- The status bar widget shows AutoJs6 device state and supports selected-device switching.
- Notifications and background tasks are used for connection, command dispatch, and project sync feedback.

### Device connectivity

- Supports AutoJs6 Client -> IDE LAN listener mode.
- Supports AutoJs6 Server -> IDE LAN/IP connection mode.
- Supports ADB connection and forwarding, with Windows bundled ADB fallback.
- Supports multi-device sessions, selected-device operations, recent hosts, and connection diagnostics.
- Validates AutoJs6 app compatibility against `6.7.0` / version code `3591`.

### Script commands

- Run, save, stop, stop all, and rerun AutoJs6 scripts from JetBrains IDEs.
- Run/save/stop scripts against all devices or a selected device.
- Create new untitled AutoJs6 scripts.
- Use the command hierarchy dialog for VSCode-style command discovery.
- Open AutoJs6 online documentation directly from the IDE.

### Project commands and sync

- Create AutoJs6 projects from the bundled template.
- Detect AutoJs6 project roots through `project.json` from editor, Project View, explicit paths, and project base paths.
- Run and save AutoJs6 projects from actions or the native `AutoJs6 Project` Run Configuration.
- Sync projects through the real AutoJs6 protocol using ignore rules, modified/deleted file tracking, zip payloads, MD5, and `bytes_command` frames.
- Preserve existing AutoJs6 project layout, command names, payload fields, ports, and protocol semantics.

### Run console and logs

- AutoJs6 script and project runs now produce native JetBrains Run console output.
- Device-side log events stream into active Run console sessions.
- Run console log lines are prefixed with device name and endpoint.
- Stopping a Run session sends the corresponding AutoJs6 remote stop command.

### HTTP bridge and diagnostics

- `/exec` HTTP bridge support is available with safe loopback binding by default.
- Compatibility mode can be explicitly enabled for wider network binding.
- HTTP and reverse-command dispatch use a shared whitelist.
- Diagnostics Summary, Device Diagnostics, and Debug Help actions are included.

---

## Fixes

- Fixed Run Configuration registration so AutoJs6 configurations are correctly registered with the IntelliJ Platform.
- Fixed missing project Run Configuration support with a real `AutoJs6 Project` configuration type, producer, editor, serializer, and run profile state.
- Fixed missing toolbar access for `Run Project` and `Save Project`.
- Fixed Run console sessions ending immediately after dispatch by keeping them alive for device log streaming.
- Fixed Stop button behavior by sending remote AutoJs6 stop commands before ending Run sessions.
- Fixed project command routing so actions, command hierarchy, HTTP dispatch, and Run Configurations share the same real project sync path.
- Fixed project sync correctness with bytes-first ordering, MD5 validation, ignore handling, deleted-file tracking, and per-device sync state.
- Fixed selected-device ambiguity by using stable device keys instead of display names only.
- Fixed 2025/2026 IDE installation blocking by removing the generated `until-build="242.*"` descriptor cap.
- Fixed HTTP bridge safety by rejecting wide-network binding unless compatibility mode is explicitly confirmed.
- Fixed invalid script paths and missing `project.json` roots to fail clearly instead of reporting false success.

---

## Chores

- Added protocol fixtures and automated coverage for command payloads, project sync, Run Configurations, action registration, HTTP whitelist, status bar text, and device handshake flows.
- Added Plugin Verifier baseline for IC 2024.2.
- Added descriptor compatibility gate to prevent `until-build` regression.
- Added local environment-variable based signing and publishing configuration.
- Added release guide, compatibility matrix, usage/testing docs, VSCode parity matrix, and compatibility ledger.
- Switched project licensing to GNU General Public License v3.0.
- Updated README metadata, repository links, license text, and removed active-development warning banners.

---

## Compatibility

- JetBrains IDE baseline: IntelliJ Platform `242+` / 2024.2+.
- Descriptor range: `since-build="242"`, no `until-build` upper cap.
- Required module: `com.intellij.modules.platform`.
- AutoJs6 app requirement: `>= 6.7.0` / version code `>= 3591`.
- Build toolchain: JDK 21.
- Baseline verification: `clean check buildPlugin` and `verifyPlugin` against IC 2024.2.

### Notes

- Designed for the JetBrains IDE family; per-product public compatibility claims should follow `docs/release-compatibility-matrix.md`.
- Android Studio should be verified separately before being advertised.
- Full JavaScript debugger attach/stepping is not included in v0.1.0; Debug Help documents the current boundary.
- HTTP bridge compatibility mode may expose the bridge beyond loopback and should only be enabled intentionally.
