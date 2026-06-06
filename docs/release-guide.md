# AutoJs6 JetBrains 发布指南（JetBrains Marketplace / 插件中心）

本指南用于把 `AutoJs6` 插件发布到 JetBrains Marketplace（国内常说的 IDEA 插件中心）。本项目的发布口径是 **面向 JetBrains 全系列 IDE**，不是只面向 IntelliJ IDEA；每次公开发布前必须用 `docs/release-compatibility-matrix.md` 记录已验证产品和仍需验证的产品。

## 0. 发布元信息

| 项 | 值 |
|---|---|
| 插件名称 | `AutoJs6` |
| 插件 ID | `org.autojs.autojs6.jetbrains` |
| 仓库地址 | `https://github.com/terwer/AutoJs6-JetBrains` |
| 官网 / Vendor URL | `https://terwer.space` |
| 维护邮箱 | `youweics@163.com` |
| 目标 IDE | JetBrains IDE family，build `242+`（2024.2+），详见兼容矩阵 |
| Marketplace 目标 | JetBrains Marketplace / Plugins Repository |
| 分发物 | `build/distributions/AutoJs6-JetBrains-<version>.zip` |

> 说明：插件描述符使用 `<depends>com.intellij.modules.platform</depends>`，目标是依赖 IntelliJ Platform 的通用平台能力，避免把发布页写成“仅 IntelliJ IDEA”。但“全系列支持”的公开结论必须以 Plugin Verifier + 手工 smoke matrix 为证据。

## 1. 注册 JetBrains Marketplace 发布账号

1. 使用维护邮箱 `youweics@163.com` 注册或登录 JetBrains Account：
   - `https://account.jetbrains.com/login`
2. 打开 Marketplace：
   - `https://plugins.jetbrains.com/`
3. 登录后进入个人头像菜单，找到插件上传入口（通常为 **Upload Plugin** / **Add Plugin**）。
4. 首次发布插件时按页面提示完成：
   - 接受 JetBrains Marketplace Developer Agreement。
   - 创建或选择 Vendor profile。
   - 建议 Vendor 名称使用 `terwer` 或你在 Marketplace 上长期使用的发布者名称。
   - Vendor 官网填写 `https://terwer.space`。
   - 联系邮箱填写 `youweics@163.com`。
5. 如果以后团队多人维护，可在 Marketplace 的 Vendor/Organization 设置中再添加成员；不要共享个人密码或 token。

## 2. 发布前准备 Marketplace 素材

### 2.1 插件页面信息

| 字段 | 建议内容 |
|---|---|
| Name | `AutoJs6` |
| Short description | `Run, save, stop, and sync AutoJs6 scripts/projects from JetBrains IDEs.` |
| Description | 说明它是 AutoJs6 的 JetBrains IDE 插件，可连接 Android 设备，运行/保存/停止脚本，同步 AutoJs6 项目，查看日志和创建项目模板。不要写“仅 IDEA”。 |
| Website | `https://terwer.space` |
| Source code | `https://github.com/terwer/AutoJs6-JetBrains` |
| License | GNU General Public License v3.0（GPL-3.0），与仓库 `LICENSE` 保持一致 |
| Tags | `autojs`, `autojs6`, `android`, `javascript`, `automation`, `jetbrains` |
| Vendor email | `youweics@163.com` |
| Compatible products | 面向 JetBrains IDE family；具体已验证产品以 `docs/release-compatibility-matrix.md` 为准 |

### 2.2 推荐发布页文案

```text
AutoJs6 for JetBrains IDEs connects IntelliJ Platform-based IDEs with AutoJs6 devices.
It supports LAN/ADB connection flows, script run/save/stop commands, AutoJs6 project sync,
device logs, HTTP bridge safe mode, and project templates while preserving the AutoJs6 VSCode workflow.
```

中文说明可写：

