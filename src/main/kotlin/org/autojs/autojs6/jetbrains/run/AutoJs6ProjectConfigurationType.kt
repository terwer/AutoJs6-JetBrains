package org.autojs.autojs6.jetbrains.run

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.execution.configurations.ConfigurationTypeUtil
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.icons.AllIcons
import com.intellij.openapi.project.Project
import javax.swing.Icon

class AutoJs6ProjectConfigurationType : ConfigurationType {
    private val factory = AutoJs6ProjectConfigurationFactory(this)

    override fun getDisplayName(): String = "AutoJs6 Project"

    override fun getConfigurationTypeDescription(): String =
        "Run an AutoJs6 project containing project.json on connected AutoJs6 devices"

    override fun getIcon(): Icon = AllIcons.Nodes.Folder

    override fun getId(): String = ID

    override fun getConfigurationFactories(): Array<ConfigurationFactory> = arrayOf(factory)

    companion object {
        const val ID = "AutoJs6.Project"

        fun getInstance(): AutoJs6ProjectConfigurationType =
            ConfigurationTypeUtil.findConfigurationType(AutoJs6ProjectConfigurationType::class.java)
    }
}

class AutoJs6ProjectConfigurationFactory(type: ConfigurationType) : ConfigurationFactory(type) {
    override fun getId(): String = AutoJs6ProjectConfigurationType.ID

    override fun getName(): String = "AutoJs6 Project"

    override fun createTemplateConfiguration(project: Project): RunConfiguration =
        AutoJs6ProjectRunConfiguration(project, this, "AutoJs6 Project")
}
