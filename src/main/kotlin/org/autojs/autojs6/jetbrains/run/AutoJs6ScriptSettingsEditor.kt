package org.autojs.autojs6.jetbrains.run

import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.border.EmptyBorder

class AutoJs6ScriptSettingsEditor(private val project: Project) : SettingsEditor<AutoJs6ScriptRunConfiguration>() {
    private val scriptPathField = TextFieldWithBrowseButton()
    private val panel = JPanel(BorderLayout(8, 0)).apply {
        border = EmptyBorder(8, 8, 8, 8)
        add(JLabel("Script .js file:"), BorderLayout.WEST)
        add(scriptPathField, BorderLayout.CENTER)
    }

    init {
        val descriptor = FileChooserDescriptorFactory.createSingleFileDescriptor("js")
            .withTitle("选择 AutoJs6 JS 脚本")
            .withDescription("选择一个本地 .js 文件；项目 Run Configuration 尚未实现。")
        scriptPathField.addActionListener {
            val selected = FileChooser.chooseFile(descriptor, project, null)
            if (selected != null) {
                scriptPathField.text = selected.path
            }
        }
    }

    override fun resetEditorFrom(configuration: AutoJs6ScriptRunConfiguration) {
        scriptPathField.text = configuration.scriptPath
    }

    override fun applyEditorTo(configuration: AutoJs6ScriptRunConfiguration) {
        configuration.scriptPath = scriptPathField.text.trim()
    }

    override fun createEditor(): JComponent = panel
}
