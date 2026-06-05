package org.autojs.autojs6.jetbrains.remote

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import org.autojs.autojs6.jetbrains.AutoJs6Constants
import org.autojs.autojs6.jetbrains.AutoJs6Notifier
import org.autojs.autojs6.jetbrains.AutoJs6SettingsService
import org.autojs.autojs6.jetbrains.actions.AutoJs6CommandDispatcher
import java.net.InetSocketAddress
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.concurrent.Executors

@Service(Service.Level.APP)
class AutoJs6HttpBridgeService : Disposable {
    @Volatile private var server: HttpServer? = null
    private val executor = Executors.newSingleThreadExecutor { r -> Thread(r, "AutoJs6-http-bridge").apply { isDaemon = true } }

    fun applySettings(project: Project? = null) {
        val settings = AutoJs6SettingsService.getInstance().state
        if (!settings.httpBridgeEnabled) {
            stop(project)
            return
        }
        start(project, settings.httpBridgeBindHost, settings.httpBridgePort, settings.httpBridgeCompatibilityMode)
    }

    fun start(
        project: Project? = null,
        bindHost: String = AutoJs6SettingsService.getInstance().state.httpBridgeBindHost,
        port: Int = AutoJs6SettingsService.getInstance().state.httpBridgePort,
        compatibilityMode: Boolean = AutoJs6SettingsService.getInstance().state.httpBridgeCompatibilityMode
    ) {
        if (server != null) return
        if ((bindHost == "0.0.0.0" || bindHost == "::") && !compatibilityMode) {
            AutoJs6Notifier.error(project, "HTTP bridge wider network binding 需要显式 compatibility mode 确认")
            return
        }
        val http = HttpServer.create(InetSocketAddress(bindHost, port), 0)
        http.createContext("/exec") { exchange -> handleExecExchange(exchange, project) }
        http.createContext("/") { exchange -> respond(exchange, 404, "unknown AutoJs6 HTTP endpoint") }
        http.executor = executor
        http.start()
        server = http
        AutoJs6Notifier.info(project, "AutoJs6 HTTP bridge 已启动: http://$bindHost:$port/exec")
    }

    fun stop(project: Project? = null) {
        server?.stop(0)
        server = null
        if (project != null) AutoJs6Notifier.info(project, "AutoJs6 HTTP bridge 已停止")
    }

    fun isRunning(): Boolean = server != null

    fun diagnosticLine(): String {
        val settings = AutoJs6SettingsService.getInstance().state
        return "running=${isRunning()}, enabled=${settings.httpBridgeEnabled}, bind=${settings.httpBridgeBindHost}:${settings.httpBridgePort}, compatibilityMode=${settings.httpBridgeCompatibilityMode}, defaultSafe=${!settings.httpBridgeEnabled || settings.httpBridgeBindHost == "127.0.0.1"}"
    }

    fun handleExecForTest(cmd: String?, path: String?, project: Project? = firstOpenProject()): Pair<Int, String> = dispatch(cmd, path, project)

    private fun handleExecExchange(exchange: HttpExchange, fallbackProject: Project?) {
        if (exchange.requestMethod.uppercase() != "GET") {
            respond(exchange, 405, "AutoJs6 /exec only supports GET")
            return
        }
        val query = parseQuery(exchange.requestURI.rawQuery.orEmpty())
        val (status, body) = dispatch(query["cmd"], query["path"], fallbackProject ?: firstOpenProject())
        respond(exchange, status, body)
    }

    private fun dispatch(cmd: String?, path: String?, project: Project?): Pair<Int, String> {
        val command = cmd?.trim().orEmpty()
        if (command.isBlank()) return 400 to "missing cmd"
        if (!AutoJs6CommandDispatcher.isWhitelisted(command)) {
            return 400 to "unknown AutoJs6 command: $command"
        }
        val result = AutoJs6CommandDispatcher.dispatch(project, command, path)
        return if (result.success) {
            200 to "this command is:$command-->${path.orEmpty()}"
        } else {
            400 to result.message
        }
    }

    private fun parseQuery(raw: String): Map<String, String> {
        if (raw.isBlank()) return emptyMap()
        return raw.split('&').mapNotNull { pair ->
            val idx = pair.indexOf('=')
            if (idx < 0) return@mapNotNull null
            val key = decode(pair.substring(0, idx))
            val value = decode(pair.substring(idx + 1))
            key to value
        }.toMap()
    }

    private fun decode(value: String): String = URLDecoder.decode(value, StandardCharsets.UTF_8)

    private fun respond(exchange: HttpExchange, status: Int, body: String) {
        val bytes = body.toByteArray(StandardCharsets.UTF_8)
        exchange.responseHeaders.add("Content-Type", "text/plain; charset=utf-8")
        exchange.sendResponseHeaders(status, bytes.size.toLong())
        exchange.responseBody.use { it.write(bytes) }
    }

    private fun firstOpenProject(): Project? = ProjectManager.getInstance().openProjects.firstOrNull()

    override fun dispose() {
        stop()
        executor.shutdownNow()
    }
}