```text
AutoJs6 JetBrains 插件可在 JetBrains 全系列 IDE 中连接 AutoJs6 设备，运行、保存、停止脚本，
同步 AutoJs6 项目，查看设备日志，并基于内置模板创建 AutoJs6 项目。插件保留 AutoJs6 VSCode
扩展的使用习惯，同时采用 JetBrains Platform 的 Action、Tool Window、Run Configuration 和状态栏交互。
```

### 2.3 图标与截图

- 发布前建议准备 Marketplace 图标和 2～4 张截图：
  - Tools → AutoJs6 菜单 / Action Search。
  - 设备 Tool Window 和日志。
  - 状态栏设备选择。
  - Run Configuration（AutoJs6 Script / AutoJs6 Project）。
- 不要使用 JetBrains 产品图标、Android Studio 图标或第三方商标做插件 Logo。
- 如果暂时没有截图，也应至少准备一张真实插件 UI 截图；不要使用 mock UI。

## 3. 本地版本与仓库检查

1. 确认 `build.gradle.kts` 中的 `version` 是本次发布版本，例如：

   ```kotlin
   version = "0.1.0"
   ```

2. 确认 `src/main/resources/META-INF/plugin.xml` 中的 Vendor 信息：

   ```xml
   <vendor email="youweics@163.com" url="https://terwer.space">terwer</vendor>
   ```

3. 确认 README / 发布说明中的仓库地址均为：

   ```text
   https://github.com/terwer/AutoJs6-JetBrains
   ```

4. 确认兼容描述：
   - 允许写“JetBrains 全系列 IDE / JetBrains IDE family”。
   - 不允许只写“IDEA 插件”或“仅 IntelliJ IDEA”。
   - 不允许把未验证产品写成已验证；用兼容矩阵记录 `Target / Verified / Open`。

5. 确认 `CHANGELOG` 或 Marketplace release notes 已准备好：
   - 新增能力。
   - 修复问题。
   - 兼容范围。
   - 已知限制。
   - 回滚方式。

## 4. 构建与自动化验证

建议使用 JDK 21 构建；本项目 Gradle Kotlin DSL 中的工具链为 `21`。

```powershell
.\gradlew.bat --no-daemon --console=plain clean check buildPlugin
```

必须确认：

- `test` 通过。
- `check` 通过。
- `buildPlugin` 通过。
- `buildPlugin` 触发 `verifyPatchedPluginXmlCompatibility`。
- 生成 ZIP：

  ```text
  build/distributions/AutoJs6-JetBrains-<version>.zip
  ```

兼容描述符门禁：

- ZIP 内层插件 JAR 的 `META-INF/plugin.xml` 必须包含：

  ```xml
  <idea-version since-build="242" />
  ```

- 不得包含 `until-build`，否则 2025/2026 IDE 可能会提示 `242.* or older` 并拒绝安装。

## 5. Plugin Verifier 与全系列 IDE 兼容矩阵

基线验证命令：

```powershell
.\gradlew.bat --no-daemon --console=plain verifyPlugin
```

全系列发布前还要按 `docs/release-compatibility-matrix.md` 扩展验证范围，至少覆盖：

- IntelliJ IDEA Community / Ultimate
- WebStorm
- PyCharm
- PhpStorm
- GoLand
- RubyMine
- CLion
- Rider
- DataGrip
- DataSpell
- RustRover
- Aqua

可使用项目中的手工脚本辅助：

```powershell
.\scripts\manual\ide-family-plugin-verifier.ps1 `
  -VerifierJar "C:\tools\jetbrains-plugin-verifier\verifier.jar" `
  -PluginZip build\distributions\AutoJs6-JetBrains-<version>.zip `
  -IdeDirs "D:\JetBrains\IntelliJ IDEA 2026.1", "D:\JetBrains\WebStorm 2026.1"
```

> 如果某个 IDE 家族还没安装或无法验证，矩阵中标记为 `Open / 待验证`，不要在 Marketplace 文案中写“已验证”。

## 6. 手工 Smoke Checklist

对每个计划声明支持的 IDE，至少做以下手工检查：

1. 从磁盘安装 ZIP：
   - **Settings / Preferences → Plugins → ⚙ → Install Plugin from Disk...**
2. 重启 IDE 后确认：
   - 插件显示为 `AutoJs6`。
   - Vendor 显示 `terwer` / `youweics@163.com` / `https://terwer.space`。
