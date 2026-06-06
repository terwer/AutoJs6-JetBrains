package org.autojs.autojs6.jetbrains.statusbar

import org.autojs.autojs6.jetbrains.device.AutoJs6DeviceSnapshot

object AutoJs6DeviceStatusText {
    fun selectedValue(snapshots: List<AutoJs6DeviceSnapshot>, selectedKey: String?): String {
        if (snapshots.isEmpty()) return "AutoJs6: 无设备"
        val selected = snapshots.firstOrNull { it.key == selectedKey } ?: snapshots.singleOrNull()
        return if (selected != null) {
            "AutoJs6: ${compact(selected.name)}"
        } else {
            "AutoJs6: ${snapshots.size} 设备"
        }
    }

    fun tooltip(snapshots: List<AutoJs6DeviceSnapshot>, selectedKey: String?): String {
        if (snapshots.isEmpty()) return "AutoJs6: 未连接设备。点击 Connect 建立连接后可在这里切换。"
        val selected = snapshots.firstOrNull { it.key == selectedKey }
        val current = selected?.let { "当前设备: ${popupLabel(it)}" } ?: "当前设备: 未指定"
        return "$current\n点击切换 AutoJs6 目标设备。"
    }

    fun popupLabel(snapshot: AutoJs6DeviceSnapshot): String =
        "${snapshot.name} — ${snapshot.connectionType} — ${snapshot.endpoint}"

    fun maxValue(snapshots: List<AutoJs6DeviceSnapshot>): String =
        snapshots.map { "AutoJs6: ${compact(it.name)}" }.maxByOrNull { it.length } ?: "AutoJs6: 无设备"

    private fun compact(value: String, maxLength: Int = 28): String {
        val trimmed = value.ifBlank { "unknown" }
        return if (trimmed.length <= maxLength) trimmed else trimmed.take(maxLength - 1) + "…"
    }
}
