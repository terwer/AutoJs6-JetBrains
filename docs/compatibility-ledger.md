# AutoJs6 JetBrains MVP 兼容性台账

## 不可妥协的规则

1. 协议/运行时兼容性优先：已实现的连接与脚本命令使用已观察到的端口、帧头和命令 payload；新建项目仅使用插件内置 AutoJs6 模板创建全新项目。
2. 以 JetBrains UX 保留用户习惯：熟悉的 AutoJs6 操作名称通过 JetBrains Actions、Dialogs 和 Notifications 暴露。
3. 功能集可以扩展，但不得通过声明收缩：MVP 仅声明文件级 run/save/stop/stopAll、连接模式、单文件 `AutoJs6 Script` Run Configuration 和模板创建。
4. 不做 mock/伪造/猜测：不可用流程要么报告错误，要么保持延后；不允许假成功、静态设备数据、空 zip、假 md5，或半实现的 HTTP endpoint。

## 已实现能力台账

| 能力 | VSCode 行为证据 | JetBrains 实现证据 | 验证 | 延后项 |
|---|---|---|---|---|
| Actions | `package.json` 中的 commands/menu/keybindings，涵盖 document/connect/disconnect/run/save/stop/stopAll/newProject | `src/main/resources/META-INF/plugin.xml`, `actions/AutoJs6Actions.kt` | IDE action 注册 + 单元/构建检查 | Tool Window |
| 帧协议 | `src/device.ts`：8 字节头，int32BE length + type，JSON=1，bytes=2 | `device/FrameCodec.kt`, `device/JsonCodec.kt` | `MvpUnitTest.frameCodecUsesBigEndianLengthAndType` | bytes_command 项目同步 |
| IDE 监听 | VSCode 监听 `6347` | `AutoJs6ConnectionService.startListening` 默认 `6347` | 需要真实设备/重放连接 | MVP 无 |
| IP 连接 | VSCode 连接到 AutoJs6 server `7347` | `AutoJs6ConnectionService.connectTo(host, 7347)` | 需要真实设备/重放连接 | MVP 无 |
| Hello 握手 | VSCode 要求 AutoJs6 `6.7.0` / `3591`，并发送 extension hello | `AutoJs6Device.onJson`, `sendHello`, version rejection | 需要真实设备/重放连接 | MVP 无 |
| ADB 连接 | VSCode 解析 `adb devices -l`，将本地 TCP 转发到 AutoJs6 server | `adb/AdbService.kt` 路径/PATH/Windows fallback、parser、forward | `MvpUnitTest.parsesAdbDevicesOutput`；parser 已通过单元测试；端到端 ADB forward 已用 `emulator-5560` 验证，本地 `tcp:37047` 转发到设备 `tcp:7347` 并完成 AutoJs6 hello 握手 | Windows fallback 之外的跨平台 bundled ADB 打包 |
| 运行/保存当前文件 | VSCode 从 editor 发送带 `id`、`name`、`script` 的 `run`/`save` | `RunCurrentFileAction`, `SaveCurrentFileAction` | 需要真实已连接设备/重放 | Run On Device 选择 |
| 单文件 Run Configuration | JetBrains 原生 Run UX 应保存并重复运行固定目标 | `run/AutoJs6ScriptRunConfiguration.kt`, `run/AutoJs6ScriptConfigurationProducer.kt`, `script/AutoJs6ScriptCommand.kt`, `plugin.xml` | 需要 IDE 手工验证；payload 可通过共享构造逻辑静态审查/回放测试代码验证 | 项目 Run Configuration |
| Stop/stopAll | VSCode 发送带当前文件 `id` 的 `stop`，并广播 `stopAll` | `StopCurrentScriptAction`, `StopAllScriptsAction` | 需要真实已连接设备/重放 | rerun command |
| 新项目模板 | VSCode 复制 `assets/template` 并替换占位符 | `resources/assets/template`, `AutoJs6ProjectTemplateService` | 包名规范化单元检查；生成项目检查 | 如果强制要求精确拼音，则完整拼音音译对齐需要验收 |

## 兼容性例外矩阵

| 领域 | 例外 | 原因 | 影响 | 替代方案 |
|---|---|---|---|---|
| JetBrains IDE 系列 | 不声明支持满足 `com.intellij.modules.platform` 和插件构建范围之外的 IDE-specific 支持 | MVP 仅使用 platform APIs 实现，尚未在每个 JetBrains IDE 中手动验证 | 避免虚假声明已验证每个产品/版本 | 发布声明前，通过 Plugin Verifier/手动矩阵验证目标 IDE |
| Tool Window | 未实现 | MVP 仅允许 Actions/Dialog/Notification | 没有持久设备面板 | 使用 Connect/Disconnect actions；后续提议 Tool Window |
| 项目同步 `bytes_command` | 未实现 | 需要 zip/md5/deletedFiles/override 的设备端验证 | 目前还不能运行/保存整个项目 | 未来做对齐变更；不提供空 zip/假 md5 占位 |
| 项目 Run Configuration | 未实现 | Run Project/Save Project/项目同步协议尚未完成 | `Run | Edit Configurations...` 中只出现 `AutoJs6 Script` | 后续项目运行能力完成后单独提案 |
| HTTP `/exec` | 未实现 | 超出 MVP，且存在暴露风险 | 没有 HTTP remote command endpoint | 仅在验证命令分发后再提出未来方案 |
| Marketplace/ZIP 发布 | 不声明已验证 | 发布和 IDE-family 兼容性需要单独验证 | 不做推测性分发声明 | 后续发布文档/变更 |

## 延后项防护检查

- 不存在 Tool Window 注册，因此不会显示静态/伪造设备面板。
- 未注册项目同步 action，因此不会出现空 zip、假 md5 或虚假的 `bytes_command` 成功。
- 未注册 `AutoJs6 Project` Run Configuration；`plugin.xml` 仅注册单文件 `AutoJs6 Script`。
- 不存在 HTTP server 代码或 `/exec` route。
- Script commands 仅在 `connectedDevices()` 非空时调用 `sendCommand`；否则显示错误。
- 连接失败和 ADB 不可用时报告错误，而不是进入 connected/success 状态。



