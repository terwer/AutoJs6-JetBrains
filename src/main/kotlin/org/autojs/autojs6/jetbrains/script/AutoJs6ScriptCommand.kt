package org.autojs.autojs6.jetbrains.script

import com.intellij.openapi.vfs.VirtualFile
import java.nio.file.Files
import java.nio.file.InvalidPathException
import java.nio.file.Path

data class AutoJs6SingleFilePayload(
    val id: String,
    val name: String,
    val script: String
) {
    fun toCommandData(): Map<String, Any?> = linkedMapOf(
        "id" to id,
        "name" to name,
        "script" to script
    )
}

data class AutoJs6ScriptValidation(
    val valid: Boolean,
    val message: String = ""
)

object AutoJs6ScriptCommand {
    const val RUN_COMMAND = "run"
    const val SCRIPT_PATH_ATTRIBUTE = "scriptPath"

    fun createSingleFilePayload(path: String, name: String, script: String): AutoJs6SingleFilePayload =
        AutoJs6SingleFilePayload(path, name, script)

    fun createSingleFilePayload(file: VirtualFile, script: String): AutoJs6SingleFilePayload =
        createSingleFilePayload(file.path, file.name, script)

    fun validateVirtualJsFile(file: VirtualFile): AutoJs6ScriptValidation {
        if (!file.isInLocalFileSystem) {
            return AutoJs6ScriptValidation(false, "AutoJs6 Script 只支持本地 .js 文件")
        }
        if (file.isDirectory) {
            return AutoJs6ScriptValidation(false, "AutoJs6 Script 只支持单个 .js 文件，不能选择目录: ${file.path}")
        }
        if (!isJavaScriptFileName(file.name)) {
            return AutoJs6ScriptValidation(false, "AutoJs6 Script 只支持 .js 文件: ${file.path}")
        }
        return AutoJs6ScriptValidation(true)
    }

    fun validateLocalJsPath(scriptPath: String): AutoJs6ScriptValidation {
        val trimmed = scriptPath.trim()
        if (trimmed.isEmpty()) {
            return AutoJs6ScriptValidation(false, "请选择本地 .js 脚本文件")
        }
        val path = try {
            Path.of(trimmed)
        } catch (e: InvalidPathException) {
            return AutoJs6ScriptValidation(false, "脚本路径无效: $trimmed")
        }
        if (!Files.exists(path)) {
            return AutoJs6ScriptValidation(false, "脚本文件不存在: $trimmed")
        }
        if (Files.isDirectory(path)) {
            return AutoJs6ScriptValidation(false, "AutoJs6 Script 只支持单个 .js 文件，不能选择目录: $trimmed")
        }
        if (!Files.isRegularFile(path)) {
            return AutoJs6ScriptValidation(false, "AutoJs6 Script 只支持本地普通 .js 文件: $trimmed")
        }
        if (!isJavaScriptFileName(path.fileName.toString())) {
            return AutoJs6ScriptValidation(false, "AutoJs6 Script 只支持 .js 文件: $trimmed")
        }
        if (!Files.isReadable(path)) {
            return AutoJs6ScriptValidation(false, "脚本文件不可读取: $trimmed")
        }
        return AutoJs6ScriptValidation(true)
    }

    fun readLocalJsPayload(scriptPath: String): AutoJs6SingleFilePayload {
        val validation = validateLocalJsPath(scriptPath)
        require(validation.valid) { validation.message }
        val path = Path.of(scriptPath.trim())
        return createSingleFilePayload(
            path = path.toAbsolutePath().toString(),
            name = path.fileName.toString(),
            script = Files.readString(path)
        )
    }

    fun isJavaScriptFileName(name: String): Boolean = name.endsWith(".js", ignoreCase = true)
}
