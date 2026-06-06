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
import org.autojs.autojs6.jetbrains.project.AUTOJS6_RUN_PROJECT_COMMAND
import org.autojs.autojs6.jetbrains.project.AutoJs6ProjectSyncService

class AutoJs6ProjectRunProfileState(
    private val project: Project,
    private val projectRootPath: String
) : RunProfileState {
    override fun execute(executor: com.intellij.execution.Executor, runner: ProgramRunner<*>): ExecutionResult {
        val console = TextConsoleBuilderFactory.getInstance().createBuilder(project).console
        val handler = AutoJs6ProjectProcessHandler(project, projectRootPath)
        console.attachToProcess(handler)
        handler.startRun()
        return DefaultExecutionResult(console, handler)
    }
}

private class AutoJs6ProjectProcessHandler(
    project: Project,
    private val projectRootPath: String
) : AutoJs6LogTailingProcessHandler(project, "AutoJs6 Project") {
    override fun executeRun() {
        val root = AutoJs6ProjectRunConfiguration.resolveProjectRoot(projectRootPath)
            ?: throw IllegalStateException("请选择包含 project.json 的 AutoJs6 项目目录")

        val devices = connectionService.connectedDevices()
        if (devices.isEmpty()) {
            throw IllegalStateException("未发现已连接的设备")
        }
        startLogTailing(devices)

        val result = AutoJs6ProjectSyncService.getInstance()
            .sendProjectCommand(root, AUTOJS6_RUN_PROJECT_COMMAND, devices)
        replaceLogTargets(result.sentDevices)
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

    override fun onStopRequested(devices: List<AutoJs6Device>) {
        var sent = 0
        devices.forEach { device ->
            if (connectionService.sendCommandToDevice(device, "stopAll")) sent++
        }
        notifyTextAvailable("AutoJs6 Project: stopAll sent to $sent device(s)\n", ProcessOutputTypes.STDOUT)
    }
}