3. 打开 **Tools → AutoJs6**，确认所有动作可见。
4. 通过 Action Search 搜索 `AutoJs6`，确认 18 个 VSCode parity actions 可见。
5. 打开 `.js` 文件：
   - `Run`
   - `Save`
   - `Stop`
   - `Stop All`
6. 打开包含 `project.json` 的 AutoJs6 项目：
   - `Run Project`
   - `Save Project`
   - `AutoJs6 Project` Run Configuration
7. 连接真实 AutoJs6 设备：
   - LAN / IDE listener 路径至少一种。
   - ADB 路径至少一种（Windows 可使用内置 ADB fallback）。
8. 确认 Tool Window 显示设备信息和日志。
9. 确认状态栏可显示和切换当前设备。
10. 确认 HTTP Bridge 默认是 disabled 或 loopback-safe；wide binding 只能由用户明确开启兼容模式。
11. 记录结果到 `docs/release-compatibility-matrix.md`。

## 7. 插件签名

发布到 Marketplace 前建议签名。凭据只能放在本机环境变量、本机 Gradle properties 或安全存储中，不得提交到 Git。

### 7.1 本机环境变量方式（推荐）

```powershell
$env:INTELLIJ_SIGN_CERTIFICATE_CHAIN = Get-Content "C:\secure\autojs6\chain.crt" -Raw
$env:INTELLIJ_SIGN_PRIVATE_KEY = Get-Content "C:\secure\autojs6\private.pem" -Raw
$env:INTELLIJ_SIGN_PRIVATE_KEY_PASSWORD = "你的私钥密码"
.\gradlew.bat --no-daemon --console=plain signPlugin
```

本项目已在 `build.gradle.kts` 中接入 `intellijPlatform.signing`，只读取以下本机环境变量：

```kotlin
INTELLIJ_SIGN_CERTIFICATE_CHAIN
INTELLIJ_SIGN_PRIVATE_KEY
INTELLIJ_SIGN_PRIVATE_KEY_PASSWORD
```

`INTELLIJ_SIGN_CERTIFICATE_CHAIN` 与 `INTELLIJ_SIGN_PRIVATE_KEY` 可以是原始 PEM 内容，也可以是 Marketplace ZIP Signer 可识别的 base64 内容。签名后的 ZIP 通常会在 `build/distributions/` 下生成带 `-signed` 后缀的文件。上传 Marketplace 时优先选择签名 ZIP。

### 7.2 凭据规则

- 不提交私钥。
- 不提交证书链中的私钥材料。
- 不提交 Marketplace token。
- 不把 token 写到 README、issue、日志截图或 CI 输出。

## 8. 首次手动上传到 JetBrains Marketplace

首次发布建议手动上传，因为 JetBrains 官方流程要求新插件先在 Marketplace 页面创建并提交审核。

1. 打开：`https://plugins.jetbrains.com/`
2. 登录 JetBrains Account。
3. 进入 **Upload Plugin** / **Add Plugin**。
4. 选择 Vendor profile（推荐 `terwer`）。
5. 上传 ZIP：
   - 优先：`build/distributions/AutoJs6-JetBrains-<version>-signed.zip`
   - 如果未签名且 Marketplace 当前允许：`build/distributions/AutoJs6-JetBrains-<version>.zip`
6. 填写插件元信息：
   - Name：`AutoJs6`
   - Repository / Source Code：`https://github.com/terwer/AutoJs6-JetBrains`
   - Website：`https://terwer.space`
   - Email：`youweics@163.com`
   - License：GNU General Public License v3.0（GPL-3.0），与仓库一致
   - Tags：见第 2 节
7. 填写 release notes / change notes。
8. 选择发布渠道：
   - 首发建议先使用 hidden / private review（如果页面提供）。
   - 通过自测和审核后再公开。
