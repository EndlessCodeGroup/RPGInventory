package ru.endlesscode.inspector.bukkit.report

import org.bukkit.plugin.PluginManager
import ru.endlesscode.inspector.bukkit.util.printableForm
import ru.endlesscode.inspector.report.ListField

internal class PluginListField(
    pluginManager: PluginManager,
    interestPluginsNames: List<String>
) : ListField<String>(
    name = BukkitEnvironment.FIELD_PLUGIN_LIST,
    produceList = {
        val plugins = pluginManager.plugins.toList()
        val interestPlugins = if (interestPluginsNames.isEmpty()) {
            plugins
        } else {
            plugins.filter { it.name in interestPluginsNames }
        }

        val result = mutableListOf<String>()
        interestPlugins.mapTo(result) { it.printableForm }

        val skipCount = plugins.size - interestPlugins.size
        if (skipCount > 0) {
            result.add("<skipped $skipCount plugins>")
        }

        result
    },
    getSummary = { "<${pluginManager.plugins.size} plugins>" }
)
