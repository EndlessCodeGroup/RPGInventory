package ru.endlesscode.inspector.bukkit

import org.bukkit.plugin.java.JavaPlugin
import ru.endlesscode.inspector.bukkit.event.EventsLogger
import ru.endlesscode.inspector.bukkit.event.LogRule
import ru.endlesscode.inspector.bukkit.packet.PacketsLogger
import ru.endlesscode.inspector.bukkit.report.DataType

class Inspector : JavaPlugin() {

    companion object {
        @JvmStatic
        lateinit var GLOBAL: InspectorConfig

        const val TAG = "[Inspector]"
    }

    init {
        GLOBAL = InspectorConfig(description.version)
        loadConfig()

        enableEventsLogger()
    }

    override fun onEnable() {
        enablePacketsLogger()
    }

    private fun loadConfig() {
        config.options().copyDefaults(true)
        saveConfig()

        with (GLOBAL) {
            isEnabled = config.getBoolean("Reporter.enabled", true)
            sendData[DataType.CORE] = config.getBoolean("Reporter.data.core", true)
            sendData[DataType.PLUGINS] = config.getBoolean("Reporter.data.plugins", true)
            isEventsLoggerEnabled = config.getBoolean("EventsLogger.enabled", false)
        }
    }

    private fun enableEventsLogger() {
        if (GLOBAL.isEventsLoggerEnabled) {
            val showHierarchy = config.getBoolean("EventsLogger.hierarchy", true)
            val eventsLogger = EventsLogger(server.consoleSender, loadEventsLogRules(), showHierarchy)
            eventsLogger.inject(this)
        }
    }

    private fun enablePacketsLogger() {
        val packetsLogger = PacketsLogger(server.consoleSender)
        packetsLogger.inject(this)
    }

    private fun loadEventsLogRules(): Map<String, LogRule> {
        val rules = config.getStringList("EventsLogger.log")

        return rules.map {
            val rule = LogRule.fromString(it)
            rule.event to rule
        }.toMap()
    }
}
