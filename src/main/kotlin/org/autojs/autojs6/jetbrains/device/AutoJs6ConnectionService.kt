package org.autojs.autojs6.jetbrains.device

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import org.autojs.autojs6.jetbrains.AutoJs6Constants
import org.autojs.autojs6.jetbrains.AutoJs6Notifier
import org.autojs.autojs6.jetbrains.AutoJs6SettingsService
import org.autojs.autojs6.jetbrains.actions.AutoJs6CommandDispatcher
import java.io.Closeable
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketTimeoutException
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

interface AutoJs6ConnectionListener {
    fun deviceAttached(device: AutoJs6Device) {}
    fun deviceDetached(device: AutoJs6Device, reason: String) {}
    fun selectedDeviceChanged(device: AutoJs6Device?) {}
    fun logReceived(device: AutoJs6Device, text: String) {}
}

data class AutoJs6DeviceSnapshot(
    val key: String,
    val name: String,
    val connectionType: String,
    val endpoint: String,
    val status: String,
    val version: String,
    val adbDeviceId: String?
)

class AutoJs6Device(
    private val socket: Socket,
    private val onAttach: (AutoJs6Device) -> Unit,
    private val onDisconnect: (AutoJs6Device, String) -> Unit,
    private val project: Project? = null,
    val adbDeviceId: String? = null,
    val host: String? = null,
    val port: Int? = null,
    val connectionType: String = if (adbDeviceId != null) "ADB" else if (host == null) "IDE listener" else "LAN",
    private val onLog: (AutoJs6Device, String) -> Unit = { _, _ -> },
    private val onReverseCommand: (AutoJs6Device, String, String?) -> Unit = { _, _, _ -> }
) : Closeable {
    private val codec = FrameCodec()
    private val ids = AtomicInteger(1)
    @Volatile var attached = false; private set
    @Volatile var deviceId: String = ""; private set
    @Volatile var deviceName: String = "unknown device"; private set
    @Volatile var versionCode: Int = 0; private set
    @Volatile var versionName: String = ""; private set
    @Volatile private var closeReason: String = "socket closed"

    constructor(
        socket: Socket,
        onAttach: (AutoJs6Device) -> Unit,
        onDisconnect: (AutoJs6Device) -> Unit,
        project: Project? = null,
        adbDeviceId: String? = null,
        host: String? = null
    ) : this(
        socket = socket,
        onAttach = onAttach,
        onDisconnect = { device, _ -> onDisconnect(device) },
        project = project,
        adbDeviceId = adbDeviceId,
        host = host
    )

    init { Thread({ readLoop() }, "AutoJs6-device-reader").apply { isDaemon = true }.start() }

    fun key(): String = listOf(deviceId, adbDeviceId, host, endpoint()).firstOrNull { !it.isNullOrBlank() } ?: hashCode().toString()

    fun endpoint(): String = when {
        adbDeviceId != null -> adbDeviceId
        host != null -> "$host:${port ?: AutoJs6Constants.SERVER_PORT}"
        else -> "${socket.inetAddress.hostAddress}:${socket.port}"
    }

    fun snapshot(): AutoJs6DeviceSnapshot = AutoJs6DeviceSnapshot(
        key = key(),
        name = deviceName,
        connectionType = connectionType,
        endpoint = endpoint(),
        status = if (attached && !socket.isClosed) "connected" else "disconnected",
        version = if (versionName.isBlank()) "unknown" else "$versionName ($versionCode)",
        adbDeviceId = adbDeviceId
    )

    fun sendJson(value: Map<String, Any?>) {
        val packet = codec.encode(AutoJs6Constants.TYPE_JSON, JsonCodec.encode(value))
        synchronized(socket) { socket.getOutputStream().write(packet); socket.getOutputStream().flush() }
    }

    fun sendBytes(bytes: ByteArray) {
        val packet = codec.encode(AutoJs6Constants.TYPE_BYTES, bytes)
        synchronized(socket) { socket.getOutputStream().write(packet); socket.getOutputStream().flush() }
    }

    fun sendCommand(command: String, data: Map<String, Any?> = emptyMap()): Int {
        val id = ids.getAndIncrement()
        sendJson(mapOf("id" to id, "type" to "command", "data" to (data + mapOf("command" to command))))
        return id
    }

    fun sendBytesCommand(md5: String, data: Map<String, Any?>) {
        sendJson(mapOf("type" to "bytes_command", "md5" to md5, "data" to data))
    }

    private fun sendHello(error: String? = null): Int {
        val id = ids.getAndIncrement()
        val data = linkedMapOf<String, Any?>("extensionVersion" to "0.1.0")
        if (error != null) data["errorMessage"] = error
        sendJson(mapOf("id" to id, "type" to "hello", "data" to data))
        return id
    }

    private fun readLoop() {
        socket.soTimeout = 5000
        try {
            val input = socket.getInputStream()
            val buf = ByteArray(8192)
            while (!socket.isClosed) {
                val n = input.read(buf)
                if (n < 0) break
                for (frame in codec.feed(buf.copyOf(n))) {
                    if (frame is AutoJs6Frame.Json) onJson(JsonCodec.decode(frame.payload))
                }
            }
        } catch (_: SocketTimeoutException) {
            if (!attached) {
                closeReason = "handshake timeout"
                AutoJs6Notifier.error(project, "连接建立超时：请确认 AutoJs6 版本不低于 ${AutoJs6Constants.MIN_VERSION_NAME}，并检查服务端/客户端模式与端口。")
            }
        } catch (t: Throwable) {
            if (!socket.isClosed) {
                closeReason = "error: ${t.message ?: t.javaClass.simpleName}"
                AutoJs6Notifier.error(project, "AutoJs6 连接错误: ${t.message}")
            }
        } finally { close(); onDisconnect(this, closeReason) }
    }

    private fun onJson(json: JsonPayload) {
        when (json.string("type")) {
            "hello" -> onHello(json)
            "log" -> onLog(this, extractLogText(json.obj("data") ?: json))
            "command" -> onReverseCommandJson(json.obj("data") ?: json)
            else -> Unit
        }
    }

    private fun onHello(json: JsonPayload) {
        val data = json.obj("data") ?: return
        deviceName = data.string("device_name") ?: "unknown device"
        versionName = data.string("app_version") ?: ""
        versionCode = data.string("app_version_code")?.toIntOrNull() ?: data.int("app_version_code") ?: 0
        if (versionCode < AutoJs6Constants.MIN_VERSION_CODE) {
            val msg = "无法建立连接, AutoJs6 版本 $versionName ($versionCode) 应不低于 ${AutoJs6Constants.MIN_VERSION_NAME} (${AutoJs6Constants.MIN_VERSION_CODE})"
            runCatching { sendHello(msg) }
            closeReason = "unsupported version"
            AutoJs6Notifier.error(project, msg)
            close()
            return
        }
        deviceId = data.string("device_id") ?: ""
        socket.soTimeout = 0
        sendHello()
        attached = true
        closeReason = "normal disconnect"
        onAttach(this)
    }

    private fun extractLogText(data: JsonPayload): String =
        data.string("log") ?: data.string("message") ?: data.string("text") ?: data.toString()

    private fun onReverseCommandJson(data: JsonPayload) {
        val map = data.asMap()
        val commandEntry = map.entries.firstOrNull { normalizeCommandKey(it.key) == "cmd" || normalizeCommandKey(it.key) == "command" }
        val command = commandEntry?.value?.toString()?.trim().orEmpty()
        val path = map.entries.firstOrNull { normalizeCommandKey(it.key) == "path" }?.value?.toString()
        if (command.isBlank()) {
            AutoJs6Notifier.error(project, "设备反向命令缺少 command/cmd 字段")
            return
        }
        onReverseCommand(this, command, path)
    }

    private fun normalizeCommandKey(key: String): String = key.replace('\u00A0', ' ').trim().lowercase()

    override fun close() { runCatching { socket.close() } }
    override fun toString(): String = "$deviceName (${endpoint()})"
}

