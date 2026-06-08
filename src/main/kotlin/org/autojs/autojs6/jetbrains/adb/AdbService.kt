package org.autojs.autojs6.jetbrains.adb

import com.intellij.execution.configurations.PathEnvironmentVariableUtil
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.SystemInfo
import com.intellij.util.EnvironmentUtil
import org.autojs.autojs6.jetbrains.AutoJs6Constants
import org.autojs.autojs6.jetbrains.AutoJs6Notifier
import org.autojs.autojs6.jetbrains.AutoJs6SettingsService
import org.autojs.autojs6.jetbrains.device.AutoJs6ConnectionService
import java.io.File
import java.net.ServerSocket
import java.util.concurrent.ConcurrentHashMap


data class AdbDeviceInfo(val id: String, val properties: Map<String, String>) {
    val model: String = properties["model"] ?: "Unknown"
    val product: String = properties["product"] ?: "Unknown"
    val device: String = properties["device"] ?: "Unknown"
    val transportId: String = properties["transport_id"] ?: ""
    val name: String = "$model ($id)"
    val detail: String = "model=$model, product=$product, device=$device" + if (transportId.isNotBlank()) ", transport_id=$transportId" else ""
}

data class AdbForwardHandle(val serial: String, val localPort: Int, val remotePort: Int)

@Service(Service.Level.PROJECT)
class AdbService(private val project: Project) {
    private val forwards = ConcurrentHashMap<String, MutableList<AdbForwardHandle>>()

    fun resolveExecutable(): String? {
        val configured = AutoJs6SettingsService.getInstance().state.adbPath.trim()
        AdbExecutableResolver.resolveConfigured(configured, SystemInfo.isWindows)?.let { return it }
        AdbExecutableResolver.resolveFromPath(SystemInfo.isWindows)?.let { return it }
        if (canRun("adb")) return "adb"
        if (!SystemInfo.isWindows) {
            AdbExecutableResolver.resolveFromCommonUnixLocations()?.let { return it }
        }
        if (SystemInfo.isWindows) {
            val candidates = listOf(
                File(PathManager.getPluginsPath(), "AutoJs6/tools/adb.exe"),
                File(PathManager.getPluginsPath(), "AutoJs6/tools/platform-tools/adb.exe"),
                File(PathManager.getHomePath(), "tools/platform-tools/adb.exe"),
                File("src/main/resources/tools/adb.exe"),
                File("src/main/resources/tools/platform-tools/adb.exe")
            )
            return candidates.firstOrNull { AdbExecutableResolver.isUsableExecutable(it, windows = true) }?.absolutePath
        }
        return null
    }

    fun listDevices(): List<AdbDeviceInfo> {
        val adb = resolveExecutable() ?: run {
            AutoJs6Notifier.error(project, "ADB 不可用，请配置 ADB 路径或将 adb 加入 PATH。Windows 版插件会尝试使用内置 adb.exe fallback。")
            return emptyList()
        }
        val result = run(adb, "devices", "-l")
        if (result.exitCode != 0) {
            AutoJs6Notifier.error(project, "执行 adb devices -l 失败: ${result.stderr.ifBlank { result.stdout }}")
            return emptyList()
        }
        val parsed = parseDevices(result.stdout)
        if (parsed.isEmpty()) AutoJs6Notifier.warn(project, "未发现可用 ADB 设备；请检查 USB 调试授权、设备 online 状态和 adb server。")
        return parsed
    }

    fun connectViaAdb(device: AdbDeviceInfo) {
        val adb = resolveExecutable() ?: run {
            AutoJs6Notifier.error(project, "ADB 不可用，请配置 ADB 路径或将 adb 加入 PATH")
            return
        }
        val serverLocalPort = findAvailablePort()
        val providerLocalPort = findAvailablePort()
        val handles = listOf(
            AdbForwardHandle(device.id, serverLocalPort, AutoJs6SettingsService.getInstance().state.serverPort),
            AdbForwardHandle(device.id, providerLocalPort, AutoJs6SettingsService.getInstance().state.adbServerPort)
        )
        handles.forEach { handle ->
            val forward = run(adb, "-s", handle.serial, "forward", "tcp:${handle.localPort}", "tcp:${handle.remotePort}")
            if (forward.exitCode != 0) {
                cleanupForDevice(device.id)
                AutoJs6Notifier.error(project, "ADB forward 失败 (${handle.localPort}->${handle.remotePort}): ${forward.stderr.ifBlank { forward.stdout }}")
                return
            }
        }
        forwards.computeIfAbsent(device.id) { mutableListOf() }.addAll(handles)
        service<AutoJs6ConnectionService>().connectTo("127.0.0.1", serverLocalPort, device.id, project)
        Thread {
            Thread.sleep(5000)
            val connected = service<AutoJs6ConnectionService>().connectedDevices().any { it.adbDeviceId == device.id }
            if (!connected) {
                val provider = queryDebugServerProvider(adb, device.id)
                AutoJs6Notifier.error(project, "ADB 连接 ${device.id} 超时。$provider")
                cleanupForDevice(device.id)
            }
        }.apply { isDaemon = true; name = "AutoJs6-adb-timeout" }.start()
    }

