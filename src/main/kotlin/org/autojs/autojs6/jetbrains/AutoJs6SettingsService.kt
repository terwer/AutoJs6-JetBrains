package org.autojs.autojs6.jetbrains

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service

@Service(Service.Level.APP)
@State(name = "AutoJs6Settings", storages = [Storage("autojs6.xml")])
class AutoJs6SettingsService : PersistentStateComponent<AutoJs6SettingsService.State> {
    data class RecentHostRecord(
        var host: String = "",
        var lastConnectedEpochMillis: Long = 0
    )

    data class State(
        var adbPath: String = "",
        var listeningPort: Int = AutoJs6Constants.IDE_LISTENING_PORT,
        var serverPort: Int = AutoJs6Constants.SERVER_PORT,
        var adbServerPort: Int = AutoJs6Constants.ADB_SERVER_PORT,
        /** Legacy list retained for compatibility with existing persisted settings. */
        var recentHosts: MutableList<String> = mutableListOf(),
        var recentHostRecords: MutableList<RecentHostRecord> = mutableListOf(),
        var qrHintEnabled: Boolean = true,
        var httpBridgeEnabled: Boolean = false,
        var httpBridgeBindHost: String = "127.0.0.1",
        var httpBridgePort: Int = AutoJs6Constants.HTTP_SERVER_PORT,
        var httpBridgeCompatibilityMode: Boolean = false
    )

    private var state = State()
    override fun getState(): State = state
    override fun loadState(state: State) {
        this.state = state
        if (state.recentHostRecords.isEmpty() && state.recentHosts.isNotEmpty()) {
            val now = System.currentTimeMillis()
            state.recentHostRecords = state.recentHosts
                .filter { it.isNotBlank() }
                .map { RecentHostRecord(it, now) }
                .toMutableList()
        }
    }

    fun addRecentHost(host: String, timestamp: Long = System.currentTimeMillis()) {
        val normalized = normalizeHostRecord(host) ?: return
        state.recentHostRecords.removeAll { it.host.equals(normalized, ignoreCase = true) }
        state.recentHostRecords.add(0, RecentHostRecord(normalized, timestamp))
        while (state.recentHostRecords.size > 20) state.recentHostRecords.removeLast()
        state.recentHosts = state.recentHostRecords.map { it.host }.toMutableList()
    }

    fun clearRecentHosts(): Int {
        val count = state.recentHostRecords.size.coerceAtLeast(state.recentHosts.size)
        state.recentHostRecords.clear()
        state.recentHosts.clear()
        return count
    }

    fun recentHostDisplayStrings(): List<String> = state.recentHostRecords.map { record ->
        "${record.host}  (${java.time.Instant.ofEpochMilli(record.lastConnectedEpochMillis)})"
    }

    private fun normalizeHostRecord(host: String): String? {
        val normalized = host.trim()
        if (normalized.isEmpty()) return null
        if (normalized == "127.0.0.1" || normalized.equals("localhost", ignoreCase = true)) return null
        return normalized
    }

    companion object { fun getInstance(): AutoJs6SettingsService = service() }
}
