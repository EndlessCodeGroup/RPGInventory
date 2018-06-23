package ru.endlesscode.inspector.bukkit

import org.bukkit.event.Listener
import ru.endlesscode.inspector.bukkit.plugin.PluginLifecycle

class InspectorPlugin : PluginLifecycle(), Listener {

    override fun onEnable() {
        logger.info("onEnable")
    }

    override fun onDisable() {
        logger.info("onEnable")
    }
}
