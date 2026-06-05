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
import org.autojs.autojs6.jetbrains.script.AutoJs6ScriptCommand
import java.io.OutputStream
import java.util.concurrent.atomic.AtomicBoolean

class AutoJs6ScriptRunProfileState(
    private val project: Project,
    private val scriptPath: String
) : RunProfileState {
    override fun execute(executor: com.intellij.execution.Executor, runner: ProgramRunner<*>): ExecutionResult {
        val console = TextConsoleBuilderFactory.getInstance().createBuilder(project).console
        val handler = AutoJs6ScriptProcessHandler(project, scriptPath)
        console.attachToProcess(handler)
        handler.startScriptRun()
        return DefaultExecutionResult(console, handler)
    }
}

private class AutoJs6ScriptProcessHandler(
    private val project: Project,
    private val scriptPath: String
) : ProcessHandler() {
    private val terminated = AtomicBoolean(false)

    fun startScriptRun() {
        startNotify()
        ApplicationManager.getApplication().executeOnPooledThread {
            val exitCode = runCatching {
                executeScriptRun()
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

    private fun executeScriptRun() {
        val payload = try {
            AutoJs6ScriptCommand.readLocalJsPayload(scriptPath)
        } catch (t: Throwable) {
            throw IllegalStateException(t.message ?: "脚本文件不可读取")
        }

        val service = service<AutoJs6ConnectionService>()
        val sentCount = service.sendCommandToConnectedDevices(
            AutoJs6ScriptCommand.RUN_COMMAND,
            payload.toCommandData()
        )
        if (sentCount <= 0) {
            throw IllegalStateException("未发现已连接的设备")
        }

        notifyTextAvailable(
            "AutoJs6 Script: sent command=run to $sentCount device(s)\n" +
                "id=${payload.id}\n" +
                "name=${payload.name}\n",
            ProcessOutputTypes.STDOUT
        )
        AutoJs6Notifier.info(project, "AutoJs6 Script 已发送到 $sentCount 个设备: ${payload.name}")
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
