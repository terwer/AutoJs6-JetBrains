package org.autojs.autojs6.jetbrains.actions

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileTypes.PlainTextFileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.LightVirtualFile
import org.autojs.autojs6.jetbrains.AutoJs6Constants
import org.autojs.autojs6.jetbrains.AutoJs6Notifier
import org.autojs.autojs6.jetbrains.AutoJs6SettingsService
import org.autojs.autojs6.jetbrains.adb.AdbService
import org.autojs.autojs6.jetbrains.connection.AutoJs6NetworkInterfaces
import org.autojs.autojs6.jetbrains.device.AutoJs6ConnectionService
import org.autojs.autojs6.jetbrains.device.AutoJs6Device
import org.autojs.autojs6.jetbrains.project.AUTOJS6_RUN_PROJECT_COMMAND
import org.autojs.autojs6.jetbrains.project.AUTOJS6_SAVE_PROJECT_COMMAND
import org.autojs.autojs6.jetbrains.project.AutoJs6ProjectSyncService
import org.autojs.autojs6.jetbrains.project.AutoJs6ProjectTemplateService
import org.autojs.autojs6.jetbrains.remote.AutoJs6HttpBridgeService
import org.autojs.autojs6.jetbrains.script.AutoJs6ScriptCommand
import java.net.URI
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import javax.swing.Icon

object AutoJs6ActionSupport {
    private const val RERUN_DELAY_MS = 480L

    fun showConnectDialog(project: Project) {
        val settings = AutoJs6SettingsService.getInstance()
        val choices = mutableListOf(
            "AutoJs6 Client → IDE 监听 (LAN)",
            "AutoJs6 Server → IDE 主动连接 (LAN/IP)",
            "AutoJs6 Server → IDE 主动连接 (ADB)",
            "显示连接诊断摘要"
        )
        if (settings.state.recentHostRecords.isNotEmpty()) choices += "清理最近连接记录"
        when (Messages.showChooseDialog(project, "选择 AutoJs6 连接方式", "AutoJs6 Connect", null as Icon?, choices.toTypedArray(), choices[1])) {
            0 -> showClientLanInstructions(project)
            1 -> connectByLan(project)
            2 -> connectByAdb(project)
            3 -> showDiagnostics(project)
            4 -> clearRecentRecords(project)
        }
    }

    fun disconnectAll(project: Project) = service<AutoJs6ConnectionService>().disconnectAll(project)

    fun showClientLanInstructions(project: Project) {
        val service = service<AutoJs6ConnectionService>()
        service.startListening(project)
        val hints = AutoJs6NetworkInterfaces.listSuitableIpv4Interfaces()
        if (hints.isEmpty()) {
            Messages.showWarningDialog(
                project,
                "未发现合适的本地 IPv4 地址。请确认 IDE 所在设备已连接 LAN，且防火墙允许 ${AutoJs6SettingsService.getInstance().state.listeningPort} 端口。",
                "AutoJs6 Client Connection"
            )
            return
        }
        val labels = hints.map { "${it.address} — ${it.displayName} (${it.name})" }.toTypedArray()
        val selected = if (labels.size == 1) 0 else Messages.showChooseDialog(project, "选择 IDE listening address", "AutoJs6 Client Connection", null as Icon?, labels, labels.first())
        if (selected < 0) return
        val hint = hints[selected]
        val message = hint.instruction(AutoJs6SettingsService.getInstance().state.listeningPort) +
            "\n\n可复制地址: ${hint.address}:${AutoJs6SettingsService.getInstance().state.listeningPort}" +
            if (AutoJs6SettingsService.getInstance().state.qrHintEnabled) "\nQR/copyable hint: 当前以可复制文本形式提供。" else ""
        AutoJs6Notifier.info(project, message)
        Messages.showInfoMessage(project, message, "AutoJs6 Client Connection")
    }

    fun connectByLan(project: Project) {
        val settings = AutoJs6SettingsService.getInstance()
        val recent = settings.state.recentHostRecords.joinToString("\n") { "- ${it.host}" }
        val prompt = buildString {
            append("输入 AutoJs6 服务端 Host/IP，可包含端口，例如 192.168.1.2:${settings.state.serverPort}")
            if (recent.isNotBlank()) append("\n\nRecent records:\n$recent")
        }
        val input = Messages.showInputDialog(project, prompt, "AutoJs6 LAN Connect", null) ?: return
        val parsed = AutoJs6NetworkInterfaces.parseHostPort(input, settings.state.serverPort)
        if (parsed == null) {
            AutoJs6Notifier.error(project, "无法解析 host:port: $input")
            return
        }
        service<AutoJs6ConnectionService>().connectTo(parsed.host, parsed.port, project = project)
    }

