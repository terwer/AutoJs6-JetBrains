package org.autojs.autojs6.jetbrains.run

import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessOutputTypes
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import org.autojs.autojs6.jetbrains.AutoJs6Notifier
import org.autojs.autojs6.jetbrains.device.AutoJs6ConnectionListener
import org.autojs.autojs6.jetbrains.device.AutoJs6ConnectionService
import org.autojs.autojs6.jetbrains.device.AutoJs6Device
import java.io.OutputStream
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.atomic.AtomicBoolean

internal abstract class AutoJs6LogTailingProcessHandler(
    protected val project: Project,
    private val sessionName: String
) : ProcessHandler(), AutoJs6ConnectionListener {
    protected val connectionService: AutoJs6ConnectionService = service()
    private val terminated = AtomicBoolean(false)
    private val listenerRegistered = AtomicBoolean(false)
    private val targetDeviceKeys = CopyOnWriteArraySet<String>()

    fun startRun() {
        startNotify()
        ApplicationManager.getApplication().executeOnPooledThread {
            runCatching {
                executeRun()
            }.onFailure { t ->
                val message = t.message ?: t.javaClass.simpleName
                notifyTextAvailable("ERROR: $message\n", ProcessOutputTypes.STDERR)
                AutoJs6Notifier.error(project, message)
                terminate(1)
            }
        }
    }

    protected abstract fun executeRun()

    protected fun startLogTailing(devices: List<AutoJs6Device>) {
        replaceLogTargets(devices)
        if (targetDeviceKeys.isEmpty()) return
        if (listenerRegistered.compareAndSet(false, true)) {
            connectionService.addListener(this)
        }
        notifyTextAvailable(
            "$sessionName: device log streaming started for ${targetDeviceKeys.size} device(s). " +
                    "Press Stop to stop remote script/log tail.\n",
            ProcessOutputTypes.STDOUT
        )
    }

    protected fun replaceLogTargets(devices: List<AutoJs6Device>) {
        targetDeviceKeys.clear()
        targetDeviceKeys.addAll(devices.filter { it.attached }.map { it.key() })
    }

    override fun logReceived(device: AutoJs6Device, text: String) {
        if (terminated.get()) return
        if (!targetDeviceKeys.contains(device.key())) return
        notifyTextAvailable(
            formatAutoJs6RunLogText(device.deviceName, device.endpoint(), text),
            ProcessOutputTypes.STDOUT
        )
    }

    override fun deviceDetached(device: AutoJs6Device, reason: String) {
        if (terminated.get()) return
        if (!targetDeviceKeys.remove(device.key())) return
        notifyTextAvailable(
            formatAutoJs6RunLogText(device.deviceName, device.endpoint(), "[disconnect] $reason"),
            ProcessOutputTypes.STDERR
        )
        if (targetDeviceKeys.isEmpty()) terminate(0)
    }

    override fun destroyProcessImpl() {
        val targets = currentTargetDevices()
        runCatching {
            if (targets.isNotEmpty()) onStopRequested(targets)
        }.onFailure { t ->
            notifyTextAvailable(
                "WARN: failed to stop remote AutoJs6 script: ${t.message ?: t.javaClass.simpleName}\n",
                ProcessOutputTypes.STDERR
            )
        }
        terminate(1)
    }

    protected open fun onStopRequested(devices: List<AutoJs6Device>) = Unit

    override fun detachProcessImpl() {
        if (terminated.compareAndSet(false, true)) {
            unregisterLogListener()
            notifyProcessDetached()
        }
    }

    override fun detachIsDefault(): Boolean = false

    override fun getProcessInput(): OutputStream? = null

    protected fun terminate(exitCode: Int) {
        if (terminated.compareAndSet(false, true)) {
            unregisterLogListener()
            notifyProcessTerminated(exitCode)
        }
    }

    private fun currentTargetDevices(): List<AutoJs6Device> =
        connectionService.connectedDevices().filter { targetDeviceKeys.contains(it.key()) }

    private fun unregisterLogListener() {
        if (listenerRegistered.compareAndSet(true, false)) {
            connectionService.removeListener(this)
        }
    }
}

internal fun formatAutoJs6RunLogText(deviceName: String, endpoint: String, text: String): String {
    val prefix = "[$deviceName $endpoint]"
    val normalized = text.replace("\r\n", "\n").replace('\r', '\n').trimEnd('\n')
    if (normalized.isEmpty()) return "$prefix\n"
    return normalized.split('\n').joinToString(separator = "\n", postfix = "\n") { "$prefix $it" }
}
