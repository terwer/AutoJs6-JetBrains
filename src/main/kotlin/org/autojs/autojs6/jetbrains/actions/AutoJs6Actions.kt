package org.autojs.autojs6.jetbrains.actions

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import org.autojs.autojs6.jetbrains.AutoJs6Constants
import org.autojs.autojs6.jetbrains.AutoJs6Notifier
import org.autojs.autojs6.jetbrains.adb.AdbService
import org.autojs.autojs6.jetbrains.device.AutoJs6ConnectionService
import org.autojs.autojs6.jetbrains.project.AutoJs6ProjectTemplateService

class ViewDocumentAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) { BrowserUtil.browse(AutoJs6Constants.DOC_URL) }
}

class ConnectAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val choice = Messages.showChooseDialog(project, "选择 AutoJs6 连接方式", "AutoJs6 Connect", null, arrayOf("IDE 监听客户端连接", "通过 IP 连接服务端", "通过 ADB 连接"), "通过 IP 连接服务端")
        when (choice) {
            0 -> { service<AutoJs6ConnectionService>().startListening(project); AutoJs6Notifier.info(project, "AutoJs6 IDE 监听已启动: ${AutoJs6Constants.IDE_LISTENING_PORT}") }
            1 -> connectByIp(project)
            2 -> connectByAdb(project)
        }
    }
    private fun connectByIp(project: Project) {
        val host = Messages.showInputDialog(project, "输入 AutoJs6 服务端 Host/IP", "AutoJs6 Connect", null) ?: return
        if (host.isBlank()) return
        service<AutoJs6ConnectionService>().connectTo(host.trim(), AutoJs6Constants.SERVER_PORT, project = project)
    }
    private fun connectByAdb(project: Project) {
        val adb = project.getService(AdbService::class.java)
        val devices = adb.listDevices()
        if (devices.isEmpty()) return
        val names = devices.map { it.name }.toTypedArray()
        val selected = Messages.showChooseDialog(project, "选择 ADB 设备", "AutoJs6 ADB Connect", null, names, names.first())
        if (selected >= 0) adb.connectViaAdb(devices[selected])
    }
}

class DisconnectAllAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) { service<AutoJs6ConnectionService>().disconnectAll(e.project) }
}

abstract class CurrentFileCommandAction(private val command: String) : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val payload = currentFilePayload(project, e) ?: return
        val sent = service<AutoJs6ConnectionService>().sendCommand(command, payload)
        if (!sent) AutoJs6Notifier.error(project, "未发现已连接的设备")
    }

    protected open fun currentFilePayload(project: Project, e: AnActionEvent): Map<String, Any?>? {
        val editor = e.getData(CommonDataKeys.EDITOR)
        val doc = editor?.document ?: return noFile(project)
        val vf = FileDocumentManager.getInstance().getFile(doc) ?: e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return noFile(project)
        if (!vf.isInLocalFileSystem) return noFile(project)
        return mapOf("id" to vf.path, "name" to vf.name, "script" to doc.text)
    }

    private fun noFile(project: Project): Map<String, Any?>? { AutoJs6Notifier.error(project, "需在正在编辑的本地文件窗口中使用该命令"); return null }
}

class RunCurrentFileAction : CurrentFileCommandAction("run")
class SaveCurrentFileAction : CurrentFileCommandAction("save")
class StopCurrentScriptAction : CurrentFileCommandAction("stop") {
    override fun currentFilePayload(project: Project, e: AnActionEvent): Map<String, Any?>? = super.currentFilePayload(project, e)?.let { mapOf("id" to it["id"]) }
}

class StopAllScriptsAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val sent = service<AutoJs6ConnectionService>().sendCommand("stopAll")
        if (!sent) AutoJs6Notifier.error(project, "未发现已连接的设备")
    }
}

class NewProjectAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project
        try {
            AutoJs6ProjectTemplateService(project).chooseAndCreate()
        } catch (t: Throwable) {
            AutoJs6Notifier.error(project, "创建 AutoJs6 新项目失败: ${t.message}")
            Messages.showErrorDialog(project, "创建 AutoJs6 新项目失败：\n${t.message}", "New AutoJs6 Project")
        }
    }
}
