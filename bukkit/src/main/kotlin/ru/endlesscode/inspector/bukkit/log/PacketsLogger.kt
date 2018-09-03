package ru.endlesscode.inspector.bukkit.log

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.error.DetailedErrorReporter
import com.comphenix.protocol.events.ConnectionSide
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.injector.GamePhase
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.plugin.Plugin
import ru.endlesscode.inspector.bukkit.util.PrintUtils
import ru.endlesscode.inspector.bukkit.util.firstUpperCase

internal class PacketsLogger(
        private val sender: ConsoleCommandSender,
        private val rules: Map<String, LogRule>
) {

    companion object {
        private const val TAG = "[PacketsLogger]"
    }

    fun inject(plugin: Plugin) {
        val params = PacketAdapter.params()
                .gamePhase(GamePhase.BOTH)
                .connectionSide(ConnectionSide.BOTH)
                .listenerPriority(ListenerPriority.MONITOR)
                .types(PacketType.values().toSet())
                .plugin(plugin)

        ProtocolLibrary.getProtocolManager().addPacketListener(object : PacketAdapter(params) {
            override fun onPacketReceiving(event: PacketEvent) {
                onPacketEvent(event)
            }

            override fun onPacketSending(event: PacketEvent) {
                onPacketEvent(event)
            }
        })
    }

    private fun onPacketEvent(event: PacketEvent) {
        val logRule = findLogRule(event.packetType) ?: return

        logRule.log {
            logPacket(event)
        }
    }

    private fun findLogRule(packetType: PacketType): LogRule? {
        val protocol = packetType.protocol.name.firstUpperCase()
        val sender = packetType.sender.name.firstUpperCase()

        // Possible rule keys in order from more specific to wider.
        // TODO: Maybe cache this
        val keys = listOf(
                "$protocol.$sender.${packetType.name()}",
                "$protocol.$sender",
                protocol
        )

        for (key in keys) {
            if (key in rules) return rules[key]
        }

        return null
    }

    private fun logPacket(event: PacketEvent) {
        val message = mutableListOf(
                "$TAG ${PrintUtils.toString(event.packetType)}",
                "  Player: ${PrintUtils.toString(event.player)}",
                "  Fields:"
        )

        val packetDescription = DetailedErrorReporter.getStringDescription(event.packet.handle)
                .split('\n')
                .toMutableList()
        // Remove class name and square brackets
        packetDescription.removeAt(packetDescription.lastIndex)
        packetDescription.removeAt(0)

        if (packetDescription.isEmpty()) {
            message += "    <no fields>"
        } else {
            for (s in packetDescription) {
                message += "  $s"
            }
        }

        sender.sendMessage(message.toTypedArray())
    }
}