    fun connectByAdb(project: Project) {
        val adb = project.getService(AdbService::class.java)
        val devices = adb.listDevices()
        if (devices.isEmpty()) return
        val names = devices.map { "${it.name} — ${it.detail}" }.toTypedArray()
        val selected = Messages.showChooseDialog(project, "选择 ADB 设备", "AutoJs6 ADB Connect", null as Icon?, names, names.first())
        if (selected >= 0) adb.connectViaAdb(devices[selected])
    }

    fun clearRecentRecords(project: Project) {
        val yes = Messages.showYesNoDialog(project, "确认清除所有 AutoJs6 最近连接记录吗？", "AutoJs6 Recent Records", null as Icon?)
        if (yes == Messages.YES) {
            val count = AutoJs6SettingsService.getInstance().clearRecentHosts()
            AutoJs6Notifier.info(project, "已清理 $count 条最近连接记录")
        }
    }

    fun sendCurrentFileCommand(project: Project, e: AnActionEvent?, path: String?, command: String, targetDevice: AutoJs6Device?) {
        val payload = currentFilePayload(project, e, path) ?: return
        val service = service<AutoJs6ConnectionService>()
        val sent = if (targetDevice != null) {
            if (service.sendCommandToDevice(targetDevice, command, payload)) 1 else 0
        } else {
            service.sendCommandToConnectedDevices(command, payload)
        }
        if (sent == 0) AutoJs6Notifier.error(project, "未发现已连接的设备")
        else AutoJs6Notifier.info(project, "AutoJs6 command=$command 已发送到 $sent 个设备: ${payload["name"]}")
    }

    fun stopCurrentScript(project: Project, e: AnActionEvent?, path: String?, targetDevice: AutoJs6Device?) {
        val payload = currentFilePayload(project, e, path) ?: return
        val stopData = mapOf("id" to payload["id"])
        val service = service<AutoJs6ConnectionService>()
        val sent = if (targetDevice != null) {
            if (service.sendCommandToDevice(targetDevice, "stop", stopData)) 1 else 0
        } else {
            service.sendCommandToConnectedDevices("stop", stopData)
        }
        if (sent == 0) AutoJs6Notifier.error(project, "未发现已连接的设备")
    }

    fun stopAll(project: Project, targetDevice: AutoJs6Device?) {
        val service = service<AutoJs6ConnectionService>()
        val sent = if (targetDevice != null) {
            if (service.sendCommandToDevice(targetDevice, "stopAll")) 1 else 0
        } else {
            service.sendCommandToConnectedDevices("stopAll")
        }
        if (sent == 0) AutoJs6Notifier.error(project, "未发现已连接的设备")
    }

    fun rerunCurrentScript(project: Project, e: AnActionEvent?, path: String?) {
        val payload = currentFilePayload(project, e, path) ?: return
        val service = service<AutoJs6ConnectionService>()
        val stopSent = service.sendCommandToConnectedDevices("stop", mapOf("id" to payload["id"]))
        if (stopSent == 0) {
            AutoJs6Notifier.error(project, "未发现已连接的设备")
            return
        }
        ApplicationManager.getApplication().executeOnPooledThread {
            Thread.sleep(RERUN_DELAY_MS)
            service.sendCommandToConnectedDevices("run", payload)
            AutoJs6Notifier.info(project, "AutoJs6 rerun 已完成 stop→run: ${payload["name"]}")
        }
    }

    fun rerunProject(project: Project, e: AnActionEvent?, path: String?) {
        stopAll(project, null)
        ApplicationManager.getApplication().executeOnPooledThread {
            Thread.sleep(RERUN_DELAY_MS)
            ApplicationManager.getApplication().invokeLater { sendProjectCommand(project, e, path, AUTOJS6_RUN_PROJECT_COMMAND) }
        }
    }

    fun sendProjectCommand(project: Project, e: AnActionEvent?, path: String?, command: String) {
        val root = resolveProjectRoot(project, e, path)
        if (root == null) {
            AutoJs6Notifier.error(project, "需要 AutoJs6 project：当前上下文缺少 project.json")
            return
        }
        val devices = service<AutoJs6ConnectionService>().connectedDevices()
        AutoJs6ProjectSyncService.getInstance().runProjectSyncInBackground(project, root, command, devices)
    }

