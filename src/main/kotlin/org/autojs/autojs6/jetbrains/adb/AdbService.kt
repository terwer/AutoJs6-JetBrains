package org.autojs.autojs6.jetbrains.adb

import com.intellij.openapi.application.PathManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import org.autojs.autojs6.jetbrains.AutoJs6Constants
import org.autojs.autojs6.jetbrains.AutoJs6Notifier
import org.autojs.autojs6.jetbrains.AutoJs6SettingsService
import org.autojs.autojs6.jetbrains.device.AutoJs6ConnectionService
import java.io.File
import java.net.ServerSocket


data class AdbDeviceInfo(val id: String, val properties: Map<String, String>) {
    val model: String = properties["model"] ?: "Unknown"
    val product: String = properties["product"] ?: "Unknown"
    val name: String = "$model ($id)"
}

@Service(Service.Level.PROJECT)
class AdbService(private val project: Project) {
    fun resolveExecutable(): String? {
        val configured = AutoJs6SettingsService.getInstance().state.adbPath.trim()
        if (configured.isNotEmpty() && File(configured).canExecute()) return configured
        if (canRun("adb")) return "adb"
        if (System.getProperty("os.name").lowercase().contains("win")) {
            val candidates = listOf(
                File(PathManager.getPluginsPath(), "AutoJs6/tools/platform-tools/adb.exe"),
                File(PathManager.getHomePath(), "tools/platform-tools/adb.exe"),
                File("src/main/resources/tools/platform-tools/adb.exe")
            )
            return candidates.firstOrNull { it.canExecute() || it.exists() }?.absolutePath
        }
        return null
    }

    fun listDevices(): List<AdbDeviceInfo> {
        val adb = resolveExecutable() ?: run {
            AutoJs6Notifier.error(project, "ADB 不可用，请配置 ADB 路径或将 adb 加入 PATH")
            return emptyList()
        }
        val result = run(adb, "devices", "-l")
        if (result.exitCode != 0) {
            AutoJs6Notifier.error(project, "执行 adb devices -l 失败: ${result.stderr.ifBlank { result.stdout }}")
            return emptyList()
        }
        return parseDevices(result.stdout)
    }

    fun connectViaAdb(device: AdbDeviceInfo) {
        val adb = resolveExecutable() ?: run {
            AutoJs6Notifier.error(project, "ADB 不可用，请配置 ADB 路径或将 adb 加入 PATH")
            return
        }
        val localPort = findAvailablePort()
        val forward = run(adb, "-s", device.id, "forward", "tcp:$localPort", "tcp:${AutoJs6Constants.ADB_SERVER_PORT}")
        if (forward.exitCode != 0) {
            AutoJs6Notifier.error(project, "ADB forward 失败: ${forward.stderr.ifBlank { forward.stdout }}")
            return
        }
        project.getService(AutoJs6ConnectionService::class.java).connectTo("127.0.0.1", localPort, device.id)
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
            val m = Regex("^(\\S+)\\s+device\\s+(.+)$").find(line.trim()) ?: return@mapNotNull null
            val props = Regex("(\\S+?):([^\\s]+)").findAll(m.groupValues[2]).associate { it.groupValues[1] to it.groupValues[2] }
            AdbDeviceInfo(m.groupValues[1], props)
        }.toList()
    }
}

data class ProcResult(val exitCode: Int, val stdout: String, val stderr: String)
