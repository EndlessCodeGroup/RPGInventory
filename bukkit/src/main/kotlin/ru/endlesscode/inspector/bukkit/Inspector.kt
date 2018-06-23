package ru.endlesscode.inspector.bukkit

import org.bukkit.plugin.java.JavaPlugin

class Inspector : JavaPlugin() {

    override fun onEnable() {
        loadConfig()
    }

    private fun loadConfig() {
        config.options().copyDefaults(true)
        saveConfig()

        with (InspectorConfig) {
            isEnabled = config.getBoolean("enabled", true)
            sendData[CORE] = config.getBoolean("data.core", true)
            sendData[PLUGINS] = config.getBoolean("data.plugins", true)
        }
    }
}
