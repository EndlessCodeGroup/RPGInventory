package ru.endlesscode.inspector.bukkit

import org.bukkit.plugin.java.JavaPlugin
import ru.endlesscode.inspector.bukkit.log.EventsLogger
import ru.endlesscode.inspector.bukkit.log.LogRule
import ru.endlesscode.inspector.bukkit.log.PacketsLogger
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

        if (GLOBAL.isEventsLoggerEnabled) {
            enableEventsLogger()
        }
    }

    override fun onEnable() {
        if (GLOBAL.isPacketsLoggerEnabled) {
            enablePacketsLogger()
        }
    }

    private fun loadConfig() {
        config.options().copyDefaults(true)
        saveConfig()

        with (GLOBAL) {
            isEnabled = config.getBoolean("Reporter.enabled", true)
            sendData[DataType.CORE] = config.getBoolean("Reporter.data.core", true)
            sendData[DataType.PLUGINS] = config.getBoolean("Reporter.data.plugins", true)
            isEventsLoggerEnabled = config.getBoolean("EventsLogger.enabled", false)
            isPacketsLoggerEnabled = config.getBoolean("PacketsLogger.enabled", false)
        }
    }

    private fun enableEventsLogger() {
        val showHierarchy = config.getBoolean("EventsLogger.hierarchy", true)
        val eventsLogger = EventsLogger(server.consoleSender, loadEventsLogRules(), showHierarchy)
        eventsLogger.inject(this)
    }

    private fun enablePacketsLogger() {
        if (!server.pluginManager.isPluginEnabled("ProtocolLib")) {
            logger.warning("ProtocolLib not found, packets logger will be disabled!")
            return
        }

        val packetsLogger = PacketsLogger(server.consoleSender, loadPacketsLogRules())
        packetsLogger.inject(this)
    }

    private fun loadEventsLogRules(): Map<String, LogRule> {
        return loadLogRules(config.getStringList("EventsLogger.log"))
    }

    private fun loadPacketsLogRules(): Map<String, LogRule> {
        return loadLogRules(config.getStringList("PacketsLogger.log"))
    }

    private fun loadLogRules(rules: List<String>): Map<String, LogRule> {
        return rules.map {
            val rule = LogRule.fromString(it)
            rule.name to rule
        }.toMap()
    }
}
