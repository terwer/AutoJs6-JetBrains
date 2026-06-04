# AutoJs6 JetBrains 用户自行发布指南（草案）

> 本文档是 `complete-autojs6-vscode-parity` 的发布交付要求草案。实现阶段必须把其中命令替换为项目实际 Gradle task、插件 ID、版本号和截图/检查结果。禁止把未验证步骤标记为完成。

## 1. 发布原则

- 发布账号、Marketplace token、签名私钥、证书链都由用户自己持有和执行，项目不得内置任何发布凭据。
- 发布前必须通过四规则审计：历史项目 100% 运行兼容、保留用户习惯、功能只增不减、无 mock/fake/推测完成。
- 发布前必须通过 JetBrains IDE 全家桶兼容矩阵；个别 IDE 如无法支持，必须进入例外矩阵并说明证据、原因、影响和替代方案。
- 发布文档必须同时覆盖 Marketplace 发布和私有 ZIP 分发。

## 2. 发布前准备

1. 确认版本号：
   - `plugin.xml` / Gradle metadata 中的插件版本已更新。
   - changelog 包含新增、修复、兼容性、已知限制。
2. 确认兼容范围：
   - `since-build` / `until-build` 或 Gradle patchPluginXml 配置符合目标版本策略。
   - 依赖只声明必要 IntelliJ Platform modules，避免无意绑定 IntelliJ IDEA 专属 API。
3. 确认资源：
   - icons、docs、AutoJs6 模板、ADB fallback 策略、license、README 完整。
4. 确认安全：
   - HTTP bridge 默认关闭或 loopback。
   - 发布包不含 token、私钥、用户路径、调试日志。

## 3. 本地构建 ZIP

```powershell
./gradlew clean buildPlugin
```

预期产物：

```text
build/distributions/<plugin-name>-<version>.zip
```

本地安装验证：

1. 打开任一目标 JetBrains IDE。
2. `Settings | Plugins | ⚙ | Install Plugin from Disk...`
3. 选择 `build/distributions/*.zip`。
4. 重启 IDE。
5. 执行 smoke test：插件加载、Action 可搜索、连接配置页可打开、不会报启动异常。

## 4. Plugin Verifier 兼容验证

实现阶段应配置 Gradle Plugin Verifier，并针对兼容矩阵中的每个 IDE/build 执行验证。

示例命令：

```powershell
./gradlew verifyPlugin
```

验收要求：

- 不允许存在阻断级 binary compatibility 问题。
- 每个目标 IDE/build 保存验证日志。
- 如某个 IDE 不兼容，必须写入例外矩阵：IDE、版本、失败证据、原因、影响、替代方案、是否阻断发布。

## 5. 插件签名

发布到 Marketplace 前应准备签名配置。凭据只能通过环境变量、Gradle properties 或本地安全存储传入，禁止提交到仓库。

需要准备：

- certificate chain
- private key
- private key password
- Marketplace publish token

示例环境变量名称：

```powershell
$env:CERTIFICATE_CHAIN='...'
$env:PRIVATE_KEY='...'
$env:PRIVATE_KEY_PASSWORD='...'
$env:PUBLISH_TOKEN='...'
```

签名构建：

```powershell
./gradlew signPlugin
```

## 6. Marketplace 发布（用户自己执行）

1. 用户登录 JetBrains Marketplace。
2. 创建或确认插件条目、插件 ID、说明、图标、license、vendor 信息。
3. 本地执行最终检查：

```powershell
./gradlew clean buildPlugin verifyPlugin signPlugin
```

4. 上传方式二选一：
   - 网页手动上传签名后的 ZIP。
   - 使用 Gradle `publishPlugin`，token 由用户本地环境变量提供。

```powershell
./gradlew publishPlugin
```

5. 提交审核后记录：
   - 上传 ZIP 文件名和 hash。
   - 版本号。
   - changelog。
   - Marketplace 审核状态。
   - 审核反馈和处理记录。

## 7. 私有 ZIP 分发

1. 执行：

```powershell
./gradlew clean buildPlugin verifyPlugin
```

2. 将 `build/distributions/*.zip` 放入私有发布位置。
3. 提供给用户的安装步骤：
   - `Settings | Plugins | ⚙ | Install Plugin from Disk...`
   - 选择 ZIP。
   - 重启 IDE。
4. 提供回滚步骤：
   - `Settings | Plugins` 禁用或卸载当前版本。
   - 安装上一版本 ZIP。
   - 重启 IDE。

## 8. JetBrains 全家桶兼容矩阵要求

默认目标：所有满足所需 IntelliJ Platform modules 的 JetBrains IDE 都应兼容，不能只绑定 IDEA。

矩阵至少记录：

| IDE | 版本/build | 是否验证 | 结果 | 例外原因 | 替代方案 |
|---|---|---:|---|---|---|
| IntelliJ IDEA | TBD | 否 | TBD | - | - |
| WebStorm | TBD | 否 | TBD | - | - |
| PyCharm | TBD | 否 | TBD | - | - |
| PhpStorm | TBD | 否 | TBD | - | - |
| GoLand | TBD | 否 | TBD | - | - |
| CLion | TBD | 否 | TBD | - | - |
| RubyMine | TBD | 否 | TBD | - | - |
| DataGrip | TBD | 否 | TBD | - | - |
| Rider | TBD | 否 | TBD | - | - |
| DataSpell | TBD | 否 | TBD | - | - |
| Android Studio / IntelliJ-based IDE | TBD | 否 | TBD | 如非 JetBrains Marketplace 目标，仍需单独说明 | TBD |

## 9. 发布阻断条件

任一条件满足即禁止发布：

- VSCode parity 矩阵存在未解释缺口。
- compatibility ledger 中存在历史项目运行不兼容。
- 存在 mock/fake/stub 成功路径。
- Plugin Verifier 存在阻断级兼容问题。
- 发布包含凭据或用户本地隐私路径。
- JetBrains 全家桶兼容声明没有证据，且没有例外矩阵。
- Marketplace 文档、私有 ZIP 安装步骤、回滚步骤不完整。

## 10. 参考官方文档

- IntelliJ Platform Gradle Plugin 2.x: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin.html
- Publishing a Plugin: https://plugins.jetbrains.com/docs/intellij/publishing-plugin.html
- Plugin Signing: https://plugins.jetbrains.com/docs/intellij/plugin-signing.html
- Verifying Plugin Compatibility: https://plugins.jetbrains.com/docs/intellij/verifying-plugin-compatibility.html
- Plugin Compatibility with IntelliJ Platform Products: https://plugins.jetbrains.com/docs/intellij/plugin-compatibility.html
- Marketplace Approval Guidelines: https://plugins.jetbrains.com/docs/marketplace/jetbrains-marketplace-approval-guidelines.html
