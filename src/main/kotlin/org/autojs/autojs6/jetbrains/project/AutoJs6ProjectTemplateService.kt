package org.autojs.autojs6.jetbrains.project

import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import org.autojs.autojs6.jetbrains.AutoJs6Notifier
import java.net.URI
import java.nio.file.*
import java.text.Normalizer
import kotlin.io.path.*

class AutoJs6ProjectTemplateService(private val project: Project?) {
    fun chooseAndCreate() {
        val descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor()
            .withTitle("选择 AutoJs6 项目目录")
            .withDescription("所选目录将作为 AutoJs6 项目根目录；插件会直接把内置模板生成到该目录。")
        val target = FileChooser.chooseFile(descriptor, project, null)?.toNioPath() ?: return
        val created = createProject(target)
        AutoJs6Notifier.info(project, "AutoJs6 项目已创建: $created")
        Messages.showInfoMessage(project, "AutoJs6 项目已创建：\n$created", "New AutoJs6 Project")
    }

    fun createProject(target: Path, overwrite: Boolean = false): Path {
        val projectName = target.fileName.toString()
        val packageSuffix = normalizePackageSuffix(projectName)
        Files.createDirectories(target)
        val root = templateRoot()
        root.use { template ->
            Files.walk(template.root).use { stream ->
                stream.forEach { source ->
                    val rel = template.root.relativize(source)
                    if (rel.toString().isEmpty()) return@forEach
                    val dest = target.resolve(rel.toString())
                    if (Files.isDirectory(source)) {
                        Files.createDirectories(dest)
                    } else {
                        Files.createDirectories(dest.parent)
                        if (Files.exists(dest) && !overwrite) return@forEach
                        var bytes = Files.readAllBytes(source)
                        if (isTextLike(dest)) {
                            val text = bytes.toString(Charsets.UTF_8)
                                .replace("%PROJECT_NAME_PLACEHOLDER%", projectName)
                                .replace("%PACKAGE_SUFFIX_PLACEHOLDER%", packageSuffix)
                            bytes = text.toByteArray(Charsets.UTF_8)
                        }
                        Files.write(dest, bytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
                    }
                }
            }
        }
        return target
    }

    private fun isTextLike(path: Path): Boolean = path.extension.lowercase() in setOf("json", "js", "txt", "md", "xml", "properties", "gradle", "kt", "java") || path.fileName.toString().startsWith(".")

    private fun templateRoot(): TemplateRoot {
        val url = javaClass.getResource("/assets/template") ?: error("Missing /assets/template resource")
        return if (url.protocol == "file") TemplateRoot(Paths.get(url.toURI()), null) else {
            val uri = URI.create(url.toString().substringBefore("!"))
            val fs = runCatching { FileSystems.getFileSystem(uri) }.getOrElse { FileSystems.newFileSystem(uri, emptyMap<String, Any>()) }
            TemplateRoot(fs.getPath("/assets/template"), fs)
        }
    }

    data class TemplateRoot(val root: Path, val fs: FileSystem?) : AutoCloseable { override fun close() { fs?.close() } }

    companion object {
        fun normalizePackageSuffix(name: String): String {
            var s = Normalizer.normalize(name, Normalizer.Form.NFKD).replace(Regex("\\p{M}+"), "")
            s = s.map { ch -> if (ch in 'A'..'Z' || ch in 'a'..'z' || ch in '0'..'9' || ch == '_') ch.lowercaseChar() else '_' }.joinToString("")
                .replace(Regex("_+"), "_")
                .trim('_')
            if (s.isEmpty()) s = "app"
            if (s.first().isDigit()) s = "app_$s"
            return s
        }
    }
}

