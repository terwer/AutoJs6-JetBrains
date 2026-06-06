# AutoJs6 JetBrains 兼容性台账

## 不可妥协的规则

1. **协议/运行时兼容性优先**：连接、hello、script command、project `bytes_command` 使用 VSCode 扩展观察到的 frame 与 payload 语义；新建项目只使用插件内置模板创建全新项目。
2. **保留用户习惯并采用 JetBrains best practices**：18 个 VSCode command 都有 JetBrains Action；入口通过 Tools、Action Search、Editor/Project View context、Tool Window、Notification、Background Task 暴露。
3. **Additive feature policy**：Tool Window、状态栏设备切换、诊断摘要、Debug Help、Run Configuration、HTTP safe mode 是 additive helper，不替代必需 VSCode-equivalent 行为。
4. **No mock/fake/speculation**：没有真实实现和验证的发布/IDE-family/Marketplace 声明保持 open；不能用空 zip、假 md5、静态设备、fake connected 或未知 protocol 字段标记完成。

## 已实现能力台账

| 能力 | VSCode 行为证据 | JetBrains 实现证据 | 验证 | Open / release gate |
|---|---|---|---|---|
| 18 commands/actions | `package.json.contributes.commands`, `Extension.commands` | `plugin.xml`, `actions/AutoJs6Actions.kt`, `AutoJs6CommandDispatcher.kt` | `MvpUnitTest.pluginXmlRegistersAllVscodeParityActionsAndToolWindow` | runIde 全量手工入口截图仍可作为 release 附件补充 |
| Context/menu/keymap | `editor/title`, `explorer/context`, F6/F8/chords | Editor popup group、Project View popup group、Toolbar group、Tools group、keyboard-shortcut metadata | plugin.xml test covers project toolbar actions and registration | 无 |
| Frame protocol | `device.ts`: 8-byte header, JSON=1, bytes=2 | `FrameCodec.kt`, `JsonCodec.kt` | frame/json replay unit tests | 无 |
| Hello/version | AutoJs6 min `6.7.0 / 3591` | `AutoJs6Device.onHello` | replay tests；ADB live hello from `emulator-5560` / `HONOR SDY-AN00` / code `3810` | 无 |
| Device logs | VSCode output channel receives `data:log` | Tool Window log panel via `AutoJs6ConnectionListener.logReceived` | fixture row + implementation | UI screenshot可在 release 附件补充 |
| Status Bar device switcher | VSCode 使用 quick pick/命令入口选择设备 | `statusBarWidgetFactory` 注册 `AutoJs6DeviceStatus`，显示当前连接设备并用 snapshot key 切换 shared selected device | `MvpUnitTest.statusBarDeviceTextReflectsSelectionAndEmptyState`；plugin.xml 注册测试；`AutoJs6ConnectionListener.selectedDeviceChanged` 实时刷新 | runIde 可补充多设备切换截图；不改变 all-devices parity commands |
| Device reverse commands | Source uses `\u00A0cmd\u00A0` / whitelist | normalized key handling + `remoteWhitelist` | unknown command gate test | Successful reverse UI dispatch 可在 runIde 手工补充 |
| ADB devices/forward | `adb devices -l`, forward to 7347/20347 | `AdbService.kt` parses model/product, creates forwards, timeout/provider diagnostics, cleanup | parser unit test；live manual forward to `tcp:7347` completed hello | Provider state variations require more devices for release matrix |
| LAN/client connection | Source offers client/server LAN pickers | local IPv4 filter, copyable client-mode hint, host:port parsing, duplicate detection | host parser test | Real LAN outside emulator not yet in release matrix |
| Recent records | VSCode globalState stores records | `RecentHostRecord(host,timestamp)`, clear confirmation | unit test | 无 |
| Run/save/stop/rerun | source sends command payload with id/name/script; rerun stop→run | current file/project view/path support, selected-device support, 480 ms rerun delay | existing replay tests + action registration test | Full UI smoke in runIde remains release evidence |
| Project sync | `project.ts`/`diff.ts`: project.json, ignore, mtime, zip, md5, deletedFiles, override, bytes before JSON | `AutoJs6ProjectSyncService.kt`, Backgroundable task, `AutoJs6 Project` Run Configuration | unit test for ignore/diff/deleted/zip/md5；`projectSyncSendsRealBytesCommandFramesForRunAndSave` verifies bytes-before-JSON through plugin sync code；raw ADB replay sent `save_project` and `run_project` | 若设备对“已同步但无变更”的 `run_project` 没有可见执行效果，保持 VSCode diff 兼容语义，不强制改为 full sync |
| HTTP `/exec` | Source `/exec?cmd=&path=` on `0.0.0.0:10347` | configurable safe bridge, default disabled/loopback, compatibility mode required for wide bind | unknown/missing/no-project gate test；`scripts/manual/http-bridge-replay.ps1` documents runIde replay | runIde HTTP smoke 可作为发布附件补充，不阻断当前 parity implementation |
| Debug boundary | JS breakpoint contribution | `AutoJs6.DebugHelp`, matrix row, reusable command/device services | action registered, help text implemented | Full debugger attachment deferred by design |
| Release docs | User-managed publication required | `docs/release-guide.md`, `docs/release-compatibility-matrix.md` | docs present | Plugin Verifier/family matrix execution remains open |

