package ru.endlesscode.inspector.bukkit.report

import org.bukkit.plugin.Plugin
import ru.endlesscode.inspector.api.report.FilterableListField
import ru.endlesscode.inspector.bukkit.Inspector
import ru.endlesscode.inspector.bukkit.util.printableForm

class PluginListField(plugins: Array<Plugin>) : FilterableListField<String>(
        BukkitEnvironment.TAG_PLUGIN_LIST,
        produceList = { plugins.map { it.printableForm } },
        getSummary = { "<${it.size} plugins>" }
) {

    override val show: Boolean
        get() = Inspector.GLOBAL.shouldSendData(DataType.PLUGINS)
}
