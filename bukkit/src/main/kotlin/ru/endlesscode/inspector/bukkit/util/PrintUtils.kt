package ru.endlesscode.inspector.bukkit.util

import com.comphenix.protocol.PacketType
import org.bukkit.entity.Player

internal object PrintUtils {

    fun toString(obj: Any): String {
        return when(obj) {
            is Player -> playerToString(obj)
            is PacketType -> packetTypeToString(obj)
            else -> obj.toString()
        }
    }

    private fun playerToString(player: Player): String {
        return "${player.javaClass.simpleName}[name=${player.name}]"
    }

    private fun packetTypeToString(packetType: PacketType): String {
        val protocol = packetType.protocol.name.firstUpperCase()
        val sender = packetType.sender.name.firstUpperCase()
        return packetType.toString().replace(packetType.name(), "$protocol.$sender.${packetType.name()}")
    }

    private fun String.firstUpperCase(): String {
        return this.toLowerCase().capitalize()
    }
}
