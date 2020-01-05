package ru.endlesscode.inspector.bukkit.report

import org.bukkit.plugin.Plugin
import ru.endlesscode.inspector.bukkit.Inspector
import ru.endlesscode.inspector.bukkit.util.printableForm
import ru.endlesscode.inspector.report.ReportEnvironment
import ru.endlesscode.inspector.report.ReportField
import ru.endlesscode.inspector.report.TextField


class BukkitEnvironment internal constructor(
    plugin: Plugin,
    properties: Properties
) : ReportEnvironment {

    companion object {
        const val FIELD_PLUGIN = "Plugin"
        const val FIELD_CORE = "Server core"
        const val FIELD_PLUGIN_LIST = "Installed plugins"
        const val FIELD_INSPECTOR_VERSION = "Inspector version"
        const val FIELD_REPORTER_ID = "Reporter ID"

        @JvmStatic
        internal val DEFAULT_PROPERTIES = Properties()
    }

    val inspector = Inspector(plugin, properties.configName)

    override val appVersion: String = plugin.description.version
    override val reporterId: String = inspector.serverId.toString()

    override val fields: Map<String, ReportField>

    override val isInspectorEnabled: Boolean
        get() = inspector.isEnabled

    init {
        fields = mapOf(
            FIELD_PLUGIN to TextField(FIELD_PLUGIN, plugin.printableForm),
            FIELD_CORE to TextField(
                FIELD_CORE,
                "${plugin.server.name} (${plugin.server.version})"
            ).showOnlyIf { inspector.shouldSendData(DataType.CORE) },

            FIELD_PLUGIN_LIST to PluginListField(
                plugin.server.pluginManager,
                properties.interestPluginsNames
            ).showOnlyIf { inspector.shouldSendData(DataType.PLUGINS) },

            FIELD_INSPECTOR_VERSION to TextField(FIELD_INSPECTOR_VERSION, Inspector.version),
            FIELD_REPORTER_ID to TextField(FIELD_REPORTER_ID, inspector.serverId.toString())
        )
    }

    /**
     * Contains properties for environment customization.
     *
     * @param interestPluginsNames Names of the plugins that should be added to report. Empty list
     * means that we need to receive full list of plugins (this value used by default).
     * @param configName Name of config file that will be used for Inspector'sconfig. This file will
     * be stored in your plugin's folder. By default - "inspector.yml"
     */
    class Properties @JvmOverloads constructor(
        val interestPluginsNames: List<String> = emptyList(),
        val configName: String = "inspector.yml"
    )
}
