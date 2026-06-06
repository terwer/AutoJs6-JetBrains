package org.autojs.autojs6.jetbrains.project

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import org.autojs.autojs6.jetbrains.AutoJs6Notifier
import org.autojs.autojs6.jetbrains.device.AutoJs6Device
import org.autojs.autojs6.jetbrains.device.JsonCodec
import java.io.ByteArrayOutputStream
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import kotlin.io.path.name

const val AUTOJS6_RUN_PROJECT_COMMAND = "run_project"
const val AUTOJS6_SAVE_PROJECT_COMMAND = "save_project"

data class AutoJs6ProjectConfig(
    val root: Path,
    val ignoreEntries: List<String>,
    val resolvedIgnorePaths: List<Path>
)

data class AutoJs6ProjectDiffPayload(
    val root: Path,
    val modifiedFiles: List<String>,
    val deletedFiles: List<String>,
    val zipBytes: ByteArray,
    val md5: String,
    val override: Boolean
) {
    fun commandData(command: String): Map<String, Any?> = linkedMapOf(
        "id" to root.toString(),
        "name" to root.toString(),
        "deletedFiles" to deletedFiles,
        "override" to override,
        "command" to command
    )
}

data class AutoJs6ProjectSyncResult(
    val sent: Int,
    val failures: List<String>,
    val sentDevices: List<AutoJs6Device> = emptyList()
)

private data class FileStamp(val relativePath: String, val lastModifiedMillis: Long)
private data class ProjectSyncState(var syncedOnce: Boolean = false, var files: MutableMap<String, FileStamp> = linkedMapOf())

@Service(Service.Level.APP)
class AutoJs6ProjectSyncService {
    private val states = ConcurrentHashMap<String, ProjectSyncState>()

