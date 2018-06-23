package ru.endlesscode.inspector.bukkit

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.server.PluginEnableEvent
import ru.endlesscode.inspector.bukkit.plugin.PluginLifecycle

class InspectorPlugin : PluginLifecycle(), Listener {

    override fun onEnable() {
        logger.info("onEnable")

        server.pluginManager.registerEvents(this, this)
    }

    override fun onDisable() {
        logger.info("onEnable")
        error("Oh noooooo")
    }

    @EventHandler
    fun onPluginEnabled(event: PluginEnableEvent) {
        error("Error message here")
    }
}
