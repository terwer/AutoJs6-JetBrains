# AutoJs6 VSCode Parity Matrix

本文档是 `complete-autojs6-vscode-parity` 的实施基线。它只记录来自源 VSCode 扩展 `package.json` 与运行时代码的可验证行为，不把未实现功能写成已完成能力。

## 证据来源

- 源扩展：`D:/Users/Administrator/Documents/myproject/AutoJs6-VSCode-Extension/package.json`
  - `contributes.commands`: 18 个 `extension.*` command。
  - `contributes.menus`: `editor/title`、`explorer/context`、隐藏的 `commandPalette` variants。
  - `contributes.keybindings`: F6/F8 及组合快捷键。
  - `contributes.breakpoints`: `javascript` breakpoint contribution。
- 源运行时：
  - `src/extension.ts`: `Extension.commands`、`registerCommands()`、脚本/项目/HTTP command dispatch。
  - `src/device.ts`: 8 字节 frame header、hello、command、log、device reverse command、`bytes_command` 发送顺序。
  - `src/project.ts` / `src/diff.ts`: `project.json` 检测、ignore、mtime diff、zip、md5、deletedFiles。
- 当前 JetBrains 实现：
  - `src/main/resources/META-INF/plugin.xml`
  - `src/main/kotlin/org/autojs/autojs6/jetbrains/actions/AutoJs6Actions.kt`
  - `src/main/kotlin/org/autojs/autojs6/jetbrains/run/*`

## 状态口径

| 状态 | 含义 |
|---|---|
| Implemented | 当前代码已有真实实现，仍需在对应 verification 中保留证据。 |
| Partial | 有部分真实实现，但缺少 VSCode 等价入口、上下文、指定设备、项目语义或验证。 |
| Missing | 尚未实现，不得在 UI/文档中声称支持。 |
| Deferred/Blocked | 明确延期或等待设备/协议/发布验证，不得 fake success。 |

## Commands / menus / keybindings

| # | VSCode command | 用户意图 | VSCode 入口/快捷键 | JetBrains 目标 Action / 当前状态 | Verification step |
|---:|---|---|---|---|---|
| 1 | `extension.viewDocument` | 查看在线文档 | Keybinding: `Alt+Shift+F6` / mac `Cmd+Shift+F6` | Target: `AutoJs6.ViewDocument`; current: Implemented under `Tools → AutoJs6`; shortcut still needs parity review | Action search 可发现；打开 `https://docs.autojs6.com/`。 |
| 2 | `extension.connect` | 建立设备连接 | `editor/title`; key `Ctrl+Alt+F6` / mac `Ctrl+Cmd+F6` | Target/current: `AutoJs6.Connect`; Partial：已有 Dialog flow，后续需 Tool Window、recent records、LAN/ADB parity diagnostics | runIde 中可走 client LAN、server LAN、ADB 入口；连接失败不假成功。 |
| 3 | `extension.disconnectAll` | 断开所有连接 | Key `Ctrl+Alt+Shift+F6` / mac `Ctrl+Cmd+Shift+F6` | Target/current: `AutoJs6.DisconnectAll`; Implemented | 断开后连接列表/通知更新；重复调用安全。 |
| 4 | `extension.run` | 对传入文件或当前文件运行脚本 | `explorer/context` on `.js`; key `F6` when editor focused | Target: `AutoJs6.RunCurrentFile` + Project View selected file context; current: Partial，只覆盖当前编辑器/Tools/F6 | 当前编辑器与 Project View `.js` 都发送同一 `command=run` payload。 |
| 5 | `extension.runWithoutArguments` | 无参数运行当前脚本 | `editor/title`; hidden commandPalette variant | Target/current: `AutoJs6.RunCurrentFile`; Partial：editor title placement 未完全对齐 | 打开 `.js` 后 action 可用；无编辑器/非 JS 报错。 |
| 6 | `extension.commandsHierarchy` | 命令层级 QuickPick | Key `F8` | Target: `AutoJs6.CommandsHierarchy`; current: Missing | F8 或 Action Search 打开真实层级，不用静态假入口标记完成。 |
| 7 | `extension.runOnDevice` | 指定设备运行脚本 | `explorer/context` on `.js` | Target: `AutoJs6.RunOnDevice`; current: Missing | 多设备连接时可选择设备，只向 selected device 发送 run。 |
| 8 | `extension.stop` | 停止当前脚本 | `editor/title` / `explorer/context` on `.js`; key `Ctrl+F6` | Target/current: `AutoJs6.StopCurrentScript`; Partial：Project View selected file context 待补 | 发送 `command=stop` 且 `id` 与当前/选中文件一致。 |
| 9 | `extension.stopAll` | 停止所有脚本 | `editor/title` / `explorer/context`; key `Ctrl+Shift+F6` | Target/current: `AutoJs6.StopAllScripts`; Implemented | 向连接设备广播 `command=stopAll`。 |
| 10 | `extension.rerun` | 重新运行脚本 | `explorer/context` on `.js` | Target: `AutoJs6.Rerun`; current: Missing（Run Configuration 的 Shift+F10 不是此 VSCode command 的替代） | 先 stop 同一文件，再 run 同一文件；顺序和 delay/callback 可验证。 |
| 11 | `extension.save` | 保存当前脚本到所有设备 | `editor/title` / `explorer/context` on `.js` | Target/current: `AutoJs6.SaveCurrentFile`; Partial：Project View selected file context 待补 | 发送 `command=save`，payload 与 run 共享 id/name/script 语义。 |
| 12 | `extension.saveToDevice` | 保存到指定设备 | `explorer/context` on `.js` | Target: `AutoJs6.SaveToDevice`; current: Missing | 多设备连接时只对 selected device 发送 save。 |
| 13 | `extension.newUntitledFile` | 新建未命名脚本文件 | Command only | Target: `AutoJs6.NewUntitledFile`; current: Missing | 使用 JetBrains editor/document API 打开 unsaved document。 |
| 14 | `extension.newProject` | 从内置模板新建项目 | `editor/title`; `explorer/context` on folder; key `Ctrl+Alt+6 N` / mac `Ctrl+Cmd+6 N` | Target/current: `AutoJs6.NewProject`; Partial：Tools/dialog 已有，folder context parity 待补 | 只复制插件内置模板并替换占位符；不迁移 existing project。 |
| 15 | `extension.saveProject` | 保存项目到设备 | `explorer/context` on folder; key `Ctrl+Alt+6 S` | Target: `AutoJs6.SaveProject`; current: Missing | 有效项目发送 zip bytes，再发送 `bytes_command.command=save_project`。 |
| 16 | `extension.saveProjectWithoutArguments` | 无参数保存当前项目 | `editor/title`; hidden commandPalette variant | Target: `AutoJs6.SaveProjectCurrentContext`; current: Missing | 从当前文件/项目上下文解析 project root，失败时报告缺少 `project.json`。 |
| 17 | `extension.runProject` | 运行项目 | `explorer/context` on folder; keys `Alt+F6`, `Ctrl+Alt+6 R` | Target: `AutoJs6.RunProject`; current: Missing | 有效项目发送 zip bytes，再发送 `bytes_command.command=run_project`。 |
| 18 | `extension.runProjectWithoutArguments` | 无参数运行当前项目 | `editor/title`; hidden commandPalette variant | Target: `AutoJs6.RunProjectCurrentContext`; current: Missing | 从当前上下文解析 project root；无项目不发送 fake project command。 |