    fun chooseDevice(project: Project): AutoJs6Device? {
        val connectionService = service<AutoJs6ConnectionService>()
        connectionService.selectedConnectedDevice()?.let { return it }
        val devices = connectionService.connectedDevices()
        if (devices.isEmpty()) {
            AutoJs6Notifier.error(project, "未发现已连接的设备")
            return null
        }
        if (devices.size == 1) return devices.single()
        val labels = devices.map { "${it.deviceName} — ${it.connectionType} — ${it.endpoint()}" }.toTypedArray()
        val selected = Messages.showChooseDialog(project, "选择目标 AutoJs6 设备", "AutoJs6 Device Target", null as Icon?, labels, labels.first())
        return if (selected >= 0) devices[selected] else null
    }

    fun disconnectSelectedDevice(project: Project) {
        val device = chooseDevice(project) ?: return
        device.close()
        AutoJs6Notifier.info(project, "已断开设备: ${device.endpoint()}")
    }

    fun showDeviceDiagnostics(project: Project) {
        val device = service<AutoJs6ConnectionService>().selectedConnectedDevice() ?: chooseDevice(project) ?: return
        Messages.showInfoMessage(project, device.snapshot().toString(), "AutoJs6 Device Diagnostics")
    }

    fun showDiagnostics(project: Project) {
        val adb = project.getService(AdbService::class.java).resolveExecutable() ?: "unavailable"
        val http = service<AutoJs6HttpBridgeService>().diagnosticLine()
        val summary = service<AutoJs6ConnectionService>().diagnosticSummary() + "\nADB executable: $adb\nHTTP bridge runtime: $http"
        Messages.showInfoMessage(project, summary, "AutoJs6 Diagnostics")
    }

    fun showDebugHelp(project: Project) {
        val message = """
            AutoJs6 Debug Boundary
            - Run/Save/Stop actions send AutoJs6 device commands and do not attach a full JavaScript debugger.
            - JetBrains JavaScript breakpoints remain IDE-side editor markers for now; AutoJs6 runtime stepping/breakpoint attach requires a future debug adapter integration.
            - Device connection and command dispatch services are kept reusable for future debugger protocol support.
        """.trimIndent()
        Messages.showInfoMessage(project, message, "AutoJs6 Debug Help")
    }

    fun startHttpBridge(project: Project, compatibilityMode: Boolean = false) {
        val settings = AutoJs6SettingsService.getInstance().state
        if (compatibilityMode) {
            val choice = Messages.showYesNoDialog(
                project,
                "Compatibility mode 会将 HTTP bridge 绑定到 0.0.0.0:${settings.httpBridgePort}，与 VSCode 行为更接近，但会暴露到更宽网络。确认启用吗？",
                "AutoJs6 HTTP Bridge Compatibility Mode",
                null as Icon?
            )
            if (choice != Messages.YES) return
            settings.httpBridgeBindHost = "0.0.0.0"
            settings.httpBridgeCompatibilityMode = true
        } else {
            settings.httpBridgeBindHost = "127.0.0.1"
            settings.httpBridgeCompatibilityMode = false
        }
        settings.httpBridgeEnabled = true
        service<AutoJs6HttpBridgeService>().start(project, settings.httpBridgeBindHost, settings.httpBridgePort, settings.httpBridgeCompatibilityMode)
    }

    fun stopHttpBridge(project: Project) {
        AutoJs6SettingsService.getInstance().state.httpBridgeEnabled = false
        service<AutoJs6HttpBridgeService>().stop(project)
    }

    fun showCommandsHierarchy(project: Project) {
        val labels = arrayOf(
            "Connect Device",
            "Run Current Script",
            "Run Current Script on Selected Device",
            "Rerun Current Script",
            "Save Current Script",
            "Save Current Script to Selected Device",
            "Stop Current Script",
            "Stop All Scripts",
            "Run Project",
            "Save Project",
            "New Untitled Script",
            "New AutoJs6 Project",
            "Diagnostics",
            "Debug Help",
            "Start HTTP Bridge (safe loopback)",
            "Stop HTTP Bridge",
            "View Online Document"
        )
        when (Messages.showChooseDialog(project, "选择 AutoJs6 command", "AutoJs6 Commands", null as Icon?, labels, labels[1])) {
            0 -> showConnectDialog(project)
            1 -> sendCurrentFileCommand(project, null, null, "run", null)
            2 -> sendCurrentFileCommand(project, null, null, "run", chooseDevice(project))
            3 -> rerunCurrentScript(project, null, null)
            4 -> sendCurrentFileCommand(project, null, null, "save", null)
            5 -> sendCurrentFileCommand(project, null, null, "save", chooseDevice(project))
            6 -> stopCurrentScript(project, null, null, null)
            7 -> stopAll(project, null)
            8 -> sendProjectCommand(project, null, null, AUTOJS6_RUN_PROJECT_COMMAND)
            9 -> sendProjectCommand(project, null, null, AUTOJS6_SAVE_PROJECT_COMMAND)
            10 -> newUntitledFile(project)
            11 -> createNewProject(project, null, null)
            12 -> showDiagnostics(project)
            13 -> showDebugHelp(project)
            14 -> startHttpBridge(project, false)
            15 -> stopHttpBridge(project)
            16 -> BrowserUtil.browse(AutoJs6Constants.DOC_URL)
        }
    }

