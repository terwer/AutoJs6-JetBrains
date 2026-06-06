package org.autojs.autojs6.jetbrains.run

import com.intellij.execution.DefaultExecutionResult
import com.intellij.execution.ExecutionResult
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.filters.TextConsoleBuilderFactory
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessOutputTypes
import com.intellij.execution.runners.ProgramRunner
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import org.autojs.autojs6.jetbrains.AutoJs6Notifier
import org.autojs.autojs6.jetbrains.device.AutoJs6ConnectionService
import org.autojs.autojs6.jetbrains.project.AUTOJS6_RUN_PROJECT_COMMAND
import org.autojs.autojs6.jetbrains.project.AutoJs6ProjectSyncService
import java.io.OutputStream
import java.util.concurrent.atomic.AtomicBoolean

class AutoJs6ProjectRunProfileState(
    private val project: Project,
    private val projectRootPath: String
) : RunProfileState {
    override fun execute(executor: com.intellij.execution.Executor, runner: ProgramRunner<*>): ExecutionResult {
        val console = TextConsoleBuilderFactory.getInstance().createBuilder(project).console
        val handler = AutoJs6ProjectProcessHandler(project, projectRootPath)
        console.attachToProcess(handler)
        handler.startProjectRun()
        return DefaultExecutionResult(console, handler)
    }
}

private class AutoJs6ProjectProcessHandler(
    private val project: Project,
    private val projectRootPath: String
) : ProcessHandler() {
    private val terminated = AtomicBoolean(false)

    fun startProjectRun() {
        startNotify()
        ApplicationManager.getApplication().executeOnPooledThread {
            val exitCode = runCatching {
                executeProjectRun()
                0
            }.getOrElse { t ->
                val message = t.message ?: t.javaClass.simpleName
                notifyTextAvailable("ERROR: $message\n", ProcessOutputTypes.STDERR)
                AutoJs6Notifier.error(project, message)
                1
            }
            terminate(exitCode)
        }
    }

    private fun executeProjectRun() {
        val root = AutoJs6ProjectRunConfiguration.resolveProjectRoot(projectRootPath)
            ?: throw IllegalStateException("请选择包含 project.json 的 AutoJs6 项目目录")

        val devices = service<AutoJs6ConnectionService>().connectedDevices()
        if (devices.isEmpty()) {
            throw IllegalStateException("未发现已连接的设备")
        }

        val result = AutoJs6ProjectSyncService.getInstance()
            .sendProjectCommand(root, AUTOJS6_RUN_PROJECT_COMMAND, devices)
        result.failures.forEach { notifyTextAvailable("WARN: $it\n", ProcessOutputTypes.STDERR) }
        if (result.sent <= 0) {
            throw IllegalStateException(result.failures.firstOrNull() ?: "AutoJs6 project command 未发送到任何设备")
        }

        notifyTextAvailable(
            "AutoJs6 Project: sent command=run_project to ${result.sent} device(s)\n" +
                "root=$root\n",
            ProcessOutputTypes.STDOUT
        )
        AutoJs6Notifier.info(project, "AutoJs6 Project 已发送到 ${result.sent} 个设备: $root")
    }

    override fun destroyProcessImpl() {
        terminate(1)
    }

    override fun detachProcessImpl() {
        if (terminated.compareAndSet(false, true)) {
            notifyProcessDetached()
        }
    }

    override fun detachIsDefault(): Boolean = false

    override fun getProcessInput(): OutputStream? = null

    private fun terminate(exitCode: Int) {
        if (terminated.compareAndSet(false, true)) {
            notifyProcessTerminated(exitCode)
        }
    }
}