9. 检查 Compatible products：
   - 应来自插件描述符和依赖计算。
   - 口径为 JetBrains IDE family build `242+`。
   - 如果页面要求逐个产品声明，只勾选已经在矩阵中完成 verifier/smoke 的产品；未验证产品留待后续版本或说明为目标支持。
10. 提交审核。
11. 等待 JetBrains Marketplace 审核、自动验证和人工反馈。
12. 若被拒：不要直接重复提交；按拒绝原因修改文档、描述符、图标、签名或兼容范围后重新构建上传。

## 9. 后续版本使用 Gradle 发布（可选）

首次插件在 Marketplace 创建并通过后，可考虑用 Gradle 自动上传后续版本。

1. 在 Marketplace 用户设置中创建发布 token。
2. 本地设置环境变量：

   ```powershell
   $env:INTELLIJ_PLATFORM_PUBLISH_TOKEN = "你的 Marketplace token"
   ```

3. 本项目已在 `build.gradle.kts` 中接入 `intellijPlatform.publishing`，只读取 `INTELLIJ_PLATFORM_PUBLISH_TOKEN`；没有该环境变量时不影响 `test/check/buildPlugin`。

4. 执行：

   ```powershell
   .\gradlew.bat --no-daemon --console=plain publishPlugin
   ```

5. 发布后立即检查 Marketplace 页面、兼容产品、下载 ZIP、安装 smoke 和用户反馈。

> 当前仓库已经只通过环境变量读取发布 token；不要提交真实 token，缺少该环境变量也不会影响 `test/check/buildPlugin`。

## 10. 私有 ZIP 分发与回滚

不通过 Marketplace 时，可以分发本地 ZIP：

- 只分发 `build/distributions/` 下的 ZIP。
- 同时提供版本号、commit hash、JDK/Gradle 版本、目标 IDE build、已知限制。
- 安装方式：**Settings / Preferences → Plugins → ⚙ → Install Plugin from Disk...**。
- 保留上一个 ZIP 作为回滚版本。

回滚步骤：

1. 在 IDE 插件设置中 Disable 或 Uninstall 当前版本。
2. 重启 IDE。
3. 从磁盘安装上一个 ZIP。
4. 重启后执行 smoke checklist。
5. 在 release notes / issue 中记录回滚原因。

## 11. 发布阻断条件

出现以下任一情况，不要公开发布或不要宣传为全系列支持：

- `check` / `buildPlugin` / `verifyPlugin` 失败。
- ZIP 描述符仍包含 `until-build="242.*"` 或任何不符合预期的上限。
- `plugin.xml` 中 Vendor 邮箱、官网或仓库链接不正确。
- README / Marketplace 描述仍出现旧插件仓库 `niceSilentSam/AutoJs6-JetBrains`。
- 公开文案只写“IDEA 插件”而忽略 JetBrains 全系列。
- 未验证 IDE 被写成“已验证支持”。
- 项目同步、设备连接、HTTP Bridge 使用 mock/fake 成功结果。
- Marketplace 图标/截图/描述使用第三方商标或不真实 UI。
- 私钥、证书密码或 Marketplace token 泄露到仓库、日志或截图。

## 12. 官方参考

- Publishing a Plugin: https://plugins.jetbrains.com/docs/intellij/publishing-plugin.html
- Uploading a New Plugin: https://plugins.jetbrains.com/docs/marketplace/uploading-a-new-plugin.html
- Plugin Signing: https://plugins.jetbrains.com/docs/intellij/plugin-signing.html
- Verifying Plugin Compatibility: https://plugins.jetbrains.com/docs/intellij/verifying-plugin-compatibility.html
- Plugin Compatibility with IntelliJ Platform Products: https://plugins.jetbrains.com/docs/intellij/plugin-compatibility.html
- JetBrains Marketplace Approval Guidelines: https://plugins.jetbrains.com/docs/marketplace/jetbrains-marketplace-approval-guidelines.html
