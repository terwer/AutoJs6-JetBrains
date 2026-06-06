# Findings - JetBrains 运行后主脚本丢失

## 初始证据
- 用户截图显示 Android 端弹窗：无效项目。
- 具体路径：/data/user/0/org.autojs.autojs6/cache/remote_project/243ac0d42263d18b6124fe1151d5a3f7/main.js。
- 报错语义：AutoJs6 收到/解包了远程项目，但项目主脚本文件 main.js 不存在。
- 用户反馈：JetBrains 版本“运行一次改代码项目就无法运行”，VSCode 插件没有该问题。

## 待验证问题
- JetBrains 插件是否始终把入口脚本命名/映射为 main.js？
- 修改后再次运行时是否采用增量上传、缓存目录复用、删除旧文件或过滤文件？
- VSCode 插件运行协议/文件列表/入口字段与 JetBrains 是否一致？

## 补充用户反馈
- 单文件运行没有问题。
- “项目运行”第一次可运行；运行一次之后，再修改代码/再次运行项目就提示 main.js 不存在。
- 这把排查范围进一步收窄到“项目模式”的第二次运行/增量同步/缓存清理/入口文件生成逻辑，而不是单文件运行协议。

## 项目结构初探
- 仓库是 Gradle Kotlin JetBrains 插件项目，主要代码在 src/main/kotlin/org/autojs/autojs6/jetbrains。
- 资源中存在项目模板：src/main/resources/assets/template/main.js、project.json、package.json 等。
- 测试资源存在 src/test/resources/protocol-fixtures，说明项目已有通信协议夹具，后续可用于对比/回归。
- 当前 git 只显示新建的 .planning 变更，尚未改业务代码。

## JetBrains 项目同步链路初步结论
- 核心实现：src/main/kotlin/org/autojs/autojs6/jetbrains/project/AutoJs6ProjectSyncService.kt。
- sendProjectCommand() 每次构造 buildPayload(root, device.key())，先 sendBytes(zipBytes)，再 sendBytesCommand(md5, commandData(command))。
- 状态键为 oot绝对路径 + device.key()；第二次及以后 state.syncedOnce=true。
- buildPayloadWithState() 只把 mtime 改变的文件加入 zip；未变更文件不进入 zip。
- 当前实现第二次及以后设置 override = state.syncedOnce，即第二次增量 zip 会带 override=true。
- 这和用户报错高度吻合：如果设备端把 override=true 理解为“覆盖/重建远端项目目录”，但 JetBrains 只发送了增量 zip，那么没有变化的入口 main.js 不会被解包到新目录，Android 端运行时就报 main.js 不存在。
- 现有测试 projectSyncBuildsZipMd5IgnoresAndTracksDeletedFiles() 只验证“第二次修改 main.js 时 zip 含 main.js”，没有覆盖“第二次只修改非入口文件时 main.js 仍需存在”的真实失败场景。
- 现有 frame 测试只验证 bytes-before-json 和 md5，不验证第二次 zip entries/设备端目录语义。

## VSCode 插件对比发现
- 本地 VSCode 插件源码位于 D:/Users/Administrator/Documents/myproject/AutoJs6-VSCode-Extension，与 docs/vscode-parity-matrix.md 记录一致。
- VSCode src/device.ts:343-364：每次项目命令也是先 sendBytes(result.buffer)，再发送 	ype='bytes_command'，字段包含 deletedFiles、override: result.full、command。
- VSCode src/project.ts:110-155：ProjectObserver.diff() 首次 full=false，之后 full=true，字段名 full 实际代表“已经 diff 过一次”。
- VSCode src/diff.ts:54-85 的 FileObserver 有一个重要差异：遇到未修改文件时，从 old map 删除后直接 return，没有把未修改文件写回新的 	his.files。这会导致 VSCode 的观察状态不是完整当前文件集，而 JetBrains 目前会完整保存 current。
- JetBrains 当前实现比 VSCode “更正确地保存完整状态”，但这可能偏离 VSCode 的实际设备兼容行为；需要结合 AutoJs6 端 bytes_command 对 override 的语义继续确认。

## 新增候选根因：JetBrains 项目运行未保存编辑器文档
- AutoJs6ActionSupport.sendProjectCommand() 直接解析项目根并调用后台同步，没有调用 FileDocumentManager.saveAllDocuments()。
- AutoJs6ProjectRunProfileState.executeRun() 也直接从磁盘读取项目文件，没有显式保存文档。
- 单文件运行正常的原因：payloadFromVirtualFile() 优先使用当前编辑器 Document.text，可以发送未保存内容。
- 项目运行异常的原因之一：项目同步完全基于磁盘 Files.walk/getLastModifiedTime/newInputStream。如果用户运行一次后在 IDE 中改了 main.js 但未保存，第二次项目同步看到的是“无文件变化”，会发送空 zip + override=true，非常容易触发设备端远程项目目录缺 main.js。
- 即便用户保存了非入口文件，当前增量 zip + override=true 也可能导致入口 main.js 缺失。因此修复应同时覆盖：运行前保存文档 + override=true 时发送完整当前项目 zip。

## 已实施最小修复
- AutoJs6ProjectSyncService.buildPayloadWithState()：当 override=true（第二次及以后）时，zip 不再只包含 mtime 变化文件，而是包含当前项目内全部未忽略文件；modifiedFiles/deletedFiles 仍按 diff 计算。
- AutoJs6ActionSupport.sendProjectCommand()：项目 action 运行/保存前调用 FileDocumentManager.getInstance().saveAllDocuments()，确保编辑器未保存改动落盘后再按 mtime/内容打包。
- AutoJs6ProjectRunProfileState.executeRun()：Run Configuration 项目运行前也调用 saveAllDocuments()。
- 新增两条回归测试：
  - 第二次只修改非入口文件时，override payload 的 zip 仍包含 project.json、main.js 和修改文件。
  - 第二次无文件变化时，不再发送空 zip，而是发送包含 project.json、main.js 的当前项目 zip。



## 验证结果
- ./gradlew.bat cleanTest test --tests org.autojs.autojs6.jetbrains.MvpUnitTest --rerun-tasks --no-daemon --console=plain：成功。
- ./gradlew.bat test --no-daemon --console=plain：成功；MvpUnitTest 23 tests，0 failures，0 errors。
- git diff --check：通过。
- ./gradlew.bat check --no-daemon --console=plain：成功。

## 交付产物
- 插件包：build/distributions/AutoJs6-JetBrains-0.1.0.zip。

## 版本修正
- 修复类变更必须递增版本，已从 0.1.0 调整为 0.1.1。
- 版本来源 build.gradle.kts 已更新，AutoJs6Constants.PLUGIN_VERSION 与 hello extensionVersion 同步为 0.1.1。

## 0.1.1 验证结果
- ./gradlew.bat clean check buildPlugin --no-daemon --console=plain：成功。
- build/distributions/AutoJs6-JetBrains-0.1.1.zip：已生成。
- build/tmp/patchPluginXml/plugin.xml：<version>0.1.1</version>。
