package org.autojs.autojs6.jetbrains.device

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import org.autojs.autojs6.jetbrains.AutoJs6Constants
import org.autojs.autojs6.jetbrains.AutoJs6Notifier
import org.autojs.autojs6.jetbrains.AutoJs6SettingsService
import java.io.Closeable
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketTimeoutException
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

class AutoJs6Device(
    private val socket: Socket,
    private val onAttach: (AutoJs6Device) -> Unit,
    private val onDisconnect: (AutoJs6Device) -> Unit,
    private val project: Project? = null,
    val adbDeviceId: String? = null,
    val host: String? = null
) : Closeable {
    private val codec = FrameCodec()
    private val ids = AtomicInteger(1)
    @Volatile var attached = false; private set
    @Volatile var deviceId: String = ""; private set
    @Volatile var deviceName: String = "unknown device"; private set
    @Volatile var versionCode: Int = 0; private set
    @Volatile var versionName: String = ""; private set

    init { Thread({ readLoop() }, "AutoJs6-device-reader").apply { isDaemon = true }.start() }

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
            if (!attached) AutoJs6Notifier.error(project, "连接建立超时")
        } catch (t: Throwable) {
            if (!socket.isClosed) AutoJs6Notifier.error(project, "AutoJs6 连接错误: ${t.message}")
        } finally { close(); onDisconnect(this) }
    }

    private fun onJson(json: JsonPayload) {
        if (json.string("type") != "hello") return
        val data = json.obj("data") ?: return
        deviceName = data.string("device_name") ?: "unknown device"
        versionName = data.string("app_version") ?: ""
        versionCode = data.string("app_version_code")?.toIntOrNull() ?: 0
        if (versionCode < AutoJs6Constants.MIN_VERSION_CODE) {
            val msg = "无法建立连接, AutoJs6 版本 $versionName ($versionCode) 应不低于 ${AutoJs6Constants.MIN_VERSION_NAME} (${AutoJs6Constants.MIN_VERSION_CODE})"
            runCatching { sendHello(msg) }
            AutoJs6Notifier.error(project, msg)
            close()
            return
        }
        deviceId = data.string("device_id") ?: ""
        socket.soTimeout = 0
        sendHello()
        attached = true
        onAttach(this)
    }

    override fun close() { runCatching { socket.close() } }
    override fun toString(): String = "$deviceName (${socket.inetAddress.hostAddress}:${socket.port})"
}

@Service(Service.Level.APP)
class AutoJs6ConnectionService : Disposable {
    @Volatile private var notificationProject: Project? = null
    private val executor = Executors.newCachedThreadPool { r -> Thread(r, "AutoJs6-connection").apply { isDaemon = true } }
    private val devices = CopyOnWriteArrayList<AutoJs6Device>()
    @Volatile private var serverSocket: ServerSocket? = null

    init { startListening() }

    fun connectedDevices(): List<AutoJs6Device> = devices.filter { it.attached }

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
                    AutoJs6Device(socket, ::attach, ::detach, notificationProject)
                }
            } catch (t: Throwable) {
                if (serverSocket?.isClosed != true) {
                    serverSocket?.close()
                    serverSocket = null
                    AutoJs6Notifier.error(notificationProject, "无法监听 AutoJs6 端口 $port: ${t.message}")
                }
            }
        }
    }

    fun connectTo(host: String, port: Int = AutoJs6SettingsService.getInstance().state.serverPort, adbDeviceId: String? = null, project: Project? = null) {
        if (project != null) notificationProject = project
        executor.submit {
            try {
                val socket = Socket()
                socket.connect(InetSocketAddress(host, port), 5000)
                AutoJs6Device(socket, { dev -> attach(dev); if (adbDeviceId == null) AutoJs6SettingsService.getInstance().addRecentHost(host) }, ::detach, notificationProject, adbDeviceId, host)
            } catch (t: Throwable) {
                AutoJs6Notifier.error(notificationProject, "连接 AutoJs6 服务端 $host:$port 失败: ${t.message}")
            }
        }
    }

    fun sendCommand(command: String, data: Map<String, Any?> = emptyMap()): Boolean {
        val current = connectedDevices()
        if (current.isEmpty()) return false
        current.forEach { it.sendCommand(command, data) }
        return true
    }

    fun disconnectAll(project: Project? = null) {
        if (project != null) notificationProject = project
        devices.forEach { it.close() }
        devices.clear()
        AutoJs6Notifier.info(notificationProject, "已断开所有 AutoJs6 设备连接")
    }

    private fun attach(device: AutoJs6Device) {
        if (!devices.contains(device)) devices += device
        AutoJs6Notifier.info(notificationProject, "AutoJs6 设备已连接: $device")
    }

    private fun detach(device: AutoJs6Device) { devices.remove(device) }

    override fun dispose() {
        runCatching { serverSocket?.close() }
        disconnectAll()
        executor.shutdownNow()
    }
}
