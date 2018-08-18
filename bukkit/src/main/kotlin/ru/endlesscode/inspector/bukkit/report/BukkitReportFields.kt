package ru.endlesscode.inspector.bukkit.report

import org.bukkit.plugin.Plugin
import ru.endlesscode.inspector.api.report.FilterableListField
import ru.endlesscode.inspector.bukkit.InspectorConfig
import ru.endlesscode.inspector.bukkit.util.printableForm

class PluginListField(plugins: Array<Plugin>) : FilterableListField<String>(
        BukkitEnvironment.TAG_PLUGIN_LIST,
        produceList = { plugins.map { it.printableForm } },
        getSummary = { "<${it.size} plugins>" }
) {

    override val show: Boolean
        get() = InspectorConfig.shouldSendData(DataType.PLUGINS)
}