@Service(Service.Level.APP)
class AutoJs6ConnectionService : Disposable {
    @Volatile private var notificationProject: Project? = null
    private val executor = Executors.newCachedThreadPool { r -> Thread(r, "AutoJs6-connection").apply { isDaemon = true } }
    private val devices = CopyOnWriteArrayList<AutoJs6Device>()
    private val listeners = CopyOnWriteArrayList<AutoJs6ConnectionListener>()
    @Volatile private var serverSocket: ServerSocket? = null
    @Volatile private var selectedDeviceKey: String? = null

    init { startListening() }

    fun addListener(listener: AutoJs6ConnectionListener) { listeners += listener }
    fun removeListener(listener: AutoJs6ConnectionListener) { listeners -= listener }

    fun connectedDevices(): List<AutoJs6Device> = devices.filter { it.attached }
    fun deviceSnapshots(): List<AutoJs6DeviceSnapshot> = connectedDevices().map { it.snapshot() }

    fun selectedDeviceKey(): String? = selectedConnectedDevice()?.key()

    fun selectDevice(key: String?) {
        val normalizedKey = key?.let { wanted -> connectedDevices().firstOrNull { it.key() == wanted }?.key() }
        if (selectedDeviceKey == normalizedKey) return
        selectedDeviceKey = normalizedKey
        notifySelectedDeviceChanged()
    }

