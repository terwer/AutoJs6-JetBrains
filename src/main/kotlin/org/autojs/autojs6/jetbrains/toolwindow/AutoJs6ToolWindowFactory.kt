package org.autojs.autojs6.jetbrains.toolwindow

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import org.autojs.autojs6.jetbrains.actions.AutoJs6ActionSupport
import org.autojs.autojs6.jetbrains.device.AutoJs6ConnectionListener
import org.autojs.autojs6.jetbrains.device.AutoJs6ConnectionService
import org.autojs.autojs6.jetbrains.device.AutoJs6Device
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JSplitPane
import javax.swing.JTable
import javax.swing.JTextArea
import javax.swing.ListSelectionModel
import javax.swing.SwingUtilities
import javax.swing.table.DefaultTableModel

class AutoJs6ToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val panel = AutoJs6ToolWindowPanel(project)
        val content = ContentFactory.getInstance().createContent(panel.component, "Devices", false)
        toolWindow.contentManager.addContent(content)
        Disposer.register(project, panel)
    }

    override fun shouldBeAvailable(project: Project): Boolean = true
}

private class AutoJs6ToolWindowPanel(private val project: Project) : Disposable, AutoJs6ConnectionListener {
    private val service = service<AutoJs6ConnectionService>()
    private val tableModel = object : DefaultTableModel(arrayOf("Name", "Type", "Endpoint", "Status", "Version"), 0) {
        override fun isCellEditable(row: Int, column: Int): Boolean = false
    }
    private val table = JTable(tableModel)
    private val logArea = JTextArea()
    val component: JPanel = JPanel(BorderLayout())

    init {
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
        table.preferredScrollableViewportSize = Dimension(600, 160)
        table.selectionModel.addListSelectionListener {
            if (!it.valueIsAdjusting) {
                val row = table.selectedRow
                val key = if (row >= 0) table.getValueAt(row, 0)?.toString() else null
                val snapshot = service.deviceSnapshots().firstOrNull { snap -> snap.name == key }
                service.selectDevice(snapshot?.key)
            }
        }
        logArea.isEditable = false
        val buttons = JPanel()
        fun addButton(text: String, action: () -> Unit) = buttons.add(JButton(text).apply { addActionListener { action() } })
        addButton("Run") { AutoJs6ActionSupport.sendCurrentFileCommand(project, null, null, "run", service.selectedConnectedDevice()) }
        addButton("Save") { AutoJs6ActionSupport.sendCurrentFileCommand(project, null, null, "save", service.selectedConnectedDevice()) }
        addButton("Stop") { AutoJs6ActionSupport.stopCurrentScript(project, null, null, service.selectedConnectedDevice()) }
        addButton("Disconnect") { service.selectedConnectedDevice()?.close() }
        addButton("Diagnostics") { AutoJs6ActionSupport.showDiagnostics(project) }
        addButton("Debug Help") { AutoJs6ActionSupport.showDebugHelp(project) }

        val top = JPanel(BorderLayout())
        top.add(JScrollPane(table), BorderLayout.CENTER)
        top.add(buttons, BorderLayout.SOUTH)
        val split = JSplitPane(JSplitPane.VERTICAL_SPLIT, top, JScrollPane(logArea))
        split.resizeWeight = 0.45
        component.add(split, BorderLayout.CENTER)
        service.addListener(this)
        refreshDevices()
    }

    override fun deviceAttached(device: AutoJs6Device) = refreshDevices()
    override fun deviceDetached(device: AutoJs6Device, reason: String) {
        appendLog(device, "[disconnect] $reason")
        refreshDevices()
    }
    override fun logReceived(device: AutoJs6Device, text: String) = appendLog(device, text)

    private fun refreshDevices() {
        SwingUtilities.invokeLater {
            val selected = service.selectedConnectedDevice()?.key()
            tableModel.setRowCount(0)
            service.deviceSnapshots().forEach { snap ->
                tableModel.addRow(arrayOf(snap.name, snap.connectionType, snap.endpoint, snap.status, snap.version))
            }
            val rowToSelect = service.deviceSnapshots().indexOfFirst { it.key == selected }
            if (rowToSelect >= 0) table.setRowSelectionInterval(rowToSelect, rowToSelect)
        }
    }

    private fun appendLog(device: AutoJs6Device, text: String) {
        SwingUtilities.invokeLater {
            logArea.append("[${device.deviceName} ${device.endpoint()}] $text\n")
            logArea.caretPosition = logArea.document.length
        }
    }

    override fun dispose() { service.removeListener(this) }
}
