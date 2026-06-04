package org.autojs.autojs6.jetbrains

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service

@Service(Service.Level.APP)
@State(name = "AutoJs6Settings", storages = [Storage("autojs6.xml")])
class AutoJs6SettingsService : PersistentStateComponent<AutoJs6SettingsService.State> {
    data class State(
        var adbPath: String = "",
        var listeningPort: Int = AutoJs6Constants.IDE_LISTENING_PORT,
        var serverPort: Int = AutoJs6Constants.SERVER_PORT,
        var adbServerPort: Int = AutoJs6Constants.ADB_SERVER_PORT,
        var recentHosts: MutableList<String> = mutableListOf()
    )

    private var state = State()
    override fun getState(): State = state
    override fun loadState(state: State) { this.state = state }

    fun addRecentHost(host: String) {
        val normalized = host.trim()
        if (normalized.isEmpty() || normalized == "127.0.0.1") return
        state.recentHosts.remove(normalized)
        state.recentHosts.add(0, normalized)
        while (state.recentHosts.size > 20) state.recentHosts.removeLast()
    }

    companion object { fun getInstance(): AutoJs6SettingsService = service() }
}
