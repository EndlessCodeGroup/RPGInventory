package ru.endlesscode.inspector.bukkit.report

import org.bukkit.plugin.Plugin
import ru.endlesscode.inspector.api.report.ReportEnvironment
import ru.endlesscode.inspector.api.report.TextField
import ru.endlesscode.inspector.bukkit.InspectorConfig
import ru.endlesscode.inspector.bukkit.util.printableForm

class BukkitEnvironment(plugin: Plugin) : ReportEnvironment {

    companion object {
        const val TAG_PLUGIN = "Plugin"
        const val TAG_CORE = "Server core"
        const val TAG_PLUGIN_LIST = "Installed plugins"

    }

    override val fields = mapOf(
            TAG_PLUGIN to TextField(TAG_PLUGIN, plugin.printableForm),
            TAG_CORE to TextField(TAG_CORE,"${plugin.server.name} (${plugin.server.version})") { InspectorConfig.shouldSendData(DataType.CORE) },
            TAG_PLUGIN_LIST to PluginListField(plugin.server.pluginManager.plugins)
    )

    override val defaultFieldsTags: List<String> = listOf(TAG_PLUGIN, TAG_CORE, TAG_PLUGIN_LIST)
}
