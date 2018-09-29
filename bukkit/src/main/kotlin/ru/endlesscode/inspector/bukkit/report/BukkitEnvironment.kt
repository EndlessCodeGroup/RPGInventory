package ru.endlesscode.inspector.bukkit.report

import org.bukkit.plugin.Plugin
import ru.endlesscode.inspector.api.report.ReportEnvironment
import ru.endlesscode.inspector.api.report.TextField
import ru.endlesscode.inspector.bukkit.Inspector
import ru.endlesscode.inspector.bukkit.plugin.TrackedPlugin
import ru.endlesscode.inspector.bukkit.util.printableForm


class BukkitEnvironment(
    plugin: Plugin,
    properties: BukkitEnvironment.Properties
) : ReportEnvironment {

    companion object {
        const val TAG_PLUGIN = "Plugin"
        const val TAG_CORE = "Server core"
        const val TAG_PLUGIN_LIST = "Installed plugins"
        const val TAG_INSPECTOR_VERSION = "Inspector version"
        const val TAG_REPORTER_ID = "Reporter ID"

        @JvmStatic
        internal val EMPTY_PROPERTIES = Properties()
    }

    public val inspector = Inspector(plugin)

    override val fields = mapOf(
        TAG_PLUGIN to TextField(TAG_PLUGIN, plugin.printableForm),
        TAG_CORE to TextField(
            TAG_CORE,
            "${plugin.server.name} (${plugin.server.version})"
        ) { inspector.shouldSendData(DataType.CORE) },

        TAG_PLUGIN_LIST to PluginListField(
            plugin.server.pluginManager.plugins.asList(),
            properties.interestPluginsNames
        ) { inspector.shouldSendData(DataType.PLUGINS) },

        TAG_INSPECTOR_VERSION to TextField(TAG_INSPECTOR_VERSION, Inspector.version),
        TAG_REPORTER_ID to TextField(TAG_REPORTER_ID, inspector.reporterId.toString())
    )

    override val defaultFieldsTags: List<String> = listOf(
        TAG_PLUGIN, TAG_CORE, TAG_PLUGIN_LIST, TAG_INSPECTOR_VERSION, TAG_REPORTER_ID
    )

    override val isInspectorEnabled: Boolean
        get() = inspector.isEnabled

    /**
     * Contains properties for environment customization
     */
    class Properties @JvmOverloads constructor(
        val interestPluginsNames: List<String> = emptyList()
    )
}
