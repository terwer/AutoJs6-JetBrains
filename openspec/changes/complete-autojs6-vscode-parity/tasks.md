## 1. Parity 基线与矩阵

- [ ] 0.1 确认并发布四条不可妥协规则：runtime/protocol compatibility plus built-in-template project scaffolding、保留用户习惯并采用 JetBrains best practices、additive feature policy，以及 no mock/fake/speculation。
- [ ] 0.2 增加 compatibility ledger，要求每个已声明的 command、protocol、project-sync、connection、UI 和 release 行为都有证据。
- [ ] 0.3 增加 no-mock gate：如果任务依赖静态数据、fake success、未实现 placeholder、猜测的 protocol fields、未验证 publishing steps 或未验证 IDE compatibility claims，则不能标记为完成。
- [ ] 1.1 从 `package.json` commands、menus、keybindings 和 breakpoints 建立 VSCode parity matrix。
- [ ] 1.2 将每个 VSCode command 映射到 JetBrains Action id、UI location、shortcut recommendation 和 verification step。
- [ ] 1.3 记录 VSCode 行为与更安全 JetBrains 行为之间被接受的差异，尤其是 HTTP binding；差异必须有明确 compatibility-mode fallback 或已记录的用户批准例外。
- [ ] 1.4 准备 protocol replay fixtures，覆盖 JSON frame、bytes frame、hello、command、log 和 bytes_command payload。

## 2. Action 与上下文入口 Parity

- [ ] 2.1 注册缺失的 parity actions：commandsHierarchy、runOnDevice、rerun、saveToDevice、newUntitledFile、runProject、saveProject，以及 argument-less variants。
- [ ] 2.2 为 parity actions 增加 editor context、Project View context、main menu、toolbar 和 action search placement。
- [ ] 2.3 在 JetBrains 允许的范围内，为 F6/F8 和 VSCode-equivalent compound shortcuts 增加 suggested keymap metadata。
- [ ] 2.4 将 rerun 实现为 stop 后再 run，并使用确定性 delay 或 callback。
- [ ] 2.5 使用 JetBrains editor/document APIs 实现 newUntitledFile。
- [ ] 2.6 验证全部 18 个 parity commands 都可发现且可调用。

## 3. 设备定向、Tool Window 与日志

- [ ] 3.1 增加 AutoJs6 Tool Window，展示 connected device table 和 connection metadata。
- [ ] 3.2 为 runOnDevice 和 saveToDevice 实现 selected-device targeting。
- [ ] 3.3 增加 device-scoped disconnect、run、save、stop 和 diagnostics actions。
- [ ] 3.4 将设备 log payloads 路由到 dedicated output console 或 Tool Window panel。
- [ ] 3.5 在 attach、normal disconnect、unexpected disconnect 和 reconnect 时更新 Tool Window。
- [ ] 3.6 为主要操作结果和错误增加 notifications 与 log entries。

## 4. 项目差量同步（Project Diff Synchronization）

- [ ] 4.1 为文件夹和 `project.json` 文件实现 AutoJs6 project detection。
- [ ] 4.2 解析 `project.json` 并 normalize `ignore` entries。
- [ ] 4.3 实现安全项目文件遍历，以及基于 mtime 的 modified/deleted tracking。
- [ ] 4.4 使用 resolved path handling 实现 ignore filtering。
- [ ] 4.5 按 VSCode 扩展行为一致的 relative paths zip modified files。
- [ ] 4.6 计算 zip bytes 的 md5。
- [ ] 4.7 在 JSON `bytes_command` payload 之前发送 bytes payload。
- [ ] 4.8 为所有设备和 selected folder contexts 实现 runProject 和 saveProject commands。
- [ ] 4.9 在 background 中运行 project sync，并提供 progress、cancellation 和 error reporting。
- [ ] 4.10 在 AutoJs6 device 上验证 run_project/save_project。

## 5. 高级连接体验（Advanced Connection Experience）

