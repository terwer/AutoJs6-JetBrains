package org.autojs.autojs6.jetbrains.run

import com.intellij.execution.actions.ConfigurationContext
import com.intellij.execution.actions.LazyRunConfigurationProducer
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.util.Ref
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import org.autojs.autojs6.jetbrains.script.AutoJs6ScriptCommand

class AutoJs6ScriptConfigurationProducer : LazyRunConfigurationProducer<AutoJs6ScriptRunConfiguration>() {
    override fun getConfigurationFactory(): ConfigurationFactory =
        AutoJs6ScriptConfigurationType.getInstance().configurationFactories.single()

    override fun setupConfigurationFromContext(
        configuration: AutoJs6ScriptRunConfiguration,
        context: ConfigurationContext,
        sourceElement: Ref<PsiElement>
    ): Boolean {
        val file = findContextFile(context) ?: return false
        val validation = AutoJs6ScriptCommand.validateVirtualJsFile(file)
        if (!validation.valid) return false

        configuration.scriptPath = file.path
        configuration.name = "AutoJs6 Script: ${file.name}"

        val psiFile = PsiManager.getInstance(context.project).findFile(file) ?: return true
        sourceElement.set(psiFile)
        return true
    }

    override fun isConfigurationFromContext(
        configuration: AutoJs6ScriptRunConfiguration,
        context: ConfigurationContext
    ): Boolean {
        val file = findContextFile(context) ?: return false
        return AutoJs6ScriptCommand.validateVirtualJsFile(file).valid && file.path == configuration.scriptPath
    }

    private fun findContextFile(context: ConfigurationContext): VirtualFile? {
        val psiFile = context.psiLocation?.containingFile
        if (psiFile?.virtualFile != null) return psiFile.virtualFile
        return CommonDataKeys.VIRTUAL_FILE.getData(context.dataContext)
    }
}
