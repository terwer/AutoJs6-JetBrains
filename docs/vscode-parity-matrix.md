# AutoJs6 VSCode Parity Matrix

本文档是 `complete-autojs6-vscode-parity` 的验收矩阵。状态只按当前代码和已记录证据填写；无法验证的发布/IDE-family 项保持 open，不用推测结果冒充完成。

## 2026-06-06 人工回归审计与修复结论

- 已补齐右上 Toolbar/editor-title 等效入口：`Run Project` / `Save Project` 现在加入 `AutoJs6.ToolbarGroup`。
- 已注册 `AutoJs6 Project` Run Configuration，可在 `Run | Edit Configurations...` 中创建，运行时复用 project diff/zip/md5/`bytes_command.command=run_project` 语义。
- `CommandsHierarchy -> Run Project/Save Project` 仍按同一 `sendProjectCommand` 路径发送；自动化 replay 已覆盖插件项目同步代码发送 bytes frame 后再发送 `bytes_command`。若设备端对“项目已同步但未显式重新执行”没有可见效果，不强制改变 VSCode 兼容 diff 语义。

## 证据来源

- 源扩展：`D:/Users/Administrator/Documents/myproject/AutoJs6-VSCode-Extension/package.json`
  - `contributes.commands`: 18 个 `extension.*` command。
  - `contributes.menus`: `editor/title`、`explorer/context`、隐藏 commandPalette variants。
  - `contributes.keybindings`: F6/F8/组合快捷键。
  - `contributes.breakpoints`: `javascript` breakpoint contribution。
- 源运行时：
  - `src/extension.ts`: `Extension.commands`、`registerCommands()`、script/project/HTTP dispatch。
  - `src/device.ts`: JSON/bytes frame、hello、log、device reverse command、`bytes_command` ordering。
  - `src/project.ts` / `src/diff.ts`: `project.json`、ignore、mtime diff、zip、md5、deletedFiles、override。
- JetBrains 实现：
  - `src/main/resources/META-INF/plugin.xml`
  - `src/main/kotlin/org/autojs/autojs6/jetbrains/actions/*`
  - `src/main/kotlin/org/autojs/autojs6/jetbrains/device/*`
  - `src/main/kotlin/org/autojs/autojs6/jetbrains/project/AutoJs6ProjectSyncService.kt`
  - `src/main/kotlin/org/autojs/autojs6/jetbrains/remote/AutoJs6HttpBridgeService.kt`
  - `src/main/kotlin/org/autojs/autojs6/jetbrains/toolwindow/AutoJs6ToolWindowFactory.kt`

## Commands / menus / keybindings

