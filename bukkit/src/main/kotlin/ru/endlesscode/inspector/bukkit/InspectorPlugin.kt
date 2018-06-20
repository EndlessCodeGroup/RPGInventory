package ru.endlesscode.inspector.bukkit

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.server.PluginEnableEvent
import ru.endlesscode.inspector.bukkit.plugin.InnerPlugin

class InspectorPlugin : InnerPlugin(), Listener {

    override fun onEnable() {
        logger.info("onEnable")

        server.pluginManager.registerEvents(this, this)
        error("!")
    }

    override fun onDisable() {
        logger.info("onEnable")
    }

    @EventHandler
    fun onPluginEnabled(event: PluginEnableEvent) {
        error("!")
    }
}