## Breakpoint parity row

| VSCode contribution | Source | JetBrains parity requirement | Current status | Verification |
|---|---|---|---|---|
| JavaScript breakpoints | `package.json.contributes.breakpoints[0].language = javascript` | 记录并暴露 JS breakpoint 与 AutoJs6 run/save/debug boundary 的关系，不暗示未支持的 stepping/attach 行为 | Missing documentation/help action | 打开 JS 文件时帮助文档说明 IDE breakpoint 当前边界；未来 debugger 支持另开提案或任务。 |

## Accepted differences / compatibility notes

| 领域 | VSCode 行为 | JetBrains parity 决策 | 必需 fallback / 证据 |
|---|---|---|---|
| HTTP bridge binding | 源扩展 `AJHttpServer` 监听 `0.0.0.0:10347` 并处理 `/exec?cmd=&path=` | JetBrains 版本默认 disabled 或 loopback-bound；wide binding 必须显式 compatibility mode | 文档和 Settings 里提供兼容模式；replay/manual test 验证 `/exec` command dispatch。 |
| 快捷键 | VSCode 直接贡献 F6/F8/组合键 | JetBrains 版本注册 suggested keymap metadata；遇 IDE 冲突允许用户覆盖 | Keymap 中可搜索 AutoJs6 action；不因快捷键冲突阻断 Action 可达性。 |
| Run Configuration | VSCode 无原生 JetBrains Run Configuration | `AutoJs6 Script` 是 additive JetBrains-native UX，不替代任何 VSCode command row | Run Configuration payload 必须与 Run Current File 一致；项目 Run Configuration 仍 deferred。 |
| 新建项目 | VSCode 从扩展内置 `assets/template` 创建全新项目 | JetBrains 保持内置模板 scaffolding；不得把 existing project runtime compatibility 变成迁移/转换流程 | 生成项目检查 `project.json` 和 `main.js`；existing project actions 后续按 project diff sync 兼容实现。 |

## Release-blocking gate

- 任一 VSCode command row 为 Missing/Partial 时，不能声称 full parity release 完成。
- 任一 protocol/project/http row 只有静态数据、fake success、未验证字段或猜测行为时，不能勾选对应任务。
- 任何 JetBrains-only convenience action 必须映射到真实 parity command 或清楚标为 additive helper，不能替代必需 VSCode-equivalent 行为。
