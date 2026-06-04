> [!WARNING]
> **⚠️ 积极开发中** — 本插件目前处于积极开发阶段。功能可能尚未完善，API 可能发生变更，且可能存在已知或未知的 Bug。请自行承担使用风险。待项目达到稳定版本后，此提示将被移除。

<p align="center">
  <a href="https://github.com/niceSilentSam/AutoJs6-JetBrains">
    <img src="https://img.shields.io/github/stars/niceSilentSam/AutoJs6-JetBrains?style=social" alt="Stars" />
  </a>
  <a href="https://github.com/niceSilentSam/AutoJs6-JetBrains/blob/main/LICENSE">
    <img src="https://img.shields.io/github/license/niceSilentSam/AutoJs6-JetBrains" alt="License" />
  </a>
  <a href="https://github.com/niceSilentSam/AutoJs6-JetBrains/releases">
    <img src="https://img.shields.io/github/v/release/niceSilentSam/AutoJs6-JetBrains?include_prereleases" alt="Release" />
  </a>
  <img src="https://img.shields.io/badge/Kotlin-2.0.21-blue?logo=kotlin" alt="Kotlin" />
  <img src="https://img.shields.io/badge/IntelliJ_Platform-2024.2+-black?logo=jetbrains" alt="IntelliJ Platform" />
  <img src="https://img.shields.io/badge/JVM-17-orange?logo=openjdk" alt="JVM 17" />
</p>

<p align="center">
  <a href="https://docs.autojs6.com/">
    <img src="https://img.shields.io/badge/docs-AutoJs6-blue" alt="Docs" />
  </a>
  <img src="https://img.shields.io/badge/AutoJs6-≥6.7.0-green" alt="AutoJs6 Version" />
</p>

<h1 align="center">AutoJs6 JetBrains 插件</h1>

<p align="center">
  <strong>AutoJs6</strong> 的 JetBrains IDE 插件 —— 在 IntelliJ IDEA / WebStorm / PyCharm 等全系列 JetBrains IDE 中连接 Android 设备，一键运行、保存、停止脚本，并快速创建 AutoJs6 项目模板。
</p>

<p align="center">
  <a href="README.md">English</a> | <a href="README_zh_CN.md">简体中文</a>
</p>

---

## ✨ 特性