| # | VSCode command | JetBrains Action | UI placement / shortcut | Payload behavior | Evidence |
|---:|---|---|---|---|---|
| 1 | `extension.viewDocument` | `AutoJs6.ViewDocument` | Tools → AutoJs6; suggested `Alt+Shift+F6` | Opens `https://docs.autojs6.com/` | `plugin.xml`; action implementation |
| 2 | `extension.connect` | `AutoJs6.Connect` | Tools, toolbar; suggested `Ctrl+Alt+F6` | Client LAN instructions, active LAN host:port, ADB selection/forward, diagnostics | `AutoJs6Actions.kt`, `AutoJs6NetworkInterfaces.kt`, `AdbService.kt` |
| 3 | `extension.disconnectAll` | `AutoJs6.DisconnectAll` | Tools; suggested `Ctrl+Alt+Shift+F6` | Closes all sockets, updates Tool Window/listeners | `AutoJs6ConnectionService.disconnectAll` |
| 4 | `extension.run` | `AutoJs6.RunCurrentFile` | Tools, toolbar, editor popup, Project View popup; `F6` | Sends `command=run` with `id/name/script` from editor, selected `.js`, or path | `AutoJs6ActionSupport.sendCurrentFileCommand`; replay tests |
| 5 | `extension.runWithoutArguments` | `AutoJs6.RunWithoutArguments` | Tools, editor popup | Same current-context run path without explicit argument | `pluginXmlRegistersAllVscodeParityActionsAndToolWindow` |
| 6 | `extension.commandsHierarchy` | `AutoJs6.CommandsHierarchy` | Tools, toolbar; `F8` | Shows real command hierarchy and invokes the same action paths | `AutoJs6ActionSupport.showCommandsHierarchy` |
| 7 | `extension.runOnDevice` | `AutoJs6.RunOnDevice` | Tools, editor popup, Project View popup | Chooses selected/explicit device and sends only to that device | `AutoJs6ConnectionService.sendCommandToDevice`, Tool Window selection |
| 8 | `extension.stop` | `AutoJs6.StopCurrentScript` | Tools, editor popup, Project View popup; `Ctrl+F6` | Sends `command=stop` with current/selected file `id` | Existing replay tests + context support |
| 9 | `extension.stopAll` | `AutoJs6.StopAllScripts` | Tools, toolbar, editor popup, Project View popup; `Ctrl+Shift+F6` | Broadcasts `command=stopAll` | Existing replay tests |
| 10 | `extension.rerun` | `AutoJs6.Rerun` | Tools, editor popup, Project View popup | Sends stop for same file, then run after deterministic 480 ms delay | `AutoJs6ActionSupport.rerunCurrentScript` |
| 11 | `extension.save` | `AutoJs6.SaveCurrentFile` | Tools, toolbar, editor popup, Project View popup | Sends `command=save` with same payload shape as run | `AutoJs6ActionSupport.sendCurrentFileCommand` |
| 12 | `extension.saveToDevice` | `AutoJs6.SaveToDevice` | Tools, editor popup, Project View popup | Selected-device save only | `AutoJs6ConnectionService.sendCommandToDevice` |
| 13 | `extension.newUntitledFile` | `AutoJs6.NewUntitledFile` | Tools, editor popup | Opens writable unsaved `LightVirtualFile` document | `AutoJs6ActionSupport.newUntitledFile` |
| 14 | `extension.newProject` | `AutoJs6.NewProject` | Tools, Project View popup; `Ctrl+Alt+6`, `N` | Creates a new project from bundled template; no existing-project migration | `AutoJs6ProjectTemplateService`; template test |
| 15 | `extension.saveProject` | `AutoJs6.SaveProject` | Tools, toolbar, Project View popup; `Ctrl+Alt+6`, `S` | Project diff zip bytes first, then `bytes_command.command=save_project` | `AutoJs6ProjectSyncService`; `projectSyncSendsRealBytesCommandFramesForRunAndSave`; raw ADB replay |
| 16 | `extension.saveProjectWithoutArguments` | `AutoJs6.SaveProjectWithoutArguments` | Tools, editor popup; toolbar-equivalent via `AutoJs6.SaveProject` | Resolves project root from current context; refuses missing `project.json` | `AutoJs6ActionSupport.resolveProjectRoot`; toolbar registration test |
| 17 | `extension.runProject` | `AutoJs6.RunProject` | Tools, toolbar, Project View popup; `Alt+F6`; `Ctrl+Alt+6`, `R` | Project diff zip bytes first, then `bytes_command.command=run_project` | `AutoJs6ProjectSyncService`; `projectSyncSendsRealBytesCommandFramesForRunAndSave`; raw ADB replay |
| 18 | `extension.runProjectWithoutArguments` | `AutoJs6.RunProjectWithoutArguments` | Tools, editor popup; toolbar-equivalent via `AutoJs6.RunProject` | Resolves current project root; refuses missing `project.json` | `AutoJs6ActionSupport.resolveProjectRoot`; toolbar registration test |

## Protocol / project evidence