    fun selectedConnectedDevice(): AutoJs6Device? = selectedDeviceKey?.let { key -> connectedDevices().firstOrNull { it.key() == key } }

    fun diagnosticSummary(): String {
        val settings = AutoJs6SettingsService.getInstance().state
        val devicesText = deviceSnapshots().joinToString("\n") { "- ${it.name} [${it.connectionType}] ${it.endpoint} ${it.status} ${it.version}" }
            .ifBlank { "- none" }
        val recent = AutoJs6SettingsService.getInstance().recentHostDisplayStrings().joinToString("\n") { "- $it" }.ifBlank { "- none" }
        return """
            AutoJs6 Diagnostics
            Listening port: ${settings.listeningPort} (${if (serverSocket?.isClosed == false) "running" else "stopped"})
            Device server port: ${settings.serverPort}
            ADB server port: ${settings.adbServerPort}
            HTTP bridge: enabled=${settings.httpBridgeEnabled}, bind=${settings.httpBridgeBindHost}:${settings.httpBridgePort}, compatibilityMode=${settings.httpBridgeCompatibilityMode}
            Devices:
            $devicesText
            Recent records:
            $recent
        """.trimIndent()
    }

    fun startListening(project: Project? = null, port: Int = AutoJs6SettingsService.getInstance().state.listeningPort) {
        if (project != null) notificationProject = project
        if (serverSocket?.isClosed == false) return
        executor.submit {
            try {
                val ss = ServerSocket()
                ss.reuseAddress = true
                ss.bind(InetSocketAddress(port))
                serverSocket = ss
                while (!ss.isClosed) {
                    val socket = ss.accept()
                    AutoJs6Device(
                        socket = socket,
                        onAttach = ::attach,
                        onDisconnect = ::detach,
                        project = notificationProject,
                        host = socket.inetAddress.hostAddress,
                        port = socket.port,
                        connectionType = "IDE listener",
                        onLog = ::handleLog,
                        onReverseCommand = ::handleReverseCommand
                    )
                }
            } catch (t: Throwable) {
                if (serverSocket?.isClosed != true) {
                    serverSocket?.close()
                    serverSocket = null
                    AutoJs6Notifier.error(notificationProject, "无法监听 AutoJs6 端口 $port: ${t.message}. 请检查端口占用或防火墙设置。")
                }
            }
        }
    }

    fun connectTo(host: String, port: Int = AutoJs6SettingsService.getInstance().state.serverPort, adbDeviceId: String? = null, project: Project? = null) {
        if (project != null) notificationProject = project
        val normalizedHost = host.trim()
        val duplicate = connectedDevices().firstOrNull { device ->
            (adbDeviceId != null && device.adbDeviceId == adbDeviceId) ||
                (adbDeviceId == null && device.host == normalizedHost && device.port == port)
        }
        if (duplicate != null) {
            AutoJs6Notifier.warn(notificationProject, "设备已连接，无需重复连接: ${duplicate.endpoint()}")
            return
        }
        executor.submit {
            try {
                val socket = Socket()
                socket.connect(InetSocketAddress(normalizedHost, port), 5000)
                AutoJs6Device(
                    socket = socket,
                    onAttach = { dev ->
                        attach(dev)
                        if (adbDeviceId == null) AutoJs6SettingsService.getInstance().addRecentHost(if (port == AutoJs6Constants.SERVER_PORT) normalizedHost else "$normalizedHost:$port")
                    },
                    onDisconnect = ::detach,
                    project = notificationProject,
                    adbDeviceId = adbDeviceId,
                    host = normalizedHost,
                    port = port,
                    connectionType = if (adbDeviceId == null) "LAN server" else "ADB",
                    onLog = ::handleLog,
                    onReverseCommand = ::handleReverseCommand
                )
            } catch (t: Throwable) {
                AutoJs6Notifier.error(notificationProject, "连接 AutoJs6 服务端 $normalizedHost:$port 失败: ${t.message}. 诊断：确认 AutoJs6 Server mode 已开启、两端在同一网络、端口未被防火墙阻断，且没有重复连接。")
            }
        }
    }

