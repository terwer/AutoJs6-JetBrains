# AutoJs6 JetBrains MVP 使用与测试指南

本文档说明如何启动、安装、连接设备并验证 AutoJs6 JetBrains MVP 插件。

## 1. 构建与启动

开发模式启动 sandbox IDE：

```powershell
.\gradlew.bat runIde
```

构建并测试插件：

```powershell
.\gradlew.bat test buildPlugin --no-daemon
```

插件 ZIP 输出：

```text
build/distributions/AutoJs6-JetBrains-0.1.1.zip
```

安装 ZIP：

```text
Settings / Preferences
→ Plugins
→ 齿轮图标
→ Install Plugin from Disk
→ 选择 build/distributions/AutoJs6-JetBrains-0.1.1.zip
→ Restart IDE
```

## 2. 插件入口

入口：

```text
Tools → AutoJs6
```

MVP Action：

- 查看在线文档 (View Online Document)
- 建立设备连接 (Connect)
- 断开所有连接 (Disconnect All)
- 运行脚本 (Run)
- 保存到所有设备 (Save)
- 停止当前脚本 (Stop)
- 停止所有脚本 (Stop All)
- 新建项目 (New Project)

建议快捷键：

| 功能 | 快捷键 |
|---|---|
| Run Current File | `F6` |
| Stop Current Script | `Ctrl + F6` |
| Stop All Scripts | `Ctrl + Shift + F6` |
| Connect | `Ctrl + Alt + F6` |
| Disconnect All | `Ctrl + Alt + Shift + F6` |
| Rerun selected Run Configuration | `Shift + F10` |

## 3. 连接方式

执行：

```text
Tools → AutoJs6 → 建立设备连接 (Connect)
```

### 3.1 ADB 连接（推荐 emulator-5560）

确认设备 online：

```powershell
adb devices -l
adb -s emulator-5560 get-state
adb -s emulator-5560 shell getprop sys.boot_completed
```

预期：

```text
emulator-5560 device
device
1
```

如存在重复的 `127.0.0.1:5561`，先断开：

```powershell
adb disconnect 127.0.0.1:5561
```

AutoJs6 App 中开启：

```text
服务端模式 / Server mode
```

IDE 中选择：

```text
Tools → AutoJs6 → 建立设备连接
→ 通过 ADB 连接
→ emulator-5560
```

插件会执行等价逻辑：

```powershell
adb -s emulator-5560 forward tcp:<localPort> tcp:7347
```

然后通过 `127.0.0.1:<localPort>` 与 AutoJs6 完成 TCP hello 握手。

> 实测 AutoJs6 server hello 来自设备端口 `7347`；不要把插件连接 forward 到 `20347`。

### 3.2 通过 IP 连接 AutoJs6 服务端

适合电脑和设备在同一网络，且 AutoJs6 App 已开启 Server mode。

```text
Tools → AutoJs6 → 建立设备连接
→ 通过 IP 连接服务端
→ 输入设备 IP
```

默认端口：

```text
7347
```

### 3.3 IDE 监听客户端连接

适合 AutoJs6 App 作为客户端主动连接 IDE。

```text
Tools → AutoJs6 → 建立设备连接
→ IDE 监听客户端连接
```

默认监听端口：

```text
6347
```

AutoJs6 侧连接电脑 IP 的 `6347` 端口。

## 4. 手工验证 emulator-5560 ADB forward

创建 forward：

```powershell
adb -s emulator-5560 forward --remove tcp:37047 2>$null
adb -s emulator-5560 forward tcp:37047 tcp:7347
```

创建 `verify-autojs6-hello.py`：

```python
import socket, struct, json

s = socket.create_connection(("127.0.0.1", 37047), timeout=8)
s.settimeout(8)

header = s.recv(8)
length, typ = struct.unpack(">ii", header)
payload = b""
while len(payload) < length:
    payload += s.recv(length - len(payload))

print("length=", length, "type=", typ)
print(payload.decode("utf-8", errors="replace"))

msg = json.loads(payload.decode("utf-8"))
assert typ == 1
assert msg["type"] == "hello"
assert int(msg["data"]["app_version_code"]) >= 3591

resp = {"id": 1, "type": "hello", "data": {"extensionVersion": "0.1.1"}}
out = json.dumps(resp, separators=(",", ":")).encode()
s.sendall(struct.pack(">ii", len(out), 1) + out)
s.close()
print("hello ok")
```

运行：

```powershell
python .\verify-autojs6-hello.py
```

预期输出类似：

```text
length= 133 type= 1
{"type":"hello","data":{"device_name":"HONOR SDY-AN00","app_version":"6.7.0","device_id":"...","app_version_code":3810}}
hello ok
```

清理 forward：

```powershell
adb -s emulator-5560 forward --remove tcp:37047
```

## 5. 验证运行当前脚本

在 IDE 中打开本地 `.js` 文件，例如：

```javascript
toast("Hello from JetBrains");
```

确保设备已连接后执行：

```text
Tools → AutoJs6 → 运行脚本
```

或按：

```text
F6
```

预期：AutoJs6 设备侧运行当前编辑器内容。

## 6. 验证保存与停止

保存当前文件到设备：

```text
Tools → AutoJs6 → 保存到所有设备
```

停止当前脚本：

```text
Tools → AutoJs6 → 停止当前脚本
```

停止所有脚本：

```text
Tools → AutoJs6 → 停止所有脚本
```

无连接设备时，这些命令会报错，不会显示假成功。

## 7. 单文件 Run Configuration

本变更新增的 JetBrains 原生运行配置只覆盖单个本地 `.js` 文件：

