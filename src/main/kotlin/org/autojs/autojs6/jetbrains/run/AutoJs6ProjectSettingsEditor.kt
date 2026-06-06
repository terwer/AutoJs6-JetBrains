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

class AutoJs6ProjectSettingsEditor(private val project: Project) : SettingsEditor<AutoJs6ProjectRunConfiguration>() {
    private val projectRootField = TextFieldWithBrowseButton()
    private val panel = JPanel(BorderLayout(8, 0)).apply {
        border = EmptyBorder(8, 8, 8, 8)
        add(JLabel("Project root:"), BorderLayout.WEST)
        add(projectRootField, BorderLayout.CENTER)
    }

    init {
        val descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor()
            .withTitle("选择 AutoJs6 项目目录")
            .withDescription("选择包含 project.json 的 AutoJs6 项目根目录。")
        projectRootField.addActionListener {
            val selected = FileChooser.chooseFile(descriptor, project, null)
            if (selected != null) {
                projectRootField.text = selected.path
            }
        }
    }

    override fun resetEditorFrom(configuration: AutoJs6ProjectRunConfiguration) {
        projectRootField.text = configuration.projectRootPath
    }

    override fun applyEditorTo(configuration: AutoJs6ProjectRunConfiguration) {
        configuration.projectRootPath = projectRootField.text.trim()
    }

    override fun createEditor(): JComponent = panel
}