| Area | Required behavior | Evidence |
|---|---|---|
| Frame codec | 8-byte header, int32BE length/type, JSON=1, bytes=2 | `MvpUnitTest.frameCodecUsesBigEndianLengthAndType` |
| Device hello | AutoJs6 minimum `6.7.0 / 3591`, IDE replies hello | replay tests; live ADB hello from `HONOR SDY-AN00`, AutoJs6 `6.7.0`, code `3810` |
| Logs | Device `type=log` payload routed to Tool Window log panel | `AutoJs6Device.onJson`, `AutoJs6ToolWindowPanel.logReceived`; fixture shape `log-event.json` |
| Device reverse command | Only whitelisted command/cmd payloads execute; unknown rejected | `AutoJs6CommandDispatcher.remoteWhitelist`; `commandWhitelistAndHttpGateRejectUnknownCommands` |
| Project detection | Directory or ancestor containing `project.json`; missing config rejected | `AutoJs6ProjectSyncService.resolveProjectRoot/validateProjectRoot` |
| Ignore filtering | `project.json.ignore` normalized and filtered using resolved path handling | `projectSyncBuildsZipMd5IgnoresAndTracksDeletedFiles` |
| mtime diff/deleted | Per root/device state tracks modified/deleted relative paths | `projectSyncBuildsZipMd5IgnoresAndTracksDeletedFiles` |
| Zip/md5 | Modified files zipped by relative path, md5 over zip bytes | `projectSyncBuildsZipMd5IgnoresAndTracksDeletedFiles` |
| bytes ordering | Send bytes frame before JSON `bytes_command` | `AutoJs6ProjectSyncService.runProjectSyncInBackground`; `projectSyncSendsRealBytesCommandFramesForRunAndSave`; raw ADB replay sent `save_project` then `run_project` |

## Breakpoint parity row

| VSCode contribution | JetBrains parity | Current status | Evidence |
|---|---|---|---|
| `package.json.contributes.breakpoints[0].language = javascript` | Expose clear debug boundary; do not imply unsupported stepping/attach | Implemented as help/documentation boundary, future adapter extension point remains open | `AutoJs6.DebugHelp`, `AutoJs6ActionSupport.showDebugHelp`, this matrix |

## Accepted differences / compatibility notes

| Area | VSCode behavior | JetBrains decision | Evidence / release gate |
|---|---|---|---|
| HTTP bridge binding | VSCode listens on `0.0.0.0:10347` | JetBrains default is disabled / loopback-safe; wider binding requires explicit compatibility mode | `AutoJs6SettingsService` HTTP fields; `AutoJs6HttpBridgeService.start`; unknown commands return 400 |
| Shortcuts | VSCode contributes F6/F8 and chords directly | JetBrains registers suggested keymap metadata; users may override conflicts | `plugin.xml` shortcuts; task 2 tests inspect F6/F8/chords |
| Run Configuration | VSCode has no JetBrains Run Configuration | `AutoJs6 Script` and `AutoJs6 Project` are JetBrains-native additive run entries that reuse the same script/project command protocols | Existing script Run Configuration payload replay test; project configuration registration/serializer tests |
| Tool Window/Diagnostics | VSCode uses output channel and quick picks | JetBrains adds Tool Window/device table/diagnostics as additive helpers | `AutoJs6ToolWindowFactory`, `DiagnosticsSummaryAction` |

## Manual regression blockers and resolution

| Blocker | Evidence | Required before archive |
|---|---|---|
| Toolbar project actions | Fixed: `AutoJs6.RunProject` / `AutoJs6.SaveProject` now join `AutoJs6.ToolbarGroup` | `pluginXmlRegistersAllVscodeParityActionsAndToolWindow` verifies toolbar registration |
| CommandsHierarchy project run/save not proven | Mitigated: same `sendProjectCommand` path now has frame-level replay coverage through `AutoJs6ProjectSyncService.sendProjectCommand` | If a device does not visibly execute an already-synchronized project, keep VSCode-compatible diff semantics and document device behavior rather than forcing an incompatible full sync |
| Project Run Configuration | Fixed: `AutoJs6ProjectConfigurationType`, producer, settings editor, run profile state registered | `pluginXmlRegistersRunConfigurationTypeWithPlatformExtensionPoint`; project serializer test |

## Release-blocking gate

- Any row marked Missing/Partial/Open cannot be used to claim full parity release.
- Project/HTTP/ADB/IDE-family claims require passing tests, replay, real-device evidence, or explicit documented exception.
- JetBrains-only convenience actions (`DiagnosticsSummary`, selected-device wrappers, Debug Help) are additive wrappers/helpers and do not replace the 18 VSCode command rows.
