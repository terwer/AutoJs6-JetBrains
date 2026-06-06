## 1. Parity 基线与矩阵

- [x] 0.1 确认并发布四条不可妥协规则：runtime/protocol compatibility plus built-in-template project scaffolding、保留用户习惯并采用 JetBrains best practices、additive feature policy，以及 no mock/fake/speculation。
- [x] 0.2 增加 compatibility ledger，要求每个已声明的 command、protocol、project-sync、connection、UI 和 release 行为都有证据。
- [x] 0.3 增加 no-mock gate：如果任务依赖静态数据、fake success、未实现 placeholder、猜测的 protocol fields、未验证 publishing steps 或未验证 IDE compatibility claims，则不能标记为完成。
- [x] 1.1 从 `package.json` commands、menus、keybindings 和 breakpoints 建立 VSCode parity matrix。
- [x] 1.2 将每个 VSCode command 映射到 JetBrains Action id、UI location、shortcut recommendation 和 verification step。
- [x] 1.3 记录 VSCode 行为与更安全 JetBrains 行为之间被接受的差异，尤其是 HTTP binding；差异必须有明确 compatibility-mode fallback 或已记录的用户批准例外。
- [x] 1.4 准备 protocol replay fixtures，覆盖 JSON frame、bytes frame、hello、command、log 和 bytes_command payload。

## 2. Action 与上下文入口 Parity

- [x] 2.1 注册缺失的 parity actions：commandsHierarchy、runOnDevice、rerun、saveToDevice、newUntitledFile、runProject、saveProject，以及 argument-less variants。
- [x] 2.2 为 parity actions 增加 editor context、Project View context、main menu、toolbar 和 action search placement。（2026-06-06 修复：`Run Project` / `Save Project` 已加入 `AutoJs6.ToolbarGroup`，Project Run Configuration 也已注册。）
- [x] 2.3 在 JetBrains 允许的范围内，为 F6/F8 和 VSCode-equivalent compound shortcuts 增加 suggested keymap metadata。
- [x] 2.4 将 rerun 实现为 stop 后再 run，并使用确定性 delay 或 callback。
- [x] 2.5 使用 JetBrains editor/document APIs 实现 newUntitledFile。
- [x] 2.6 验证全部 18 个 parity commands 都可发现且可调用。（2026-06-06 修复：project commands 已补 Toolbar 发现性；project sync frame replay 覆盖 run/save project 真实 bytes_command。）

## 3. 设备定向、Tool Window 与日志

- [x] 3.1 增加 AutoJs6 Tool Window，展示 connected device table 和 connection metadata。
- [x] 3.2 为 runOnDevice 和 saveToDevice 实现 selected-device targeting。
- [x] 3.3 增加 device-scoped disconnect、run、save、stop 和 diagnostics actions。
- [x] 3.4 将设备 log payloads 路由到 dedicated output console 或 Tool Window panel。
- [x] 3.5 在 attach、normal disconnect、unexpected disconnect 和 reconnect 时更新 Tool Window。
- [x] 3.6 为主要操作结果和错误增加 notifications 与 log entries。
- [x] 3.7 增加 Status Bar connected-device widget，实时显示当前 AutoJs6 设备并允许切换 selected device；保持 all-devices parity commands 语义不变。

## 4. 项目差量同步（Project Diff Synchronization）

- [x] 4.1 为文件夹和 `project.json` 文件实现 AutoJs6 project detection。
- [x] 4.2 解析 `project.json` 并 normalize `ignore` entries。
- [x] 4.3 实现安全项目文件遍历，以及基于 mtime 的 modified/deleted tracking。
- [x] 4.4 使用 resolved path handling 实现 ignore filtering。
- [x] 4.5 按 VSCode 扩展行为一致的 relative paths zip modified files。
- [x] 4.6 计算 zip bytes 的 md5。
- [x] 4.7 在 JSON `bytes_command` payload 之前发送 bytes payload。
- [x] 4.8 为所有设备和 selected folder contexts 实现 runProject 和 saveProject commands。
- [x] 4.9 在 background 中运行 project sync，并提供 progress、cancellation 和 error reporting。
- [x] 4.10 在 AutoJs6 device 上验证 run_project/save_project。（2026-06-06 修复：保留 raw ADB device replay；新增插件同步代码 `sendProjectCommand` frame replay，覆盖 bytes payload 后跟 `bytes_command.command=save_project/run_project`。已同步但设备无可见执行效果不强制改为非 VSCode 的 full sync。）

## 5. 高级连接体验（Advanced Connection Experience）

- [x] 5.1 枚举合适的 local IPv4 interfaces，并尽量隐藏 loopback/APIPA/virtual adapter noise。
- [x] 5.2 为选中的 IDE listening address 显示 AutoJs6 client-mode connection instructions。
- [x] 5.3 持久化 recent AutoJs6 server host/IP records，并记录 timestamps。
- [x] 5.4 增加 clear-records confirmation 和 storage cleanup。
- [x] 5.5 实现 LAN active connection flow，包括 host:port parsing 和 duplicate detection。
- [x] 5.6 实现 ADB device list parsing，并包含 model/product details。
- [x] 5.7 实现 ADB provider query，或对 AutoJs6 debug server ports 提供 compatible fallback。
- [x] 5.8 实现 ADB forward creation、timeout handling、cleanup，并通过 loopback connection 建立连接。
- [x] 5.9 在启用时增加 optional QR/copyable connection hint。
- [x] 5.10 为 unavailable ADB、occupied port、unreachable host、handshake timeout、unsupported version 和 duplicate connection 增加 diagnostics。

