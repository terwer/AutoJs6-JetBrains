> [!WARNING]
> **⚠️ Under Active Development** — This plugin is currently in active development. Features may be incomplete, APIs may change, and bugs are expected. Use at your own risk. This notice will be removed once the project reaches a stable release.

<p align="center">
  <a href="https://github.com/terwer/AutoJs6-JetBrains">
    <img src="https://img.shields.io/github/stars/terwer/AutoJs6-JetBrains?style=social" alt="Stars" />
  </a>
  <a href="https://github.com/terwer/AutoJs6-JetBrains/blob/main/LICENSE">
    <img src="https://img.shields.io/github/license/terwer/AutoJs6-JetBrains" alt="License" />
  </a>
  <a href="https://github.com/terwer/AutoJs6-JetBrains/releases">
    <img src="https://img.shields.io/github/v/release/terwer/AutoJs6-JetBrains?include_prereleases" alt="Release" />
  </a>
  <img src="https://img.shields.io/badge/Kotlin-2.0.21-blue?logo=kotlin" alt="Kotlin" />
  <img src="https://img.shields.io/badge/IntelliJ_Platform-2024.2+-black?logo=jetbrains" alt="IntelliJ Platform" />
  <img src="https://img.shields.io/badge/JVM-21-orange?logo=openjdk" alt="JVM 21" />
</p>

<p align="center">
  <a href="https://docs.autojs6.com/">
    <img src="https://img.shields.io/badge/docs-AutoJs6-blue" alt="Docs" />
  </a>
  <img src="https://img.shields.io/badge/AutoJs6-≥6.7.0-green" alt="AutoJs6 Version" />
</p>

<h1 align="center">AutoJs6 JetBrains Plugin</h1>

<p align="center">
  A <strong>JetBrains IDE plugin</strong> for AutoJs6.Connect Android devices in IntelliJ IDEA, WebStorm, PyCharm, and the full JetBrains IDE family. Run, save, and stop scripts with one click, and scaffold AutoJs6 projects from a built-in template.
</p>

<p align="center">
  <a href="README.md">English</a> | <a href="README_zh_CN.md">简体中文</a>
</p>

---

## ✨ Features

