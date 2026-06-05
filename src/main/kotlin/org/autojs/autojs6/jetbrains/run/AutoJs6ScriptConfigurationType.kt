package org.autojs.autojs6.jetbrains.run

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.execution.configurations.ConfigurationTypeUtil
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.icons.AllIcons
import com.intellij.openapi.project.Project
import javax.swing.Icon

class AutoJs6ScriptConfigurationType : ConfigurationType {
    private val factory = AutoJs6ScriptConfigurationFactory(this)

    override fun getDisplayName(): String = "AutoJs6 Script"

    override fun getConfigurationTypeDescription(): String =
        "Run a single local AutoJs6 JavaScript file on connected AutoJs6 devices"

    override fun getIcon(): Icon = AllIcons.RunConfigurations.Application

    override fun getId(): String = ID

    override fun getConfigurationFactories(): Array<ConfigurationFactory> = arrayOf(factory)

    companion object {
        const val ID = "AutoJs6.Script"

        fun getInstance(): AutoJs6ScriptConfigurationType =
            ConfigurationTypeUtil.findConfigurationType(AutoJs6ScriptConfigurationType::class.java)
    }
}

class AutoJs6ScriptConfigurationFactory(type: ConfigurationType) : ConfigurationFactory(type) {
    override fun getId(): String = AutoJs6ScriptConfigurationType.ID

    override fun getName(): String = "AutoJs6 Script"

    override fun createTemplateConfiguration(project: Project): RunConfiguration =
        AutoJs6ScriptRunConfiguration(project, this, "AutoJs6 Script")
}