| 特性 | 说明 |
|---|---|
| 🔌 **多模式连接** | IDE 监听（端口 `6347`）、IP 直连（端口 `7347`）、ADB 连接 —— 三种方式连接 AutoJs6 设备 |
| ▶️ **运行脚本** | 快捷键一键将当前编辑的脚本发送到所有已连接设备运行 |
| 💾 **保存到设备** | 将当前脚本推送到所有已连接设备 |
| ⏹️ **停止脚本** | 停止当前脚本或一键停止所有运行中的脚本 |
| 📁 **项目模板** | 基于内置历史模板快速创建 AutoJs6 项目 |
| 📖 **在线文档** | 从 IDE 内直接跳转 [docs.autojs6.com](https://docs.autojs6.com/) |
| ⌨️ **键盘优先** | 完整的快捷键支持 —— 无需离开 IDE 即可完成全部操作 |

## 🚀 快速开始

### 环境要求

- **JetBrains IDE** — IntelliJ IDEA / WebStorm / PyCharm 等全系列 JetBrains IDE（build `242+`）
- **JDK 17+**
- **AutoJs6 应用** — 版本 `≥ 6.7.0`（版本号 `≥ 3591`），已安装在 Android 设备上
- 设备与电脑处于 **同一网络**（IP / 监听模式）或通过 **USB + ADB** 连接

### 安装

```bash
# 克隆并构建插件
git clone https://github.com/niceSilentSam/AutoJs6-JetBrains.git
cd AutoJs6-JetBrains
./gradlew buildPlugin
```

构建完成后，插件压缩包位于 `build/distributions/` 目录。通过以下方式安装：

> **Settings → Plugins → ⚙️ → Install Plugin from Disk...**

### 连接设备

1. 打开菜单 **Tools → AutoJs6**（或使用下方快捷键）
2. 选择连接方式：
   - **IDE 监听客户端连接** — 在端口 `6347` 启动监听服务，AutoJs6 应用主动连接到 IDE
   - **通过 IP 连接服务端** — 输入设备 IP，IDE 主动连接 AutoJs6 服务端端口 `7347`
   - **通过 ADB 连接** — 从 ADB 设备列表中选择 USB 连接的设备

### 运行脚本

1. 在编辑器中打开任意 `.js` 文件
2. 按下 <kbd>F6</kbd> —— 脚本即刻在所有已连接设备上运行

## ⌨️ 快捷键

| 操作 | 快捷键 |
|---|---|
| 建立设备连接 | <kbd>Ctrl</kbd> + <kbd>Alt</kbd> + <kbd>F6</kbd> |
| 断开所有连接 | <kbd>Ctrl</kbd> + <kbd>Alt</kbd> + <kbd>Shift</kbd> + <kbd>F6</kbd> |
| 运行当前脚本 | <kbd>F6</kbd> |
| 停止当前脚本 | <kbd>Ctrl</kbd> + <kbd>F6</kbd> |
| 停止所有脚本 | <kbd>Ctrl</kbd> + <kbd>Shift</kbd> + <kbd>F6</kbd> |
| 新建 AutoJs6 项目 | <kbd>Ctrl</kbd> + <kbd>Alt</kbd> + <kbd>6</kbd>，然后 <kbd>N</kbd> |

## 🏗️ 项目架构

```
org.autojs.autojs6.jetbrains
├── actions    # IDE Actions — 所有用户命令的 UI 入口
├── adb        # ADB 集成 — 设备发现与端口转发
├── device     # 核心网络层 — Socket 连接与二进制协议
└── project    # 项目脚手架 — 基于模板的项目创建
```

### 通信协议

插件使用基于 TCP 的自定义二进制分帧协议：

| 组成部分 | 详情 |
|---|---|
| 帧头大小 | 8 字节 |
| 载荷类型 | `1` = JSON，`2` = 字节流 |
| 最大帧大小 | 64 MB |
| 握手方式 | 双向 `hello` 消息，含版本协商 |
| 最低应用版本 | `6.7.0`（版本号 `3591`）—— 低于此版本将拒绝连接 |

### 端口说明

| 端口 | 用途 |
|---|---|
| `6347` | IDE 监听端口 —— IDE 接受设备的传入连接 |
| `7347` | 服务端端口 —— AutoJs6 应用监听 IDE 的连接 |
| `10347` | HTTP 服务端口 |
| `20347` | ADB 服务端口 |

## 🛠️ 开发指南

```bash
# 在沙箱 IDE 实例中运行插件
./gradlew runIde

# 构建插件发布包
./gradlew buildPlugin

# 运行测试
./gradlew test

# 验证插件在各 IDE 版本间的兼容性
./gradlew verifyPlugin
```

### 技术栈

| 技术 | 版本 |
|---|---|
| Kotlin | 2.0.21 |
| IntelliJ Platform | 2024.2 (IC) |
| IntelliJ Platform Gradle 插件 | 2.2.1 |
| JVM 工具链 | 17 |
| 构建系统 | Gradle (Kotlin DSL) |

## 📋 兼容性

- ✅ 全系列 JetBrains IDE（IntelliJ IDEA、WebStorm、PyCharm、GoLand 等）
- ✅ AutoJs6 应用版本 `≥ 6.7.0`
- ✅ Windows / macOS / Linux（Windows 内置 ADB）
- ✅ 与 [AutoJs6 VSCode 扩展](https://github.com/niceSilentSam/AutoJs6-VSCode-Extension) 工作流对等

## 📖 文档

完整的 AutoJs6 文档请访问 [docs.autojs6.com](https://docs.autojs6.com/)。

在 IDE 中可通过 **Tools → AutoJs6 → 查看在线文档** 直接访问。

## 🤝 参与贡献

欢迎贡献代码！请随时提交 Pull Request。

1. Fork 本仓库
2. 创建特性分支（`git checkout -b feature/amazing-feature`）
3. 提交更改（`git commit -m 'feat: add amazing feature'`）
4. 推送分支（`git push origin feature/amazing-feature`）
5. 发起 Pull Request

## 📄 许可证

本项目基于 MIT 许可证开源 —— 详见 [LICENSE](LICENSE) 文件。

## 🔗 相关项目

- [AutoJs6](https://github.com/SuperMonster003/AutoJs6) — AutoJs6 Android 应用
- [AutoJs6 VSCode 扩展](https://github.com/niceSilentSam/AutoJs6-VSCode-Extension) — VSCode 版本
- [AutoJs6 文档](https://docs.autojs6.com/) — 官方文档