- [ ] 5.1 枚举合适的 local IPv4 interfaces，并尽量隐藏 loopback/APIPA/virtual adapter noise。
- [ ] 5.2 为选中的 IDE listening address 显示 AutoJs6 client-mode connection instructions。
- [ ] 5.3 持久化 recent AutoJs6 server host/IP records，并记录 timestamps。
- [ ] 5.4 增加 clear-records confirmation 和 storage cleanup。
- [ ] 5.5 实现 LAN active connection flow，包括 host:port parsing 和 duplicate detection。
- [ ] 5.6 实现 ADB device list parsing，并包含 model/product details。
- [ ] 5.7 实现 ADB provider query，或对 AutoJs6 debug server ports 提供 compatible fallback。
- [ ] 5.8 实现 ADB forward creation、timeout handling、cleanup，并通过 loopback connection 建立连接。
- [ ] 5.9 在启用时增加 optional QR/copyable connection hint。
- [ ] 5.10 为 unavailable ADB、occupied port、unreachable host、handshake timeout、unsupported version 和 duplicate connection 增加 diagnostics。

## 6. 远程命令桥接（Remote Command Bridge）

- [ ] 6.1 实现 configurable HTTP server lifecycle 和 settings。
- [ ] 6.2 为支持的 parity commands 实现 `/exec?cmd=<command>&path=<path>` dispatch。
- [ ] 6.3 默认将 HTTP bridge 设为 disabled 或 loopback-bound safe mode。
- [ ] 6.4 为 wider network binding 增加 explicit compatibility mode，并要求 user confirmation。
- [ ] 6.5 实现 device-originated command dispatch whitelist。
- [ ] 6.6 用清晰错误信息拒绝 unknown HTTP 或 device commands。
- [ ] 6.7 通过 replay/manual tests 验证 remote rerunProject 和普通 command dispatch。

## 7. VSCode Parity 完成门禁

- [ ] 7.1 验证 scope 只包含 VSCode full-parity requirements，以及已批准的 additive JetBrains-native presentation/diagnostic helpers。
- [ ] 7.2 验证每个 VSCode extension command 都有 JetBrains action、context placement、command payload behavior 和 validation step。
- [ ] 7.3 验证每个 VSCode visible workflow 都有等价 JetBrains-native UI，或有明确记录并批准的 exception。
- [ ] 7.4 验证新增 convenience actions（如有）都是 real parity commands 的 additive wrappers，且不会替代必需的 VSCode-equivalent behavior。
- [ ] 7.5 增加 one-click diagnostics summary，覆盖 port、devices、ADB、recent records、HTTP bridge 和 compatibility mode，作为 additive JetBrains-native helper。
- [ ] 7.6 如果任意 parity row 缺失、mocked、guessed 或 undocumented，则阻断 release。

## 8. Debug Boundary 与帮助

- [ ] 8.1 在 plugin help 中记录 JavaScript breakpoint parity 和当前 debugger limitations。
- [ ] 8.2 增加 debug/help action，说明 run/save 与 full debugger attachment 的边界。
- [ ] 8.3 保持 device services 和 command dispatch 可扩展，以便未来 debugger integration。
- [ ] 8.4 为 VSCode breakpoint contribution 和已接受的 JetBrains 行为增加 parity matrix row。

## 9. 发布、兼容性与回归（Release / Compatibility / Regression）

- [ ] 9.1 定义 JetBrains IDE family/version compatibility matrix 和 exception matrix；默认目标是包含所需 modules 的所有 JetBrains IntelliJ Platform IDE，不只 IDEA/WebStorm。
- [ ] 9.2 打包 icons、templates、docs，并定义 platform-specific ADB fallback strategy。
- [ ] 9.3 为 frame codec、project diff、package suffix normalization、command whitelist 和 HTTP dispatch 增加 unit tests。
- [ ] 9.4 为 runIde、LAN connection、ADB connection、run/save、project sync、HTTP bridge，以及每个声明的 JetBrains IDE family target 增加 integration tests 或 manual scripts。
- [ ] 9.5 生成 distributable plugin ZIP 和 metadata。
- [ ] 9.6 准备并维护 `release-guide.md`：local ZIP build/install、Plugin Verifier、signing、Marketplace upload by the user、version/changelog preparation、approval checklist、private distribution、rollback 和 troubleshooting。
- [ ] 9.7 只有当每个 command/protocol/UI row 都有 passing evidence 或 accepted documented difference 时，才可将 parity matrix 标记为 complete。
- [ ] 9.8 release 前运行最终四规则 audit：无 runtime/protocol breakage、无 disruptive workflow rewrite、无 missing claimed features、无 mock/fake/speculative completion、无 unverified IDE compatibility claim。

