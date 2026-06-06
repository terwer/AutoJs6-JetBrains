# Progress - JetBrains 运行后主脚本丢失

## 2026-06-06
- 新建排查计划 $planId。
- 读取并遵循 planning-with-files-zh 技能要求。
- 已记录用户截图中的关键报错：远程项目缓存目录内 main.js 不存在。

- 补充记录用户新反馈：单文件运行正常；项目模式运行一次后再次运行报错。因此后续优先排查项目模式的二次运行、增量上传和缓存/删除逻辑。

- 完成第一轮被动结构检查：确认是 Gradle/Kotlin JetBrains 插件，入口源目录和模板资源位置已定位；未改业务代码。

- 阅读 AutoJs6ProjectSyncService.kt 与项目同步相关测试；形成首个高置信候选根因：二次运行增量 zip + override=true 组合可能导致设备端远程项目目录被重建后缺失未修改的 main.js。

- 对比了本地 VSCode 扩展 device.ts/project.ts/diff.ts；确认协议字段表面一致，但 VSCode 文件观察状态存在“未变更文件不写回新状态”的差异，可能影响后续每轮 zip 内容。
- 尝试网页搜索 AutoJs6 Android 端 bytes_command/run_project/remote_project 代码，公开搜索未直接命中，下一步准备拉取/检查 Android 源码或使用本地脚本模拟二次运行 payload。

- 阅读项目/脚本 RunProfileState 与 action 支持代码；确认项目运行不像单文件运行那样读取编辑器文档，也未显式保存文档。根因从单点扩展为两个叠加问题：未保存文档导致空 diff，增量 zip + override true 导致远端目录缺入口。

- 已应用最小修复补丁：项目运行前保存全部文档；override=true 时发送完整当前项目 zip；新增二次运行回归测试覆盖“非入口文件变化”和“无文件变化”两类触发 main.js 缺失的场景。

- 验证尝试 1：运行 ./gradlew.bat test --tests org.autojs.autojs6.jetbrains.MvpUnitTest --no-daemon，120s 超时未返回。下一步不重复同样盲跑，先检查 Gradle/测试输出与进程状态，再决定是否延长超时或跑更小范围。


- 验证尝试 2：延长超时并强制重新执行 ./gradlew.bat cleanTest test --tests org.autojs.autojs6.jetbrains.MvpUnitTest --rerun-tasks --no-daemon --console=plain，构建成功。
- 验证尝试 3：运行完整 ./gradlew.bat test --no-daemon --console=plain，构建成功；测试结果 MvpUnitTest 共 23 个测试，failures=0、errors=0、skipped=0。


- 清理临时外部源码目录：未保留超时失败的 AutoJs6 partial clone，仅保留本次排查计划与补丁。

- 质量检查：运行 git diff --check 通过，无 whitespace error；PowerShell/Git 提示 LF 将在下次 Git 触碰时转 CRLF，为仓库本地换行配置提示。

- 最终验证：运行 ./gradlew.bat check --no-daemon --console=plain 成功，包含 verifyPatchedPluginXmlCompatibility 与 test。

- 交付验证：运行 ./gradlew.bat buildPlugin --no-daemon --console=plain 成功，已生成 build/distributions 下的插件包。

- 应用户要求补充版本递增：将插件版本从 0.1.0 提升到 0.1.1；同步更新 runtime hello extensionVersion、CHANGELOG 和文档中的插件包路径/示例版本。

- 版本验证：运行 ./gradlew.bat clean check buildPlugin --no-daemon --console=plain 成功；clean 后仅生成 0.1.1 插件包。patched plugin.xml 显示 <version>0.1.1</version>。

- 按用户纠正更新 CHANGELOG：v0.1.1 仅保留用户可见修复内容，移除测试/打包验证类条目。
