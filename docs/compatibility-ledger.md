# AutoJs6 JetBrains 兼容性台账

## 不可妥协的规则

1. **协议/运行时兼容性优先**：连接、hello、script command、project `bytes_command` 使用 VSCode 扩展观察到的 frame 与 payload 语义；新建项目只使用插件内置模板创建全新项目。
2. **保留用户习惯并采用 JetBrains best practices**：18 个 VSCode command 都有 JetBrains Action；入口通过 Tools、Action Search、Editor/Project View context、Tool Window、Notification、Background Task 暴露。
3. **Additive feature policy**：Tool Window、诊断摘要、Debug Help、Run Configuration、HTTP safe mode 是 additive helper，不替代必需 VSCode-equivalent 行为。
4. **No mock/fake/speculation**：没有真实实现和验证的发布/IDE-family/Marketplace 声明保持 open；不能用空 zip、假 md5、静态设备、fake connected 或未知 protocol 字段标记完成。

## 已实现能力台账

| 能力 | VSCode 行为证据 | JetBrains 实现证据 | 验证 | Open / release gate |
|---|---|---|---|---|
| 18 commands/actions | `package.json.contributes.commands`, `Extension.commands` | `plugin.xml`, `actions/AutoJs6Actions.kt`, `AutoJs6CommandDispatcher.kt` | `MvpUnitTest.pluginXmlRegistersAllVscodeParityActionsAndToolWindow` | runIde 全量手工入口截图仍可作为 release 附件补充 |
| Context/menu/keymap | `editor/title`, `explorer/context`, F6/F8/chords | Editor popup group、Project View popup group、Toolbar group、Tools group、keyboard-shortcut metadata | plugin.xml test covers registration | JetBrains keymap conflict 由用户覆盖，不阻断 action 可达性 |
| Frame protocol | `device.ts`: 8-byte header, JSON=1, bytes=2 | `FrameCodec.kt`, `JsonCodec.kt` | frame/json replay unit tests | 无 |
| Hello/version | AutoJs6 min `6.7.0 / 3591` | `AutoJs6Device.onHello` | replay tests；ADB live hello from `emulator-5560` / `HONOR SDY-AN00` / code `3810` | 无 |
| Device logs | VSCode output channel receives `data:log` | Tool Window log panel via `AutoJs6ConnectionListener.logReceived` | fixture row + implementation | UI screenshot可在 release 附件补充 |
| Device reverse commands | Source uses `\u00A0cmd\u00A0` / whitelist | normalized key handling + `remoteWhitelist` | unknown command gate test | Successful reverse UI dispatch 可在 runIde 手工补充 |
| ADB devices/forward | `adb devices -l`, forward to 7347/20347 | `AdbService.kt` parses model/product, creates forwards, timeout/provider diagnostics, cleanup | parser unit test；live manual forward to `tcp:7347` completed hello | Provider state variations require more devices for release matrix |
| LAN/client connection | Source offers client/server LAN pickers | local IPv4 filter, copyable client-mode hint, host:port parsing, duplicate detection | host parser test | Real LAN outside emulator not yet in release matrix |
| Recent records | VSCode globalState stores records | `RecentHostRecord(host,timestamp)`, clear confirmation | unit test | 无 |
| Run/save/stop/rerun | source sends command payload with id/name/script; rerun stop→run | current file/project view/path support, selected-device support, 480 ms rerun delay | existing replay tests + action registration test | Full UI smoke in runIde remains release evidence |
| Project sync | `project.ts`/`diff.ts`: project.json, ignore, mtime, zip, md5, deletedFiles, override, bytes before JSON | `AutoJs6ProjectSyncService.kt`, Backgroundable task | unit test for ignore/diff/deleted/zip/md5；live ADB replay sent `save_project` and `run_project`, received device log frame | Larger projects/cancellation manual test remains release evidence |
| HTTP `/exec` | Source `/exec?cmd=&path=` on `0.0.0.0:10347` | configurable safe bridge, default disabled/loopback, compatibility mode required for wide bind | unknown/missing/no-project gate test | Successful runIde HTTP dispatch still open for release gate |
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

## 兼容性例外矩阵

| 领域 | 例外 / open item | 原因 | 影响 | 替代方案 / gate |
|---|---|---|---|---|
| JetBrains IDE family | 尚未逐一验证所有 IntelliJ Platform IDE 产品/版本 | 当前只构建 against IC 2024.2 platform | 不能宣称“所有 IDE 已验证” | release 前执行 Plugin Verifier 和 manual matrix；结果写入 `release-compatibility-matrix.md` |
| HTTP wide binding | 默认不绑定 `0.0.0.0` | 安全差异；VSCode 行为可暴露局域网 | 默认行为不同于 VSCode | 显式 compatibility mode 后允许 wide bind |
| Full debugger stepping | 不声明支持 | VSCode extension 只有 JS breakpoint contribution；AutoJs6 debugger adapter 未实现 | IDE breakpoint 不会驱动运行时 stepping | Debug Help 说明边界；未来 change 接入 debug adapter |
| Project Run Configuration | 未注册 `AutoJs6 Project` Run Configuration | VSCode parity command 已支持 project run/save；Run Config 是 JetBrains additive，需另行 UX 设计 | 用户通过 action/Tool Window 运行项目 | 后续独立提案，不阻断 VSCode command parity |

## No-mock completion gate

任务勾选前必须满足：真实代码路径存在、失败时报告错误、不在未连接/未验证时显示成功、protocol 字段来自 source/fixture/live behavior、release/IDE claims 有证据或保持 open。
