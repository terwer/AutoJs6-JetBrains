## Context

源项目：`D:\Users\Administrator\Documents\myproject\AutoJs6-VSCode-Extension`

已探索事实：

- `package.json` 贡献点包括 `breakpoints`、`commands`、`menus`、`keybindings`。
- 没有 `languages`、`grammars`、`configuration` 贡献点，因此第一版不需要做自定义语言能力。
- `src/extension.ts` 是主入口，负责激活、命令注册、ADB 初始化、设备客户端初始化。
- `src/device.ts` 是设备协议核心，包含 TCP server/client、hello 握手、sendCommand、sendBytes。
- `src/project.ts` 和 `src/diff.ts` 支撑模板创建和项目差量 zip 同步。
- `assets/template` 是 AutoJs6 项目模板来源。
- `tools/` 包含 Windows ADB 资源。

关键协议事实：

- IDE 监听端口：6347。
- AutoJs6 服务端默认端口：7347。
- AutoJs6 ADB 服务端端口：20347。
- HTTP 命令入口端口：10347；MVP 暂不纳入。
- TCP frame header 长度：8 bytes。
- header 前 4 bytes：payload length，int32 big-endian。
- header 后 4 bytes：payload type，int32 big-endian。
- payload type：JSON = 1，bytes = 2。
- AutoJs6 最低版本：6.7.0 / versionCode 3591。

## Goals / Non-Goals



## Non-Negotiable Compatibility Rules

1. **100% historical runtime compatibility:** implemented MVP behavior SHALL preserve existing AutoJs6 project runtime behavior and VSCode-extension-compatible protocol semantics; if an old project can run through the existing VSCode extension, the JetBrains implementation MUST NOT introduce incompatible project format, command, path, encoding, or payload changes for the same supported flow.
2. **No disruptive habit rewrites:** existing command names, mental model, connection modes, shortcut intent, project template behavior, and run/save/stop workflow SHALL remain recognizable; JetBrains-native UI is required, but it must wrap the workflow instead of forcing a different one.
3. **No feature shrink by claim:** MVP may defer features, but anything declared implemented SHALL match or exceed the source behavior for that flow. Deferred features must remain explicitly listed and must not be hidden behind partial placeholders.
4. **No mock/fake/speculation:** implementation and validation MUST use real frame codec, real socket/ADB/process interactions, real file/template operations, and real device or replay fixtures. Unknown behavior must be marked `blocked`, `deferred`, or `requires verification`.
**Goals:**

- 以 JetBrains Action 形式提供 AutoJs6 基础操作。
- 与 AutoJs6 App 保持 VSCode 扩展协议兼容。
- 先完成当前文件级别的 run/save/stop 能力。
- 支持基础连接模式：IDE 监听、IP 主动连接、ADB 端口转发。
- 复用 AutoJs6 项目模板创建新项目。
- 保持架构可扩展，后续能加入 Tool Window、项目同步和 HTTP 命令入口。

**Non-Goals:**

- MVP 不一次性实现完整 VSCode 扩展等价迁移，但不得破坏或改写已有项目、协议和用户工作流；完整等价由 `complete-autojs6-vscode-parity` 承接。
- 不做自定义 JavaScript/AutoJs6 语言解析器，除非后续提案证明 VSCode 扩展确有等价能力需要补齐。
- MVP 不实现完整项目差量 zip 同步，但必须明确标为 deferred，不能用 mock 或假成功替代。
- MVP 不实现 HTTP 远程命令入口，但必须明确标为 deferred，不能开放半成品端口。
- MVP 不做 Marketplace 发布，但不能阻碍后续私有 ZIP/Marketplace 分发。
- MVP 不得绑定某一个 JetBrains IDE；实现应基于通用 IntelliJ Platform API。若个别 JetBrains IDE 因缺少必要 platform module 或运行环境限制无法支持，必须列入兼容例外矩阵并给出原因、影响和替代方案，不能推测为已支持。

## Decisions

1. **优先 MVP，不做完整迁移**
   - 理由：源扩展核心功能较多，包含连接、ADB、项目同步、HTTP 命令入口；直接全量迁移风险高。

2. **先按文件级命令实现，而不是项目级同步**
   - 理由：当前文件 run/save 的协议和数据来源清楚；项目级 `bytes_command` 涉及 zip、md5、deletedFiles、override 语义，需设备侧验证。
   - 约束：项目级命令在 MVP 中只能标注 deferred，不允许提供 mock、假成功、空 zip 占位或与历史项目不兼容的临时协议。

3. **协议层独立于 JetBrains UI 层**
   - 理由：TCP frame codec、设备模型、命令发送应可单独测试，避免和 Action/UI 耦合。

4. **ADB 策略采用 PATH 优先，bundled Windows ADB fallback**
   - 理由：源项目内置 Windows ADB，但跨平台打包尚未确认；PATH 优先更稳妥。

5. **状态持久化只保存低风险配置**
   - 理由：MVP 只需保存最近 IP、ADB 路径、端口配置；不保存敏感凭据。

## Risks / Trade-offs

| 风险 | 级别 | 处理 |
|---|---|---|
| 未实机验证 AutoJs6 设备侧协议 | 中 | MVP 实现前应准备设备做握手和 run/save 回放验证 |
| ADB 跨平台分发不明确 | 中 | 先 PATH 优先，bundled adb 仅 Windows fallback |
| JetBrains 快捷键冲突 | 中 | 注册 Action，快捷键作为建议或默认 keymap，可由用户调整 |
| socket 生命周期泄漏 | 中 | 使用 service/disposable 生命周期，IDE/project dispose 时关闭 |
| 项目同步语义不完整 | 中高 | 本变更暂不纳入完整项目同步 |
| HTTP 端口 10347 暴露风险 | 中高 | 本变更暂不纳入 HTTP 命令入口 |
