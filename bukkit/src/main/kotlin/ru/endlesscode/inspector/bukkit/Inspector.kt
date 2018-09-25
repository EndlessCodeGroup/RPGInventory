package ru.endlesscode.inspector.bukkit

import org.bukkit.plugin.java.JavaPlugin
import ru.endlesscode.inspector.bukkit.report.DataType

class Inspector : JavaPlugin() {

    companion object {
        const val TAG = "[Inspector]"
    }

    init {
        loadConfig()
    }

    private fun loadConfig() {
        config.options().copyDefaults(true)
        saveConfig()

        with(InspectorConfig) {
            isEnabled = config.getBoolean("Reporter.enabled", true)
            sendData[DataType.CORE] = config.getBoolean("Reporter.data.core", true)
            sendData[DataType.PLUGINS] = config.getBoolean("Reporter.data.plugins", true)
        }
    }
}
