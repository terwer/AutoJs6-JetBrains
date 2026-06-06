package org.autojs.autojs6.jetbrains.run

import com.intellij.execution.actions.ConfigurationContext
import com.intellij.execution.actions.LazyRunConfigurationProducer
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.util.Ref
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import org.autojs.autojs6.jetbrains.project.AutoJs6ProjectSyncService
import java.nio.file.Path

class AutoJs6ProjectConfigurationProducer : LazyRunConfigurationProducer<AutoJs6ProjectRunConfiguration>() {
    override fun getConfigurationFactory(): ConfigurationFactory =
        AutoJs6ProjectConfigurationType.getInstance().configurationFactories.single()

    override fun setupConfigurationFromContext(
        configuration: AutoJs6ProjectRunConfiguration,
        context: ConfigurationContext,
        sourceElement: Ref<PsiElement>
    ): Boolean {
        val root = findProjectRoot(context) ?: return false
        configuration.projectRootPath = root.toString()
        configuration.name = "AutoJs6 Project: ${root.fileName}"

        val rootVirtualFile = findContextFile(context)?.let { if (it.isDirectory) it else it.parent }
        val psiFile = rootVirtualFile?.let { PsiManager.getInstance(context.project).findFile(it) }
        if (psiFile != null) sourceElement.set(psiFile)
        return true
    }

    override fun isConfigurationFromContext(
        configuration: AutoJs6ProjectRunConfiguration,
        context: ConfigurationContext
    ): Boolean {
        val root = findProjectRoot(context) ?: return false
        return root.toString() == AutoJs6ProjectRunConfiguration.resolveProjectRoot(configuration.projectRootPath)?.toString()
    }

    private fun findProjectRoot(context: ConfigurationContext): Path? {
        val file = findContextFile(context) ?: return null
        if (!file.isInLocalFileSystem) return null
        return AutoJs6ProjectSyncService.resolveProjectRoot(Path.of(file.path), true)
    }

    private fun findContextFile(context: ConfigurationContext): VirtualFile? {
        val psiFile = context.psiLocation?.containingFile
        if (psiFile?.virtualFile != null) return psiFile.virtualFile
        return CommonDataKeys.VIRTUAL_FILE.getData(context.dataContext)
    }
}