| Feature | Description |
|---|---|
| 🔌 **Multi-mode Connection** | IDE listener (port `6347`), direct IP (port `7347`), and ADB — three ways to connect your AutoJs6 devices |
| ▶️ **Run Script** | Run the currently open script on all connected devices with a single shortcut |
| ▶️ **AutoJs6 Script / Project Run Configurations** | Save and rerun one local `.js` file or an AutoJs6 project with `project.json` through JetBrains Run Configurations, the green Run button, recent runs, and <kbd>Shift</kbd> + <kbd>F10</kbd> |
| 💾 **Save to Device** | Push the current script to all connected devices |
| ⏹️ **Stop Script** | Stop the current script or all running scripts at once |
| 📁 **Project Template** | Scaffold a new AutoJs6 project from the built-in AutoJs6 template |
| 📖 **Online Docs** | Jump to [docs.autojs6.com](https://docs.autojs6.com/) directly from the IDE |
| ⌨️ **Keyboard-first** | Full keyboard shortcut support — stay in the flow without leaving your IDE |

## 🚀 Quick Start

### Prerequisites

- **JetBrains IDE** — IntelliJ IDEA / WebStorm / PyCharm / any JetBrains IDE (build `242+`)
- **JDK 21+**
- **AutoJs6 App** — version `≥ 6.7.0` (version code `≥ 3591`) installed on your Android device
- Device and computer on the **same network** (for IP/listener mode) or connected via **USB + ADB**

### Installation

```bash
# Clone and build the plugin
git clone https://github.com/terwer/AutoJs6-JetBrains.git
cd AutoJs6-JetBrains
./gradlew buildPlugin
```

The built plugin zip will be in `build/distributions/`. Install it via:

> **Settings → Plugins → ⚙️ → Install Plugin from Disk...**

### Connect Your Device

1. Open the **Tools → AutoJs6** menu (or use shortcuts below)
2. Choose a connection method:
   - **IDE Listener** — starts a server on port `6347`; AutoJs6 app connects to your IDE
   - **IP Connect** — enter the device's IP; IDE connects to AutoJs6 server on port `7347`
   - **ADB Connect** — select a USB-connected device from the ADB device list

### Run a Script

1. Open any `.js` file in the editor
2. Press <kbd>F6</kbd> — the script runs on all connected devices instantly

### Run a Saved Script Configuration

1. Open **Run → Edit Configurations...**
2. Add **AutoJs6 Script**
3. Select one local `.js` file
4. Run it from the green Run button, recent runs, or <kbd>Shift</kbd> + <kbd>F10</kbd>

`AutoJs6 Script` and `AutoJs6 Project` are real JetBrains Run Configurations. VSCode-parity project actions (`Run Project` / `Save Project`) are also exposed as AutoJs6 actions; they resolve `project.json`, build a project diff zip, calculate md5, send bytes first, and then send the JSON `bytes_command`.

## ⌨️ Keyboard Shortcuts

| Action | Shortcut |
|---|---|
| Connect Device | <kbd>Ctrl</kbd> + <kbd>Alt</kbd> + <kbd>F6</kbd> |
| Disconnect All | <kbd>Ctrl</kbd> + <kbd>Alt</kbd> + <kbd>Shift</kbd> + <kbd>F6</kbd> |
| Run Current Script | <kbd>F6</kbd> |
| Stop Current Script | <kbd>Ctrl</kbd> + <kbd>F6</kbd> |
| Stop All Scripts | <kbd>Ctrl</kbd> + <kbd>Shift</kbd> + <kbd>F6</kbd> |
| New AutoJs6 Project | <kbd>Ctrl</kbd> + <kbd>Alt</kbd> + <kbd>6</kbd>, then <kbd>N</kbd> |

## 🏗️ Architecture

```
org.autojs.autojs6.jetbrains
├── actions    # IDE Actions — UI entry points for all user commands
├── adb        # ADB integration — device discovery and port forwarding
├── device     # Core networking — socket connections and binary protocol
├── project    # Project scaffolding — template-based project creation
├── run        # JetBrains Run Configuration for a single AutoJs6 script file
└── script     # Shared single-file command payload and validation helpers
```

### Protocol

The plugin uses a custom binary framing protocol over TCP:

| Component | Detail |
|---|---|
| Frame Header | 8 bytes |
| Payload Types | `1` = JSON, `2` = Bytes |
| Max Frame Size | 64 MB |
| Handshake | Bidirectional `hello` with version negotiation |
| Min App Version | `6.7.0` (code `3591`) — connection is rejected below this |

### Ports

| Port | Purpose |
|---|---|
| `6347` | IDE Listener — IDE accepts incoming device connections |
| `7347` | Server — AutoJs6 app listens for IDE connections |
| `10347` | HTTP Server |
| `20347` | ADB Server |

## 🛠️ Development

```bash
# Run the plugin in a sandbox IDE instance
./gradlew runIde

# Build the plugin distribution
./gradlew buildPlugin

# Run tests
./gradlew test

# Verify plugin compatibility across IDE versions
./gradlew verifyPlugin
```

### Tech Stack

| Technology | Version |
|---|---|
| Kotlin | 2.0.21 |
| IntelliJ Platform | 2024.2 (IC) |
| IntelliJ Platform Gradle Plugin | 2.2.1 |
| JVM Toolchain | 21 |
| Build System | Gradle with Kotlin DSL |

## 📋 Compatibility

- ✅ Designed for the full JetBrains IDE family (IntelliJ IDEA, WebStorm, PyCharm, GoLand, Rider, CLion, DataGrip, etc.; build `242+`) — release claims follow [`docs/release-compatibility-matrix.md`](docs/release-compatibility-matrix.md)
- ✅ AutoJs6 app version `≥ 6.7.0`
- ✅ Windows / macOS / Linux (ADB bundled for Windows)
- ✅ Parity with the [AutoJs6 VSCode extension](https://github.com/niceSilentSam/AutoJs6-VSCode-Extension) workflow

## 📖 Documentation

Full AutoJs6 documentation is available at [docs.autojs6.com](https://docs.autojs6.com/).

Access it directly from the IDE via **Tools → AutoJs6 → View Online Document**.

Release and Marketplace publishing steps are documented in [`docs/release-guide.md`](docs/release-guide.md).

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'feat: add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.

## 🔗 Related Projects

- [AutoJs6](https://github.com/SuperMonster003/AutoJs6) — The AutoJs6 Android app
- [AutoJs6 VSCode Extension](https://github.com/niceSilentSam/AutoJs6-VSCode-Extension) — VSCode counterpart
- [AutoJs6 Docs](https://docs.autojs6.com/) — Official documentation
