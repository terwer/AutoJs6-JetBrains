package org.autojs.autojs6.jetbrains.run

import com.intellij.execution.Executor
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.RunConfigurationBase
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.configurations.RuntimeConfigurationException
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.project.Project
import org.autojs.autojs6.jetbrains.script.AutoJs6ScriptCommand
import org.jdom.Element

class AutoJs6ScriptRunConfiguration(
    project: Project,
    factory: ConfigurationFactory,
    name: String
) : RunConfigurationBase<RunProfileState>(project, factory, name) {
    var scriptPath: String = ""

    override fun getConfigurationEditor() = AutoJs6ScriptSettingsEditor(project)

    @Throws(RuntimeConfigurationException::class)
    override fun checkConfiguration() {
        val validation = AutoJs6ScriptCommand.validateLocalJsPath(scriptPath)
        if (!validation.valid) {
            throw RuntimeConfigurationException(validation.message)
        }
    }

    override fun getState(executor: Executor, environment: ExecutionEnvironment): RunProfileState =
        AutoJs6ScriptRunProfileState(project, scriptPath)

    override fun readExternal(element: Element) {
        super.readExternal(element)
        scriptPath = AutoJs6ScriptConfigurationSerializer.readScriptPath(element)
    }

    override fun writeExternal(element: Element) {
        super.writeExternal(element)
        AutoJs6ScriptConfigurationSerializer.writeScriptPath(element, scriptPath)
    }
}
