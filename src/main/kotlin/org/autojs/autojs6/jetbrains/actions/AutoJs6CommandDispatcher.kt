package org.autojs.autojs6.jetbrains.actions

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import org.autojs.autojs6.jetbrains.AutoJs6Notifier
import java.util.concurrent.atomic.AtomicReference


data class AutoJs6DispatchResult(val success: Boolean, val message: String)

object AutoJs6CommandDispatcher {
    const val RERUN_PROJECT_COMMAND = "rerunProject"

    val vscodeParityCommands: List<String> = listOf(
        "viewDocument",
        "connect",
        "disconnectAll",
        "run",
        "runWithoutArguments",
        "commandsHierarchy",
        "runOnDevice",
        "stop",
        "stopAll",
        "rerun",
        "save",
        "saveToDevice",
        "newUntitledFile",
        "newProject",
        "saveProject",
        "saveProjectWithoutArguments",
        "runProject",
        "runProjectWithoutArguments"
    )

    val remoteWhitelist: Set<String> = (vscodeParityCommands + RERUN_PROJECT_COMMAND).toSet()

    fun isWhitelisted(command: String): Boolean = command.trim() in remoteWhitelist

    fun dispatch(project: Project?, command: String, path: String? = null): AutoJs6DispatchResult {
        val normalized = command.trim()
        if (!isWhitelisted(normalized)) {
            return AutoJs6DispatchResult(false, "unknown command; supported=${remoteWhitelist.sorted().joinToString(", ")}")
        }
        if (project == null && normalized !in setOf("viewDocument")) {
            return AutoJs6DispatchResult(false, "no active JetBrains project for command dispatch")
        }
        val result = AtomicReference(AutoJs6DispatchResult(true, "dispatched $normalized"))
        val task = Runnable {
            result.set(runCatching {
                dispatchNow(project, normalized, path)
            }.getOrElse { AutoJs6DispatchResult(false, it.message ?: it.javaClass.simpleName) })
        }
        val app = ApplicationManager.getApplication()
        if (app == null || app.isDispatchThread) task.run() else app.invokeLater(task)
        return result.get()
    }

    fun dispatchNow(project: Project?, command: String, path: String? = null): AutoJs6DispatchResult {
        return when (command) {
            "viewDocument" -> { BrowserUtil.browse(org.autojs.autojs6.jetbrains.AutoJs6Constants.DOC_URL); ok(command) }
            "connect" -> requireProject(project, command) { AutoJs6ActionSupport.showConnectDialog(it); ok(command) }
            "disconnectAll" -> requireProject(project, command) { AutoJs6ActionSupport.disconnectAll(it); ok(command) }
            "run", "runWithoutArguments" -> requireProject(project, command) { AutoJs6ActionSupport.sendCurrentFileCommand(it, null, path, "run", null); ok(command) }
            "runOnDevice" -> requireProject(project, command) { AutoJs6ActionSupport.sendCurrentFileCommand(it, null, path, "run", AutoJs6ActionSupport.chooseDevice(it)); ok(command) }
            "stop" -> requireProject(project, command) { AutoJs6ActionSupport.stopCurrentScript(it, null, path, null); ok(command) }
            "stopAll" -> requireProject(project, command) { AutoJs6ActionSupport.stopAll(it, null); ok(command) }
            "rerun" -> requireProject(project, command) { AutoJs6ActionSupport.rerunCurrentScript(it, null, path); ok(command) }
            "save" -> requireProject(project, command) { AutoJs6ActionSupport.sendCurrentFileCommand(it, null, path, "save", null); ok(command) }
            "saveToDevice" -> requireProject(project, command) { AutoJs6ActionSupport.sendCurrentFileCommand(it, null, path, "save", AutoJs6ActionSupport.chooseDevice(it)); ok(command) }
            "newUntitledFile" -> requireProject(project, command) { AutoJs6ActionSupport.newUntitledFile(it); ok(command) }
            "newProject" -> requireProject(project, command) { AutoJs6ActionSupport.createNewProject(it, null, path); ok(command) }
            "runProject", "runProjectWithoutArguments" -> requireProject(project, command) { AutoJs6ActionSupport.sendProjectCommand(it, null, path, org.autojs.autojs6.jetbrains.project.AUTOJS6_RUN_PROJECT_COMMAND); ok(command) }
            "saveProject", "saveProjectWithoutArguments" -> requireProject(project, command) { AutoJs6ActionSupport.sendProjectCommand(it, null, path, org.autojs.autojs6.jetbrains.project.AUTOJS6_SAVE_PROJECT_COMMAND); ok(command) }
            RERUN_PROJECT_COMMAND -> requireProject(project, command) { AutoJs6ActionSupport.rerunProject(it, null, path); ok(command) }
            "commandsHierarchy" -> requireProject(project, command) { AutoJs6ActionSupport.showCommandsHierarchy(it); ok(command) }
            else -> AutoJs6DispatchResult(false, "unknown command: $command")
        }
    }

    private fun ok(command: String) = AutoJs6DispatchResult(true, "dispatched $command")

    private fun requireProject(project: Project?, command: String, block: (Project) -> AutoJs6DispatchResult): AutoJs6DispatchResult =
        project?.let(block) ?: AutoJs6DispatchResult(false, "command $command requires an active project")
}