## 实机/回放证据摘录

- `./gradlew.bat test --no-daemon`: passed after parity implementation.
- ADB real device check: `adb devices -l` found `emulator-5560 device product:Sandy model:SDY_AN00`.
- Live project protocol replay:
  - ADB forward local TCP → device `tcp:7347`.
  - Received device hello: `device_name=HONOR SDY-AN00`, `app_version=6.7.0`, `app_version_code=3810`.
  - Sent project zip bytes (`336` bytes, md5 `0f5b89dcc1326f1a9afd5a3c3de9c16a`) before JSON `bytes_command`.
  - Sent both `save_project` and `run_project`.
  - Received live device log frame after dispatch: `type=log`.
  - 审计备注：该 replay 使用 `scripts/manual/adb-project-replay.py` 直接连接设备端口，证明设备协议形状可用，但不能替代插件 UI 路径的 `runIde` 端到端验收。

## 2026-06-06 人工回归阻断与处理

| 阻断项 | 当前证据 | 处理要求 |
|---|---|---|
| 右上 Toolbar 缺项目运行/保存 | 已将 `RunProject` / `SaveProject` 加入 `AutoJs6.ToolbarGroup` | `pluginXmlRegistersAllVscodeParityActionsAndToolWindow` 覆盖 |
| CommandsHierarchy 项目运行提示已发送但未实际运行 | CommandsHierarchy 复用 `sendProjectCommand`；新增 `AutoJs6ProjectSyncService.sendProjectCommand` frame replay 证明插件代码发送 bytes + `bytes_command` | 对已同步项目无可见执行的设备行为不强制改变协议；如需进一步证明，运行 `runIde` + 设备侧脚本效果检查 |
| Project Run Configuration 缺失 | 已注册 `AutoJs6ProjectConfigurationType`、producer、settings editor、run profile state | `pluginXmlRegistersRunConfigurationTypeWithPlatformExtensionPoint`、serializer 测试、项目 sync frame replay 覆盖 |

## 兼容性例外矩阵

| 领域 | 例外 / open item | 原因 | 影响 | 替代方案 / gate |
|---|---|---|---|---|
| JetBrains IDE family | 尚未逐一验证所有 IntelliJ Platform IDE 产品/版本 | 当前只构建 against IC 2024.2 platform | 不能宣称“所有 IDE 已验证” | release 前执行 Plugin Verifier 和 manual matrix；结果写入 `release-compatibility-matrix.md` |
| HTTP wide binding | 默认不绑定 `0.0.0.0` | 安全差异；VSCode 行为可暴露局域网 | 默认行为不同于 VSCode | 显式 compatibility mode 后允许 wide bind |
| Full debugger stepping | 不声明支持 | VSCode extension 只有 JS breakpoint contribution；AutoJs6 debugger adapter 未实现 | IDE breakpoint 不会驱动运行时 stepping | Debug Help 说明边界；未来 change 接入 debug adapter |
| Project Run Configuration | 已注册 `AutoJs6 Project` Run Configuration | VSCode parity command 已支持 project run/save；JetBrains Run UX 作为 additive 入口复用同一 project sync 协议 | 当前实现支持创建/保存项目 root 并运行 `run_project` | 后续可继续做 runIde 截图/IDE-family matrix 证据 |

## No-mock completion gate

任务勾选前必须满足：真实代码路径存在、失败时报告错误、不在未连接/未验证时显示成功、protocol 字段来自 source/fixture/live behavior、release/IDE claims 有证据或保持 open。