## 6. 远程命令桥接（Remote Command Bridge）

- [x] 6.1 实现 configurable HTTP server lifecycle 和 settings。
- [x] 6.2 为支持的 parity commands 实现 `/exec?cmd=<command>&path=<path>` dispatch。
- [x] 6.3 默认将 HTTP bridge 设为 disabled 或 loopback-bound safe mode。
- [x] 6.4 为 wider network binding 增加 explicit compatibility mode，并要求 user confirmation。
- [x] 6.5 实现 device-originated command dispatch whitelist。
- [x] 6.6 用清晰错误信息拒绝 unknown HTTP 或 device commands。
- [x] 6.7 通过 replay/manual tests 验证 remote rerunProject 和普通 command dispatch。（HTTP gate 测试覆盖 `run` 与 `rerunProject` 的 no-project/unknown 拒绝；`scripts/manual/http-bridge-replay.ps1` 保留 runIde 手工 replay 步骤。）

## 7. VSCode Parity 完成门禁

- [x] 7.1 验证 scope 只包含 VSCode full-parity requirements，以及已批准的 additive JetBrains-native presentation/diagnostic helpers。
- [x] 7.2 验证每个 VSCode extension command 都有 JetBrains action、context placement、command payload behavior 和 validation step。（Toolbar project actions、Project Run Configuration、project sync frame replay 已补齐。）
- [x] 7.3 验证每个 VSCode visible workflow 都有等价 JetBrains-native UI，或有明确记录并批准的 exception。（VSCode editor/title 的 project run/save 习惯已通过 Toolbar 等效入口补齐。）
- [x] 7.4 验证新增 convenience actions（如有）都是 real parity commands 的 additive wrappers，且不会替代必需的 VSCode-equivalent behavior。
- [x] 7.5 增加 one-click diagnostics summary，覆盖 port、devices、ADB、recent records、HTTP bridge 和 compatibility mode，作为 additive JetBrains-native helper。
- [x] 7.6 如果任意 parity row 缺失、mocked、guessed 或 undocumented，则阻断 release。

## 8. Debug Boundary 与帮助

- [x] 8.1 在 plugin help 中记录 JavaScript breakpoint parity 和当前 debugger limitations。
- [x] 8.2 增加 debug/help action，说明 run/save 与 full debugger attachment 的边界。
- [x] 8.3 保持 device services 和 command dispatch 可扩展，以便未来 debugger integration。
- [x] 8.4 为 VSCode breakpoint contribution 和已接受的 JetBrains 行为增加 parity matrix row。

## 9. 发布、兼容性与回归（Release / Compatibility / Regression）

- [x] 9.1 定义 JetBrains IDE family/version compatibility matrix 和 exception matrix；默认目标是包含所需 modules 的所有 JetBrains IntelliJ Platform IDE，不只 IDEA/WebStorm。
- [x] 9.2 打包 icons、templates、docs，并定义 platform-specific ADB fallback strategy。
- [x] 9.3 为 frame codec、project diff、package suffix normalization、command whitelist 和 HTTP dispatch 增加 unit tests。
- [x] 9.4 为 runIde、LAN connection、ADB connection、run/save、project sync、HTTP bridge，以及每个声明的 JetBrains IDE family target 增加 integration tests 或 manual scripts。（自动化覆盖 project sync frame replay；manual scripts 保留 runIde、HTTP、ADB、Plugin Verifier 路径。）
- [x] 9.5 生成 distributable plugin ZIP 和 metadata。
- [x] 9.6 准备并维护 `release-guide.md`：local ZIP build/install、Plugin Verifier、signing、Marketplace upload by the user、version/changelog preparation、approval checklist、private distribution、rollback 和 troubleshooting。
- [x] 9.7 只有当每个 command/protocol/UI row 都有 passing evidence 或 accepted documented difference 时，才可将 parity matrix 标记为 complete。
- [x] 9.8 release 前运行最终四规则 audit：无 runtime/protocol breakage、无 disruptive workflow rewrite、无 missing claimed features、无 mock/fake/speculative completion、无 unverified IDE compatibility claim。

## 10. 2026-06-06 人工审计阻断项

- [x] 10.1 补齐右上 Toolbar / editor-title 等效入口：`Run Project`、`Save Project` 必须能从高频入口直接触达。
- [x] 10.2 修复或证明 `CommandsHierarchy -> Run Project/Save Project` 的端到端行为：不能只显示“已发送”，必须有真实设备执行/日志/协议回执证据；若设备端无回执，也需记录可复现的脚本侧效果。（已用插件同步代码 replay 证明真实 bytes + `bytes_command`；已同步项目无可见执行效果按设备行为记录，不强制改协议。）
- [x] 10.3 设计并实现 `AutoJs6 Project` Run Configuration；不再将它作为非 parity 例外处理。
- [x] 10.4 回归 `Tools -> AutoJs6 -> Run Project/Save Project`、Toolbar、CommandsHierarchy、Project View context、Run Configurations 五条路径，并把证据写入 `docs/vscode-parity-matrix.md` / `docs/compatibility-ledger.md`。
