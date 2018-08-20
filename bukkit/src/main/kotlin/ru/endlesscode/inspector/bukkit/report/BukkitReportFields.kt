package ru.endlesscode.inspector.bukkit.report

import org.bukkit.plugin.Plugin
import ru.endlesscode.inspector.api.report.ListField
import ru.endlesscode.inspector.bukkit.Inspector
import ru.endlesscode.inspector.bukkit.util.printableForm

class PluginListField(plugins: List<Plugin>, interestPluginsNames: List<String> = emptyList()) : ListField<String>(
        BukkitEnvironment.TAG_PLUGIN_LIST,
        produceList = {
            val interestPlugins = if (interestPluginsNames.isEmpty()) {
                plugins
            } else {
                plugins.filter { it.name in interestPluginsNames }
            }

            interestPlugins.map { it.printableForm }
        },
        getSummary = { "<${it.size} plugins>" },
        shouldShow = { Inspector.GLOBAL.shouldSendData(DataType.PLUGINS) }
)
