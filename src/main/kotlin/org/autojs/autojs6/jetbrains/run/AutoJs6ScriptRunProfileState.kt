package org.autojs.autojs6.jetbrains.run

import com.intellij.execution.DefaultExecutionResult
import com.intellij.execution.ExecutionResult
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.filters.TextConsoleBuilderFactory
import com.intellij.execution.process.ProcessOutputTypes
import com.intellij.execution.runners.ProgramRunner
import com.intellij.openapi.project.Project
import org.autojs.autojs6.jetbrains.AutoJs6Notifier
import org.autojs.autojs6.jetbrains.device.AutoJs6Device
import org.autojs.autojs6.jetbrains.script.AutoJs6ScriptCommand

class AutoJs6ScriptRunProfileState(
    private val project: Project,
    private val scriptPath: String
) : RunProfileState {
    override fun execute(executor: com.intellij.execution.Executor, runner: ProgramRunner<*>): ExecutionResult {
        val console = TextConsoleBuilderFactory.getInstance().createBuilder(project).console
        val handler = AutoJs6ScriptProcessHandler(project, scriptPath)
        console.attachToProcess(handler)
        handler.startRun()
        return DefaultExecutionResult(console, handler)
    }
}

private class AutoJs6ScriptProcessHandler(
    project: Project,
    private val scriptPath: String
) : AutoJs6LogTailingProcessHandler(project, "AutoJs6 Script") {
    @Volatile
    private var runningScriptId: String? = null

    override fun executeRun() {
        val payload = try {
            AutoJs6ScriptCommand.readLocalJsPayload(scriptPath)
        } catch (t: Throwable) {
            throw IllegalStateException(t.message ?: "脚本文件不可读取")
        }
        runningScriptId = payload.id

        val devices = connectionService.connectedDevices()
        if (devices.isEmpty()) {
            throw IllegalStateException("未发现已连接的设备")
        }
        startLogTailing(devices)

        val commandData = payload.toCommandData()
        val sentCount = devices.count { device ->
            connectionService.sendCommandToDevice(device, AutoJs6ScriptCommand.RUN_COMMAND, commandData)
        }
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

    override fun onStopRequested(devices: List<AutoJs6Device>) {
        val scriptId = runningScriptId ?: return
        val stopData = mapOf("id" to scriptId)
        var sent = 0
        devices.forEach { device ->
            if (connectionService.sendCommandToDevice(device, "stop", stopData)) sent++
        }
        notifyTextAvailable("AutoJs6 Script: stop sent to $sent device(s), id=$scriptId\n", ProcessOutputTypes.STDOUT)
    }
}