    fun newUntitledFile(project: Project) {
        val file = LightVirtualFile("untitled-autojs6-${System.currentTimeMillis()}.js", PlainTextFileType.INSTANCE, "")
        file.isWritable = true
        FileEditorManager.getInstance(project).openFile(file, true)
    }

    fun createNewProject(project: Project, e: AnActionEvent?, path: String?) {
        val target = pathToNioPath(path) ?: e?.getData(CommonDataKeys.VIRTUAL_FILE)?.takeIf { it.isDirectory && it.isInLocalFileSystem }?.let { Path.of(it.path) }
        try {
            if (target != null) {
                AutoJs6ProjectTemplateService(project).createProject(target)
                AutoJs6Notifier.info(project, "AutoJs6 项目已创建: $target")
            } else {
                AutoJs6ProjectTemplateService(project).chooseAndCreate()
            }
        } catch (t: Throwable) {
            AutoJs6Notifier.error(project, "创建 AutoJs6 新项目失败: ${t.message}")
            Messages.showErrorDialog(project, "创建 AutoJs6 新项目失败：\n${t.message}", "New AutoJs6 Project")
        }
    }

    private fun currentFilePayload(project: Project, e: AnActionEvent?, path: String?): Map<String, Any?>? {
        if (!path.isNullOrBlank()) return payloadFromPath(project, path)
        val editor = e?.getData(CommonDataKeys.EDITOR) ?: FileEditorManager.getInstance(project).selectedTextEditor
        val doc = editor?.document
        val vfFromDoc = doc?.let { FileDocumentManager.getInstance().getFile(it) }
        val vf = e?.getData(CommonDataKeys.VIRTUAL_FILE) ?: vfFromDoc
        if (vf == null) {
            AutoJs6Notifier.error(project, "需在正在编辑或选中的本地 .js 文件中使用该命令")
            return null
        }
        return payloadFromVirtualFile(project, vf, doc)
    }

    private fun payloadFromPath(project: Project, path: String): Map<String, Any?>? {
        val p = pathToNioPath(path) ?: run {
            AutoJs6Notifier.error(project, "脚本路径无效: $path")
            return null
        }
        val validation = AutoJs6ScriptCommand.validateLocalJsPath(p.toString())
        if (!validation.valid) {
            AutoJs6Notifier.error(project, validation.message)
            return null
        }
        return AutoJs6ScriptCommand.readLocalJsPayload(p.toString()).toCommandData()
    }

    private fun payloadFromVirtualFile(project: Project, vf: VirtualFile, doc: Document?): Map<String, Any?>? {
        val validation = AutoJs6ScriptCommand.validateVirtualJsFile(vf)
        if (!validation.valid) {
            AutoJs6Notifier.error(project, validation.message)
            return null
        }
        val text = doc?.text ?: ReadAction.compute<String, Throwable> { vf.inputStream.use { it.readBytes().toString(vf.charset) } }
        return AutoJs6ScriptCommand.createSingleFilePayload(vf, text).toCommandData()
    }

    private fun resolveProjectRoot(project: Project, e: AnActionEvent?, path: String?): Path? {
        val explicit = pathToNioPath(path)
        if (explicit != null) return AutoJs6ProjectSyncService.resolveProjectRoot(explicit, true)
        e?.getData(CommonDataKeys.VIRTUAL_FILE)?.let { return AutoJs6ProjectSyncService.resolveProjectRoot(Path.of(it.path), true) }
        FileEditorManager.getInstance(project).selectedTextEditor?.document
            ?.let { FileDocumentManager.getInstance().getFile(it) }
            ?.let { return AutoJs6ProjectSyncService.resolveProjectRoot(Path.of(it.path), true) }
        project.basePath?.let { return AutoJs6ProjectSyncService.resolveProjectRoot(Path.of(it), true) }
        return null
    }

