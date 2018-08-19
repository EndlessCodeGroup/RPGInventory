package ru.endlesscode.inspector.bukkit.report

import org.bukkit.plugin.Plugin
import ru.endlesscode.inspector.api.report.ReportEnvironment
import ru.endlesscode.inspector.api.report.TextField
import ru.endlesscode.inspector.bukkit.Inspector
import ru.endlesscode.inspector.bukkit.plugin.TrackedPlugin
import ru.endlesscode.inspector.bukkit.util.printableForm

class BukkitEnvironment(plugin: TrackedPlugin) : ReportEnvironment {

    companion object {
        const val TAG_PLUGIN = "Plugin"
        const val TAG_CORE = "Server core"
        const val TAG_PLUGIN_LIST = "Installed plugins"
        const val TAG_INSPECTOR_VERSION = "Inspector version"
    }

    override val fields = mapOf(
            TAG_PLUGIN to TextField(TAG_PLUGIN, plugin.printableForm),
            TAG_CORE to TextField(TAG_CORE,"${plugin.server.name} (${plugin.server.version})") { Inspector.GLOBAL.shouldSendData(DataType.CORE) },
            TAG_PLUGIN_LIST to PluginListField(plugin.server.pluginManager.plugins.asList(), plugin.interestPluginsNames),
            TAG_INSPECTOR_VERSION to TextField(TAG_INSPECTOR_VERSION, Inspector.GLOBAL.version)
    )

    override val defaultFieldsTags: List<String> = listOf(TAG_PLUGIN, TAG_CORE, TAG_PLUGIN_LIST, TAG_INSPECTOR_VERSION)
}