    fun runProjectSyncInBackground(project: Project?, root: Path, command: String, devices: List<AutoJs6Device>) {
        if (devices.isEmpty()) {
            AutoJs6Notifier.error(project, "未发现已连接的设备")
            return
        }
        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "AutoJs6 Project Sync", true) {
            override fun run(indicator: ProgressIndicator) {
                indicator.text = "Preparing AutoJs6 project diff"
                val result = sendProjectCommand(root, command, devices) { index, device ->
                    indicator.checkCanceled()
                    indicator.fraction = index.toDouble() / devices.size.coerceAtLeast(1)
                    indicator.text2 = "${device.deviceName} (${device.endpoint()})"
                }
                indicator.fraction = 1.0
                result.failures.forEach { AutoJs6Notifier.error(project, it) }
                if (result.sent > 0) {
                    AutoJs6Notifier.info(project, "AutoJs6 project command=$command 已发送到 ${result.sent} 个设备")
                }
            }
        })
    }

    fun sendProjectCommand(
        root: Path,
        command: String,
        devices: List<AutoJs6Device>,
        beforeDevice: ((Int, AutoJs6Device) -> Unit)? = null
    ): AutoJs6ProjectSyncResult {
        var sent = 0
        val failures = mutableListOf<String>()
        val sentDevices = mutableListOf<AutoJs6Device>()
        devices.forEachIndexed { index, device ->
            beforeDevice?.invoke(index, device)
            try {
                val payload = buildPayload(root, device.key())
                device.sendBytes(payload.zipBytes)
                device.sendBytesCommand(payload.md5, payload.commandData(command))
                sent++
                sentDevices += device
            } catch (t: Throwable) {
                failures += "Project sync 到 ${device.endpoint()} 失败: ${t.message}"
            }
        }
        return AutoJs6ProjectSyncResult(sent, failures, sentDevices)
    }

    fun buildPayload(root: Path, deviceKey: String): AutoJs6ProjectDiffPayload = buildPayloadWithState(root, stateKey(root, deviceKey))

    fun resetState(root: Path? = null) {
        if (root == null) {
            states.clear()
        } else {
            val prefix = root.toAbsolutePath().normalize().toString() + "|"
            states.keys.removeIf { it.startsWith(prefix) }
        }
    }

    private fun buildPayloadWithState(root: Path, key: String): AutoJs6ProjectDiffPayload {
        val normalizedRoot = validateProjectRoot(root)
        val config = loadConfig(normalizedRoot)
        val state = states.computeIfAbsent(key) { ProjectSyncState() }
        val previous = LinkedHashMap(state.files)
        val current = linkedMapOf<String, FileStamp>()
        val modified = mutableListOf<String>()

        walkProjectFiles(config).forEach { file ->
            val rel = normalizedRelativePath(normalizedRoot, file)
            val stamp = Files.getLastModifiedTime(file, LinkOption.NOFOLLOW_LINKS).toMillis()
            val old = previous.remove(file.toAbsolutePath().normalize().toString())
            current[file.toAbsolutePath().normalize().toString()] = FileStamp(rel, stamp)
            if (old == null || old.lastModifiedMillis != stamp) modified += rel
        }

        val deleted = previous.values.map { it.relativePath }.sorted()
        val sortedModified = modified.sorted()
        val override = state.syncedOnce
        val zipEntries = if (override) current.values.map { it.relativePath }.sorted() else sortedModified
        val zip = zipModifiedFiles(normalizedRoot, zipEntries)
        val payload = AutoJs6ProjectDiffPayload(
            root = normalizedRoot,
            modifiedFiles = sortedModified,
            deletedFiles = deleted,
            zipBytes = zip,
            md5 = md5Hex(zip),
            override = override
        )
        state.files = current
        state.syncedOnce = true
        return payload
    }

    private fun stateKey(root: Path, deviceKey: String): String = root.toAbsolutePath().normalize().toString() + "|" + deviceKey

    companion object {
        fun getInstance(): AutoJs6ProjectSyncService = service()

        fun resolveProjectRoot(input: Path, searchParents: Boolean = true): Path? {
            val normalized = input.toAbsolutePath().normalize()
            val start = when {
                Files.isRegularFile(normalized) && normalized.name == "project.json" -> normalized.parent
                Files.isRegularFile(normalized) -> normalized.parent
                else -> normalized
            } ?: return null
            if (!searchParents) return if (Files.exists(start.resolve("project.json"))) start else null
            var cursor: Path? = start
            while (cursor != null) {
                if (Files.exists(cursor.resolve("project.json"))) return cursor
                cursor = cursor.parent
            }
            return null
        }

        fun validateProjectRoot(root: Path): Path {
            val normalized = root.toAbsolutePath().normalize()
            require(Files.isDirectory(normalized)) { "AutoJs6 project root 必须是目录: $normalized" }
            require(Files.exists(normalized.resolve("project.json"))) { "缺少必要的项目配置文件: ${normalized.resolve("project.json")}" }
            return normalized
        }

        fun loadConfig(root: Path): AutoJs6ProjectConfig {
            val normalized = validateProjectRoot(root)
            val jsonPath = normalized.resolve("project.json")
            val parsed = JsonCodec.parseObject(Files.readString(jsonPath))
            val ignore = (parsed["ignore"] as? List<*>)
                ?.mapNotNull { it?.toString() }
                ?.map { normalizeIgnoreEntry(it) }
                ?.filter { it.isNotBlank() }
                ?: emptyList()
            val resolved = ignore.map { normalized.resolve(it).normalize() }
            return AutoJs6ProjectConfig(normalized, ignore, resolved)
        }

        fun normalizeIgnoreEntry(entry: String): String = entry
            .trim()
            .replace('\\', '/')
            .trimStart('/')
            .let { Path.of(it).normalize().toString().replace('\\', '/') }
            .removePrefix("./")

        fun walkProjectFiles(config: AutoJs6ProjectConfig): List<Path> {
            val rootReal = config.root.toRealPath(LinkOption.NOFOLLOW_LINKS)
            val out = mutableListOf<Path>()
            Files.walk(config.root).use { stream ->
                stream
                    .filter { Files.isRegularFile(it, LinkOption.NOFOLLOW_LINKS) }
                    .forEach { file ->
                        val normalized = file.toAbsolutePath().normalize()
                        val real = normalized.toRealPath(LinkOption.NOFOLLOW_LINKS)
                        if (!real.startsWith(rootReal)) return@forEach
                        if (!isIgnored(config, normalized)) out.add(normalized)
                    }
            }
            return out.sortedBy { normalizedRelativePath(config.root, it) }
        }

        fun isIgnored(config: AutoJs6ProjectConfig, file: Path): Boolean {
            val root = config.root.toAbsolutePath().normalize()
            val rel = normalizedRelativePath(root, file)
            config.ignoreEntries.forEachIndexed { index, entry ->
                if (rel == entry || rel.startsWith("$entry/")) return true
                val ignorePath = config.resolvedIgnorePaths[index]
                if (ignorePath.exists()) {
                    val realFile = runCatching { file.toRealPath(LinkOption.NOFOLLOW_LINKS) }.getOrNull() ?: return@forEachIndexed
                    val realIgnore = runCatching { ignorePath.toRealPath(LinkOption.NOFOLLOW_LINKS) }.getOrNull() ?: return@forEachIndexed
                    if (realFile.startsWith(realIgnore)) return true
                }
            }
            return false
        }

        fun normalizedRelativePath(root: Path, file: Path): String = root.toAbsolutePath().normalize()
            .relativize(file.toAbsolutePath().normalize())
            .toString()
            .replace('\\', '/')

        fun zipModifiedFiles(root: Path, relativeFiles: List<String>): ByteArray {
            val out = ByteArrayOutputStream()
            ZipOutputStream(out).use { zip ->
                relativeFiles.sorted().forEach { rel ->
                    val entry = ZipEntry(rel)
                    zip.putNextEntry(entry)
                    Files.newInputStream(root.resolve(rel)).use { input -> input.copyTo(zip) }
                    zip.closeEntry()
                }
            }
            return out.toByteArray()
        }

        fun md5Hex(bytes: ByteArray): String = MessageDigest.getInstance("MD5")
            .digest(bytes)
            .joinToString("") { "%02x".format(it) }
    }
}
