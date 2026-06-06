package org.autojs.autojs6.jetbrains.run

import com.intellij.execution.Executor
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.RunConfigurationBase
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.configurations.RuntimeConfigurationException
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.project.Project
import org.autojs.autojs6.jetbrains.project.AutoJs6ProjectSyncService
import org.jdom.Element
import java.nio.file.Files
import java.nio.file.Path

class AutoJs6ProjectRunConfiguration(
    project: Project,
    factory: ConfigurationFactory,
    name: String
) : RunConfigurationBase<RunProfileState>(project, factory, name) {
    var projectRootPath: String = ""

    override fun getConfigurationEditor() = AutoJs6ProjectSettingsEditor(project)

    @Throws(RuntimeConfigurationException::class)
    override fun checkConfiguration() {
        val root = resolveProjectRoot(projectRootPath)
        if (root == null) {
            throw RuntimeConfigurationException("请选择包含 project.json 的 AutoJs6 项目目录")
        }
        runCatching { AutoJs6ProjectSyncService.validateProjectRoot(root) }
            .getOrElse { throw RuntimeConfigurationException(it.message ?: "AutoJs6 项目目录无效") }
    }

    override fun getState(executor: Executor, environment: ExecutionEnvironment): RunProfileState =
        AutoJs6ProjectRunProfileState(project, projectRootPath)

    override fun readExternal(element: Element) {
        super.readExternal(element)
        projectRootPath = AutoJs6ProjectConfigurationSerializer.readProjectRoot(element)
    }

    override fun writeExternal(element: Element) {
        super.writeExternal(element)
        AutoJs6ProjectConfigurationSerializer.writeProjectRoot(element, projectRootPath)
    }

    companion object {
        fun resolveProjectRoot(path: String): Path? {
            if (path.isBlank()) return null
            return runCatching {
                val input = Path.of(path)
                val normalized = input.toAbsolutePath().normalize()
                when {
                    Files.isDirectory(normalized) -> AutoJs6ProjectSyncService.resolveProjectRoot(normalized, false)
                    Files.isRegularFile(normalized) -> AutoJs6ProjectSyncService.resolveProjectRoot(normalized, true)
                    else -> null
                }
            }.getOrNull()
        }
    }
}
