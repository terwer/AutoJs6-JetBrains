## 1. Parity Baseline And Matrix



- [ ] 0.1 Confirm and publish the four non-negotiable rules: 100% historical project runtime compatibility, preserved user habits with JetBrains best practices, additive feature policy, and no mock/fake/speculation.
- [ ] 0.2 Add a compatibility ledger requiring evidence for every claimed command, protocol, project-sync, connection, UI, and release behavior.
- [ ] 0.3 Add a no-mock gate: tasks cannot be checked complete if they rely on static data, fake success, unimplemented placeholders, guessed protocol fields, unverified publishing steps, or unverified IDE compatibility claims.
- [ ] 1.1 Build a VSCode parity matrix from `package.json` commands, menus, keybindings, and breakpoints.
- [ ] 1.2 Map each VSCode command to JetBrains Action id, UI location, shortcut recommendation, and verification step.
- [ ] 1.3 Record accepted differences between VSCode behavior and safer JetBrains behavior, especially HTTP binding; differences require explicit compatibility-mode fallback or documented user-approved exception.
- [ ] 1.4 Prepare protocol replay fixtures for JSON frame, bytes frame, hello, command, log, and bytes_command payloads.

## 2. Action And Context Parity

- [ ] 2.1 Register missing parity actions: commandsHierarchy, runOnDevice, rerun, saveToDevice, newUntitledFile, runProject, saveProject, and argument-less variants.
- [ ] 2.2 Add editor context, Project View context, main menu, toolbar, and action search placement for parity actions.
- [ ] 2.3 Add suggested keymap metadata for F6/F8 and VSCode-equivalent compound shortcuts where JetBrains permits.
- [ ] 2.4 Implement rerun as stop followed by run with deterministic delay or callback.
- [ ] 2.5 Implement newUntitledFile using JetBrains editor/document APIs.
- [ ] 2.6 Validate all 18 parity commands are discoverable and invokable.

## 3. Device Targeting, Tool Window, And Logs

- [ ] 3.1 Add AutoJs6 Tool Window with connected device table and connection metadata.
- [ ] 3.2 Implement selected-device targeting for runOnDevice and saveToDevice.
- [ ] 3.3 Add device-scoped disconnect, run, save, stop, and diagnostics actions.
- [ ] 3.4 Route device log payloads into a dedicated output console or Tool Window panel.
- [ ] 3.5 Update Tool Window on attach, normal disconnect, unexpected disconnect, and reconnect.
- [ ] 3.6 Add notifications and log entries for major operation results and errors.

## 4. Project Diff Synchronization

- [ ] 4.1 Implement AutoJs6 project detection for folders and `project.json` files.
- [ ] 4.2 Parse `project.json` and normalize `ignore` entries.
- [ ] 4.3 Implement safe project file traversal and mtime-based modified/deleted tracking.
- [ ] 4.4 Implement ignore filtering with resolved path handling.
- [ ] 4.5 Zip modified files with relative paths matching VSCode extension behavior.
- [ ] 4.6 Compute md5 of zip bytes.
- [ ] 4.7 Send bytes payload before JSON `bytes_command` payload.
- [ ] 4.8 Implement runProject and saveProject commands for all devices and selected folder contexts.
- [ ] 4.9 Run project sync in background with progress, cancellation, and error reporting.
- [ ] 4.10 Validate run_project/save_project on an AutoJs6 device.

## 5. Advanced Connection Experience

- [ ] 5.1 Enumerate suitable local IPv4 interfaces and hide loopback/APIPA/virtual adapter noise where possible.
- [ ] 5.2 Show AutoJs6 client-mode connection instructions for selected IDE listening address.
- [ ] 5.3 Persist recent AutoJs6 server host/IP records with timestamps.
- [ ] 5.4 Add clear-records confirmation and storage cleanup.
- [ ] 5.5 Implement LAN active connection flow with host:port parsing and duplicate detection.
- [ ] 5.6 Implement ADB device list parsing with model/product details.
- [ ] 5.7 Implement ADB provider query or compatible fallback for AutoJs6 debug server ports.
- [ ] 5.8 Implement ADB forward creation, timeout handling, cleanup, and connection through loopback.
- [ ] 5.9 Add optional QR/copyable connection hint when enabled.
- [ ] 5.10 Add diagnostics for unavailable ADB, occupied port, unreachable host, handshake timeout, unsupported version, and duplicate connection.

## 6. Remote Command Bridge

- [ ] 6.1 Implement configurable HTTP server lifecycle and settings.
- [ ] 6.2 Implement `/exec?cmd=<command>&path=<path>` dispatch for supported parity commands.
- [ ] 6.3 Default HTTP bridge to disabled or loopback-bound safe mode.
- [ ] 6.4 Add explicit compatibility mode for wider network binding with user confirmation.
- [ ] 6.5 Implement device-originated command dispatch whitelist.
- [ ] 6.6 Reject unknown HTTP or device commands with clear error messages.
- [ ] 6.7 Validate remote rerunProject and ordinary command dispatch against replay/manual tests.

## 7. VSCode Parity Completion Gate

- [ ] 7.1 Verify the scope contains only VSCode full-parity requirements and approved additive JetBrains-native presentation/diagnostic helpers.
- [ ] 7.2 Verify every VSCode extension command has a JetBrains action, context placement, command payload behavior, and validation step.
- [ ] 7.3 Verify every VSCode visible workflow has either equivalent JetBrains-native UI or an explicitly documented approved exception.
- [ ] 7.4 Verify convenience actions, if added, are additive wrappers over real parity commands and do not replace required VSCode-equivalent behavior.
- [ ] 7.5 Add one-click diagnostics summary for port, devices, ADB, recent records, HTTP bridge, and compatibility mode as an additive JetBrains-native helper.
- [ ] 7.6 Block release if any parity row is missing, mocked, guessed, or undocumented.

## 8. Debug Boundary And Help

- [ ] 8.1 Document JavaScript breakpoint parity and current debugger limitations in plugin help.
- [ ] 8.2 Add debug/help action explaining run/save vs full debugger attachment boundary.
- [ ] 8.3 Keep device services and command dispatch extensible for a future debugger integration.
- [ ] 8.4 Add parity matrix row for VSCode breakpoint contribution and accepted JetBrains behavior.

## 9. Release, Compatibility, And Regression

- [ ] 9.1 Define JetBrains IDE family/version compatibility matrix and exception matrix; default goal is every JetBrains IntelliJ Platform IDE that contains required modules, not only IDEA/WebStorm.
- [ ] 9.2 Package icons, templates, docs, and platform-specific ADB fallback strategy.
- [ ] 9.3 Add unit tests for frame codec, project diff, package suffix normalization, command whitelist, and HTTP dispatch.
- [ ] 9.4 Add integration tests or manual scripts for runIde, LAN connection, ADB connection, run/save, project sync, HTTP bridge, and every declared JetBrains IDE family target.
- [ ] 9.5 Produce distributable plugin ZIP and metadata.
- [ ] 9.6 Prepare and maintain `release-guide.md`: local ZIP build/install, Plugin Verifier, signing, Marketplace upload by the user, version/changelog preparation, approval checklist, private distribution, rollback, and troubleshooting.
- [ ] 9.7 Mark parity matrix complete only when every command/protocol/UI row has passing evidence or an accepted documented difference.
- [ ] 9.8 Run a final four-rule audit before release: no historical-project breakage, no disruptive workflow rewrite, no missing claimed features, no mock/fake/speculative completion, and no unverified IDE compatibility claim.