    fun sendCommand(command: String, data: Map<String, Any?> = emptyMap()): Boolean =
        sendCommandToConnectedDevices(command, data) > 0

    fun sendCommandToConnectedDevices(command: String, data: Map<String, Any?> = emptyMap()): Int {
        val current = connectedDevices()
        if (current.isEmpty()) return 0
        current.forEach { it.sendCommand(command, data) }
        return current.size
    }

    fun sendCommandToDevice(device: AutoJs6Device, command: String, data: Map<String, Any?> = emptyMap()): Boolean {
        if (!device.attached) return false
        device.sendCommand(command, data)
        return true
    }

    fun disconnectAll(project: Project? = null) {
        if (project != null) notificationProject = project
        val snapshot = devices.toList()
        snapshot.forEach { it.close() }
        devices.clear()
        val selectionChanged = selectedDeviceKey != null
        selectedDeviceKey = null
        AutoJs6Notifier.info(notificationProject, "已断开所有 AutoJs6 设备连接")
        notifyUi {
            snapshot.forEach { device ->
                listeners.forEach { it.deviceDetached(device, "disconnectAll") }
            }
            if (selectionChanged) listeners.forEach { it.selectedDeviceChanged(null) }
        }
    }

    private fun attach(device: AutoJs6Device) {
        val duplicate = connectedDevices().firstOrNull { it !== device && duplicateIdentity(it, device) }
        if (duplicate != null) {
            AutoJs6Notifier.warn(notificationProject, "检测到重复设备连接，已拒绝新连接: ${device.endpoint()}")
            device.close()
            return
        }
        if (!devices.contains(device)) devices += device
        val selectionChanged = selectedDeviceKey == null
        if (selectionChanged) selectedDeviceKey = device.key()
        AutoJs6Notifier.info(notificationProject, "AutoJs6 设备已连接: $device")
        notifyUi {
            listeners.forEach { it.deviceAttached(device) }
            if (selectionChanged) listeners.forEach { it.selectedDeviceChanged(device) }
        }
    }

    private fun duplicateIdentity(a: AutoJs6Device, b: AutoJs6Device): Boolean =
        (a.deviceId.isNotBlank() && a.deviceId == b.deviceId) ||
            (a.adbDeviceId != null && a.adbDeviceId == b.adbDeviceId) ||
            (a.adbDeviceId == null && b.adbDeviceId == null && a.host != null && a.host == b.host && a.port == b.port)

    private fun detach(device: AutoJs6Device, reason: String) {
        val removed = devices.remove(device)
        val selectionChanged = selectedDeviceKey == device.key()
        if (selectionChanged) selectedDeviceKey = connectedDevices().firstOrNull()?.key()
        if (removed) {
            AutoJs6Notifier.info(notificationProject, "AutoJs6 设备已断开: ${device.endpoint()} ($reason)")
            notifyUi {
                listeners.forEach { it.deviceDetached(device, reason) }
                if (selectionChanged) listeners.forEach { it.selectedDeviceChanged(selectedConnectedDevice()) }
            }
        }
    }

    private fun handleLog(device: AutoJs6Device, text: String) {
        notifyUi { listeners.forEach { it.logReceived(device, text) } }
    }

    private fun handleReverseCommand(device: AutoJs6Device, command: String, path: String?) {
        val result = AutoJs6CommandDispatcher.dispatch(notificationProject, command, path)
        if (result.success) {
            AutoJs6Notifier.info(notificationProject, "执行设备反向命令 \"$command\": ${device.deviceName}")
        } else {
            AutoJs6Notifier.error(notificationProject, "拒绝设备反向命令 \"$command\": ${result.message}")
        }
    }

    private fun notifyUi(block: () -> Unit) {
        ApplicationManager.getApplication().invokeLater(block)
    }

    private fun notifySelectedDeviceChanged() {
        val selected = selectedConnectedDevice()
        notifyUi { listeners.forEach { it.selectedDeviceChanged(selected) } }
    }

    override fun dispose() {
        runCatching { serverSocket?.close() }
        devices.forEach { it.close() }
        devices.clear()
        executor.shutdownNow()
    }
}