    fun cleanupForDevice(serial: String) {
        val adb = resolveExecutable() ?: return
        forwards.remove(serial)?.forEach { handle ->
            runCatching { run(adb, "-s", handle.serial, "forward", "--remove", "tcp:${handle.localPort}") }
        }
    }

    fun cleanupAll() { forwards.keys.toList().forEach(::cleanupForDevice) }

    fun queryDebugServerProvider(adb: String, serial: String): String {
        val res = run(adb, "-s", serial, "shell", "content", "query", "--uri", "content://org.autojs.autojs.debug.provider/debug-server")
        val combined = (res.stdout + res.stderr).trim()
        return when {
            combined.contains("Could not find provider", ignoreCase = true) -> "请确认 AutoJs6 已安装且侧拉菜单已开启 Server mode；当前设备未暴露 debug provider。"
            Regex("state=(\\d+)").find(combined)?.groupValues?.get(1) != "2" -> "AutoJs6 debug provider 未报告 running state=2；请开启 Server mode 后重试。provider=$combined"
            combined.isBlank() -> "debug provider 未返回数据；请确认 AutoJs6 Server mode 已开启。"
            else -> "debug provider: $combined"
        }
    }

    private fun run(vararg command: String): ProcResult {
        val pb = ProcessBuilder(*command).redirectErrorStream(false)
        val p = pb.start()
        val stdout = p.inputStream.readBytes().toString(Charsets.UTF_8)
        val stderr = p.errorStream.readBytes().toString(Charsets.UTF_8)
        return ProcResult(p.waitFor(), stdout, stderr)
    }

    private fun canRun(cmd: String): Boolean = runCatching { run(cmd, "version").exitCode == 0 }.getOrDefault(false)

    private fun findAvailablePort(): Int = ServerSocket(0).use { it.localPort }

    companion object {
        fun parseDevices(output: String): List<AdbDeviceInfo> = output.lineSequence().mapNotNull { line ->
            val trimmed = line.trim()
            val m = Regex("^(\\S+)\\s+device\\s+(.+)$").find(trimmed) ?: return@mapNotNull null
            val props = Regex("(\\S+?):([^\\s]+)").findAll(m.groupValues[2]).associate { it.groupValues[1] to it.groupValues[2] }
            AdbDeviceInfo(m.groupValues[1], props)
        }.toList()
    }
}

data class ProcResult(val exitCode: Int, val stdout: String, val stderr: String)

internal object AdbExecutableResolver {
    private val sdkEnvironmentKeys = listOf("ANDROID_HOME", "ANDROID_SDK_ROOT")
    private val homeRelativeSdkRoots = listOf(
        "Library/Android/sdk",
        "Android/Sdk"
    )
    private val unixExecutableLocations = listOf(
        "/opt/homebrew/bin/adb",
        "/usr/local/bin/adb",
        "/opt/local/bin/adb",
        "/usr/bin/adb",
        "/snap/bin/adb"
    )

    fun resolveConfigured(configured: String, windows: Boolean): String? =
        configuredPathCandidates(configured, windows)
            .firstOrNull { isUsableExecutable(it, windows) }
            ?.absolutePath

    fun resolveFromPath(windows: Boolean): String? {
        val executableName = if (windows) "adb.exe" else "adb"
        val pathValues = listOf(
            EnvironmentUtil.getValue("PATH"),
            PathEnvironmentVariableUtil.getPathVariableValue(),
            System.getenv("PATH")
        ).mapNotNull { it?.takeIf(String::isNotBlank) }.distinct()

        return pathValues.firstNotNullOfOrNull { pathValue ->
            PathEnvironmentVariableUtil.findInPath(executableName, pathValue) { file ->
                isUsableExecutable(file, windows)
            }?.absolutePath
        }
    }

    fun resolveFromCommonUnixLocations(): String? =
        commonUnixCandidates(
            envLookup = { name -> EnvironmentUtil.getValue(name) ?: System.getenv(name) },
            userHome = System.getProperty("user.home")
        ).firstOrNull { isUsableExecutable(it, windows = false) }?.absolutePath

    fun configuredPathCandidates(configured: String, windows: Boolean): List<File> {
        if (configured.isBlank()) return emptyList()
        val file = File(configured.trim())
        if (!file.isDirectory) return listOf(file)
        return listOf(File(file, if (windows) "adb.exe" else "adb"))
    }

    fun commonUnixCandidates(envLookup: (String) -> String?, userHome: String): List<File> {
        val sdkRoots = buildList {
            sdkEnvironmentKeys.mapNotNullTo(this) { key -> envLookup(key)?.trim()?.takeIf { it.isNotBlank() } }
            homeRelativeSdkRoots.mapTo(this) { relative -> File(userHome, relative).path }
        }
        return (sdkRoots.map { root -> File(root, "platform-tools/adb") } + unixExecutableLocations.map(::File))
            .distinctBy { it.absolutePath }
    }

    fun isUsableExecutable(file: File, windows: Boolean): Boolean =
        file.isFile && (file.canExecute() || windows)
}
