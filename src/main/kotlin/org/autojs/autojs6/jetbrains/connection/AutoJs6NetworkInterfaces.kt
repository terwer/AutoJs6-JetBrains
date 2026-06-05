package org.autojs.autojs6.jetbrains.connection

import org.autojs.autojs6.jetbrains.AutoJs6Constants
import java.net.Inet4Address
import java.net.NetworkInterface

data class AutoJs6NetworkInterfaceHint(
    val name: String,
    val displayName: String,
    val address: String,
    val mac: String
) {
    fun instruction(port: Int = AutoJs6Constants.IDE_LISTENING_PORT): String =
        "在 AutoJs6 Client mode 中连接 $address:$port"
}

data class AutoJs6HostPort(val host: String, val port: Int)

object AutoJs6NetworkInterfaces {
    fun listSuitableIpv4Interfaces(): List<AutoJs6NetworkInterfaceHint> {
        val ignoredName = Regex("(loopback|vmware|vmnet|virtualbox|vbox|hyper-v|wsl|docker|npcap)", RegexOption.IGNORE_CASE)
        return NetworkInterface.getNetworkInterfaces().asSequence()
            .filter { iface -> runCatching { iface.isUp && !iface.isLoopback && !iface.isVirtual }.getOrDefault(false) }
            .filter { iface -> !ignoredName.containsMatchIn(iface.name) && !ignoredName.containsMatchIn(iface.displayName ?: "") }
            .flatMap { iface ->
                iface.inetAddresses.asSequence()
                    .filterIsInstance<Inet4Address>()
                    .filter { address -> !address.isLoopbackAddress && !address.isAnyLocalAddress && !address.isLinkLocalAddress }
                    .filter { address -> !address.hostAddress.startsWith("169.254.") }
                    .map { address ->
                        AutoJs6NetworkInterfaceHint(
                            name = iface.name,
                            displayName = iface.displayName ?: iface.name,
                            address = address.hostAddress,
                            mac = runCatching { iface.hardwareAddress?.joinToString(":") { "%02x".format(it) }.orEmpty() }.getOrDefault("")
                        )
                    }
            }
            .distinctBy { it.address }
            .sortedWith(compareBy<AutoJs6NetworkInterfaceHint> { it.name.contains("virtual", ignoreCase = true) }.thenBy { it.name })
            .toList()
    }

    fun parseHostPort(input: String, defaultPort: Int = AutoJs6Constants.SERVER_PORT): AutoJs6HostPort? {
        val trimmed = input.trim().removePrefix("http://").removePrefix("https://").trimEnd('/')
        if (trimmed.isBlank()) return null
        val host: String
        val port: Int
        if (trimmed.startsWith("[")) {
            val end = trimmed.indexOf(']')
            if (end <= 0) return null
            host = trimmed.substring(1, end)
            port = trimmed.substring(end + 1).removePrefix(":").toIntOrNull() ?: defaultPort
        } else {
            val idx = trimmed.lastIndexOf(':')
            if (idx > 0 && trimmed.indexOf(':') == idx) {
                host = trimmed.substring(0, idx)
                port = trimmed.substring(idx + 1).toIntOrNull() ?: return null
            } else {
                host = trimmed
                port = defaultPort
            }
        }
        if (host.isBlank() || port !in 1..65535) return null
        return AutoJs6HostPort(host, port)
    }
}