    private fun pathToNioPath(path: String?): Path? {
        if (path.isNullOrBlank()) return null
        return runCatching {
            if (path.startsWith("file:", ignoreCase = true)) Path.of(URI(path)) else Path.of(path)
        }.getOrNull()
    }
}

class ViewDocumentAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) { BrowserUtil.browse(AutoJs6Constants.DOC_URL) }
}

class ConnectAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) { e.project?.let { AutoJs6ActionSupport.showConnectDialog(it) } }
}

class DisconnectAllAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) { e.project?.let { AutoJs6ActionSupport.disconnectAll(it) } }
}

abstract class CurrentFileCommandAction(private val command: String, private val selectedDevice: Boolean = false) : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        AutoJs6ActionSupport.sendCurrentFileCommand(project, e, null, command, if (selectedDevice) AutoJs6ActionSupport.chooseDevice(project) else null)
    }
}

class RunCurrentFileAction : CurrentFileCommandAction("run")
class RunWithoutArgumentsAction : CurrentFileCommandAction("run")
class RunOnDeviceAction : CurrentFileCommandAction("run", true)
class SaveCurrentFileAction : CurrentFileCommandAction("save")
class SaveToDeviceAction : CurrentFileCommandAction("save", true)

class StopCurrentScriptAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) { e.project?.let { AutoJs6ActionSupport.stopCurrentScript(it, e, null, null) } }
}

class StopAllScriptsAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) { e.project?.let { AutoJs6ActionSupport.stopAll(it, null) } }
}

class RerunAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) { e.project?.let { AutoJs6ActionSupport.rerunCurrentScript(it, e, null) } }
}

class NewUntitledFileAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) { e.project?.let { AutoJs6ActionSupport.newUntitledFile(it) } }
}

class NewProjectAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) { e.project?.let { AutoJs6ActionSupport.createNewProject(it, e, null) } }
}

class RunProjectAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) { e.project?.let { AutoJs6ActionSupport.sendProjectCommand(it, e, null, AUTOJS6_RUN_PROJECT_COMMAND) } }
}

class RunProjectWithoutArgumentsAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) { e.project?.let { AutoJs6ActionSupport.sendProjectCommand(it, e, null, AUTOJS6_RUN_PROJECT_COMMAND) } }
}

class SaveProjectAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) { e.project?.let { AutoJs6ActionSupport.sendProjectCommand(it, e, null, AUTOJS6_SAVE_PROJECT_COMMAND) } }
}

class SaveProjectWithoutArgumentsAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) { e.project?.let { AutoJs6ActionSupport.sendProjectCommand(it, e, null, AUTOJS6_SAVE_PROJECT_COMMAND) } }
}

class CommandsHierarchyAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) { e.project?.let { AutoJs6ActionSupport.showCommandsHierarchy(it) } }
}

class DiagnosticsSummaryAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) { e.project?.let { AutoJs6ActionSupport.showDiagnostics(it) } }
}

class DebugHelpAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) { e.project?.let { AutoJs6ActionSupport.showDebugHelp(it) } }
}

class StartHttpBridgeAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) { e.project?.let { AutoJs6ActionSupport.startHttpBridge(it, false) } }
}

class StartHttpBridgeCompatibilityAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) { e.project?.let { AutoJs6ActionSupport.startHttpBridge(it, true) } }
}

class StopHttpBridgeAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) { e.project?.let { AutoJs6ActionSupport.stopHttpBridge(it) } }
}

class DisconnectSelectedDeviceAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) { e.project?.let { AutoJs6ActionSupport.disconnectSelectedDevice(it) } }
}

class RunSelectedDeviceAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) { e.project?.let { AutoJs6ActionSupport.sendCurrentFileCommand(it, e, null, "run", AutoJs6ActionSupport.chooseDevice(it)) } }
}

class SaveSelectedDeviceAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) { e.project?.let { AutoJs6ActionSupport.sendCurrentFileCommand(it, e, null, "save", AutoJs6ActionSupport.chooseDevice(it)) } }
}

class StopSelectedDeviceAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) { e.project?.let { AutoJs6ActionSupport.stopCurrentScript(it, e, null, AutoJs6ActionSupport.chooseDevice(it)) } }
}

class DeviceDiagnosticsAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) { e.project?.let { AutoJs6ActionSupport.showDeviceDiagnostics(it) } }
}
