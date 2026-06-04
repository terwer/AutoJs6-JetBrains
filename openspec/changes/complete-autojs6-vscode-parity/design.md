## Context

`add-autojs6-jetbrains-mvp` 建立了 JetBrains 插件的最小闭环：连接设备、运行/保存当前文件、停止脚本、新建项目。源 VSCode 扩展的真实能力更大，入口来自 `package.json` 的 18 个命令、editor/title 与 explorer/context 菜单、F6/F8/组合快捷键、JavaScript breakpoint 贡献，以及 `extension.ts`/`device.ts`/`project.ts`/`diff.ts`/`adb.ts` 中的协议和项目同步实现。

本设计面向 MVP 之后的全面替代，不推翻 MVP，而是在 MVP 基础上补齐 VSCode parity。源扩展没有 `languages`/`grammars`/`configuration` 贡献点，因此“100% 替代 VSCode 插件”首先指设备调试工具链、命令入口和项目同步等行为等价，而不是重建 JavaScript 语言插件。

## Goals / Non-Goals



## Non-Negotiable Compatibility Rules

1. **100% historical runtime compatibility:** the parity release SHALL run existing AutoJs6 projects using existing `project.json`, template layout, ignore semantics, path/id/name/script fields, `bytes_command`, md5, deletedFiles, override, ports, and frame payload formats without requiring users to migrate old projects.
2. **Habit preservation with JetBrains best practice:** users coming from the VSCode extension SHALL find equivalent command names, action intent, connection choices, project context actions, keymap recommendations, and output/log concepts, while implementation follows JetBrains Action System, Tool Window, Settings, background task, notification, and Disposer lifecycle practices.
3. **Additive feature policy:** JetBrains-native diagnostics, Tool Window, action search, safer HTTP controls, and publishing docs are additive; they must not replace, remove, or silently weaken required VSCode parity behavior.
4. **Evidence over claims:** no mock/fake/stub behavior may satisfy a task or requirement. Each completed parity row must cite automated tests, protocol replay, runIde verification, or real AutoJs6/ADB/manual evidence. Unknowns must stay open or blocked.
**Goals:**

- 以 VSCode 扩展 `package.json` 和运行时代码为基准，建立可测试的 parity 矩阵。
- 补齐所有用户可见命令、上下文菜单、建议快捷键和错误提示路径。
- 补齐 AutoJs6 项目同步协议：差量遍历、zip、md5、deletedFiles、override、`bytes_command`。
- 提供设备 Tool Window、日志输出、连接诊断和状态栏，让 JetBrains 版本比 VSCode 版本更易用。
- 保持协议层、项目层、UI 层可单独测试，避免一次性大爆炸实现。

**Non-Goals:**

- 不在本提案中变更 AutoJs6 App 设备侧协议；默认兼容现有 VSCode 扩展协议。
- 不把 JetBrains 版本绑定为单一 IDE；目标是 JetBrains IDE 全家桶通用支持。实现必须优先使用通用 IntelliJ Platform API 和最小必要 module 依赖；个别 IDE 如确实无法支持，必须进入兼容例外矩阵并说明原因、影响、替代方案和验证证据。
- 不承诺实现完整 JavaScript 语言服务、语义补全或 AutoJs6 API 类型系统；可作为后续独立提案，但不得降低 JetBrains IDE 已有 JavaScript 使用体验。
- 不默认开放 HTTP 远程命令入口；必须提供安全开关、绑定地址和风险提示。

## Decisions

1. **以 parity 矩阵驱动，而不是凭记忆补功能**
   - 决策：建立 `VSCode command/menu/keybinding/source behavior → JetBrains action/UI/test` 映射表。
   - 理由：目标是 100% 替代，必须逐项验收，避免遗漏 `rerun`、`newUntitledFile`、指定设备动作等边缘入口。
   - 备选：按模块自由实现。放弃原因：容易只实现常用路径，无法证明替代性。

2. **协议层独立为可测试 core**
   - 决策：frame codec、JSON/bytes payload、device model、command dispatch、project bytes_command 作为 UI 无关核心模块。
   - 理由：项目同步和 HTTP/反向 command 都依赖协议正确性，必须能用单元测试和录制数据回放验证。

3. **项目同步先做行为兼容，再优化 watcher**
   - 决策：先复刻源扩展 `FileObserver` 的 mtime 差量语义、ignore 过滤和 zip/md5 格式；后续再引入增量 watcher 优化。
   - 理由：设备侧预期由现有协议决定，过早改变语义会破坏兼容。

4. **JetBrains UX 使用 Tool Window + Action Search 双入口**
   - 决策：保留所有 Action 入口，同时提供 AutoJs6 Tool Window 集中展示设备、日志、项目、快捷动作。
   - 理由：Action 保障 parity，Tool Window 提供 JetBrains 版本自有便利点。

5. **HTTP 远程命令默认安全收敛**
   - 决策：实现 `/exec` parity，但默认绑定 loopback 或默认关闭；用户显式开启才绑定 `0.0.0.0`。
   - 理由：源扩展直接监听 `0.0.0.0:10347`，JetBrains 版本应保留兼容能力但降低误暴露风险。

## Risks / Trade-offs

| 风险 | 影响 | 缓解 |
|---|---|---|
| 设备侧协议未实机覆盖所有命令 | project/http/反向 command 可能不兼容 | 使用 AutoJs6 实机、ADB 和协议录制回放作为验收门禁 |
| HTTP 兼容与安全冲突 | 默认安全设置可能与 VSCode 行为不同 | 提供显式兼容模式开关，并在 parity 矩阵中记录差异 |
| JetBrains 快捷键冲突 | 默认 keymap 可能不可用 | 注册建议快捷键并允许用户覆盖；验收以 Action 可达为准 |
| 项目同步大文件性能 | zip/md5 可能阻塞 UI | 后台任务、进度、取消、大小限制和输出日志 |

## Migration Plan

1. 先完成 MVP 并归档/稳定其 capability。
2. 建立 parity 矩阵和协议回放样本，作为本提案实施的入口任务。
3. 分阶段实现：Action parity → Tool Window/logs → project sync → advanced connection → remote bridge → release documentation → JetBrains family compatibility。
4. 每阶段用 `runIde`、单元测试、集成测试和 AutoJs6 设备实测更新矩阵。
5. 若某阶段失败，允许关闭对应增强 capability，不影响 MVP 功能；已启用的 HTTP/ADB/Socket 资源必须可 dispose 清理。关闭或延期必须在 parity 矩阵中标注，不得以 mock/fake/推测状态冒充完成。

## Confirmed Requirements

- 目标是 VSCode 扩展全量功能对齐，不新增独立自研功能范围来替代 parity。
- 需要发布能力，但由用户自行发布；项目必须提供完整发布文档 `release-guide.md`，覆盖版本准备、构建、签名、Plugin Verifier、Marketplace 上传、私有 ZIP 分发、回滚和问题排查步骤。
- 目标 IDE 为 JetBrains IDE 全家桶通用支持，禁止绑定某一个 IDE；如个别 IDE 确实无法兼容，必须列入例外矩阵并给出证据、原因、影响和替代方案。
