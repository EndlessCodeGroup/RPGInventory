package ru.endlesscode.inspector.bukkit.report

import org.bukkit.plugin.Plugin
import ru.endlesscode.inspector.api.report.ListField
import ru.endlesscode.inspector.bukkit.util.printableForm

class PluginListField(
    plugins: List<Plugin>,
    interestPluginsNames: List<String>,
    shouldShow: ListField<String>.() -> Boolean = { true }
) : ListField<String>(
    BukkitEnvironment.TAG_PLUGIN_LIST,
    produceList = {
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
    getSummary = { "<${plugins.size} plugins>" },
    shouldShow = shouldShow
)