```text
Run → Edit Configurations...
→ Add New Configuration
→ AutoJs6 Script
→ Script .js file 选择本地 .js 文件
```

也可以在当前编辑器或 Project View 选中本地 `.js` 文件时，通过 JetBrains 的运行配置创建入口生成 `AutoJs6 Script` 配置。

运行方式：

```text
绿色 Run 按钮
最近运行记录
Shift + F10
```

执行时插件读取配置中的文件路径、文件名和文本内容，并向所有已连接 AutoJs6 设备发送单文件 payload：

```json
{
  "command": "run",
  "id": "<本地脚本绝对路径>",
  "name": "<文件名.js>",
  "script": "<文件文本>"
}
```

失败边界：

- 未连接设备：报错，不发送命令，不显示假成功。
- 文件不存在、目录、非本地文件或非 `.js` 文件：配置校验失败，不发送命令。
- 文件不可读取：运行失败并提示，不发送命令。

项目运行边界：

- 当前注册 `AutoJs6 Project` Run Configuration；项目操作也可通过 VSCode parity actions 完成。
- `Run Project` / `Save Project` 会从当前文件、Project View 选中目录/文件或项目根目录解析 `project.json`。
- 有效项目会在后台任务中计算 mtime diff，按相对路径 zip modified files，计算 zip bytes 的 md5，先发送 bytes payload，再发送 JSON `bytes_command`。
- 缺少 `project.json`、zip/md5/bytes 发送失败或无连接设备时会报错，不显示假成功。

项目命令：

```text
Tools → AutoJs6 → 运行项目 (Run Project)
Tools → AutoJs6 → 保存项目 (Save Project)
Main Toolbar → AutoJs6 → Run Project / Save Project
Project View folder context → AutoJs6 → Run Project / Save Project
Run | Edit Configurations... → AutoJs6 Project
```

已记录的实机协议验证：

```text
ADB device: emulator-5560 / HONOR SDY-AN00
AutoJs6: 6.7.0 (3810)
Sent: save_project and run_project bytes_command
Observed: live device log frame after dispatch
```

### 7.1 右下状态栏设备切换

连接设备后，IDE 右下 Status Bar 会显示当前 AutoJs6 设备：

```text
AutoJs6: Pixel_8 ▾
```

点击该 Widget 可在已连接设备之间切换，例如模拟器、本机、ADB 设备。切换的是 shared selected device：

- Tool Window 中的 Run / Save / Stop / Disconnect 按钮会使用该 selected device。
- `Run on Device` / `Save to Device` / selected-device diagnostics 会优先使用该 selected device。
- 普通 `Run` / `Save` / `Run Project` / `Save Project` 仍保持 VSCode-compatible all-devices 语义，不会因为状态栏切换而静默变成单设备命令。

实时性验证：

```text
1. 连接两个设备，确认状态栏显示当前设备。
2. 点击状态栏切换到另一个设备，确认 Tool Window 选中行同步变化。
3. 断开当前设备，确认状态栏自动切到剩余设备或显示“无设备”。
```

## 8. 验证新建项目

执行：

```text
Tools → AutoJs6 → 新建项目
```

选择目标目录后，该目录就是 AutoJs6 项目根目录；插件会直接复制内置模板并替换：

```text
%PROJECT_NAME_PLACEHOLDER%
%PACKAGE_SUFFIX_PLACEHOLDER%
```

生成后检查：

```text
project.json
main.js
```

`project.json` 中应包含替换后的：

```json
{
  "name": "<目录名>",
  "packageName": "org.autojs.autojs6.<规范化包名后缀>"
}
```

## 9. 常见问题

### 9.1 `emulator-5560` 与 `127.0.0.1:5561`

`5560` 通常是 emulator console 端口，`5561` 是对应 ADB TCP 端口。若 ADB 已识别出 `emulator-5560`，优先选择 `emulator-5560`。

断开重复项：

```powershell
adb disconnect 127.0.0.1:5561
```

### 9.2 ADB 显示 offline

```powershell
adb kill-server
adb start-server
adb devices -l
```

如果仍是 `offline`，重启模拟器或重新打开 USB 调试。

### 9.3 连接成功但运行无反应

检查：

1. AutoJs6 App 是否已开启 Server mode。
2. 当前 IDE 编辑器是否打开本地 `.js` 文件。
3. 是否已经出现设备连接通知。
4. AutoJs6 版本是否不低于 `6.7.0 / 3591`。
5. 是否选择了重复 ADB serial；优先选择 `emulator-5560`。

## 10. 推荐验收顺序

```text
1. .\gradlew.bat test buildPlugin --no-daemon
2. .\gradlew.bat runIde
3. adb devices -l 确认 emulator-5560 device
4. AutoJs6 App 开启 Server mode
5. Tools → AutoJs6 → Connect → ADB → emulator-5560
6. 打开 main.js，按 F6
7. 验证 toast 或脚本行为
8. 验证 Save / Stop / Stop All
9. 验证 AutoJs6 Script Run Configuration 可创建并运行单个 .js 文件
9b. 验证 AutoJs6 Project Run Configuration 可创建并运行包含 project.json 的项目
9c. 验证右下 Status Bar 显示已连接设备，并可切换 selected device
10. 验证 New Project 生成 project.json
11. 验证 Run Project / Save Project 对有效 project.json 项目发送 bytes_command
```

## 11. 归档说明

`openspec archive` 只表示需求变更已完成并同步 specs，不会自动安装插件。

真正使用插件需要：

```powershell
.\gradlew.bat runIde
```

或安装 ZIP：

```text
build/distributions/AutoJs6-JetBrains-0.1.1.zip
```
