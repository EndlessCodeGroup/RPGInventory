package ru.endlesscode.inspector.bukkit.report

import org.bukkit.plugin.Plugin
import ru.endlesscode.inspector.api.report.ListField
import ru.endlesscode.inspector.api.report.ReportEnvironment
import ru.endlesscode.inspector.api.report.ReportField
import ru.endlesscode.inspector.api.report.TextField
import ru.endlesscode.inspector.bukkit.InspectorConfig
import ru.endlesscode.inspector.bukkit.util.printableForm

class BukkitEnvironment(plugin: Plugin) : ReportEnvironment {

    override val fields: List<Pair<String, ReportField>>

    init {
        fields = listOf(
                "Plugin" to TextField(plugin.printableForm),
                "Server core" to TextField("${plugin.server.name} (${plugin.server.version})")
                        .withCondition(InspectorConfig.shouldSendData(DataType.CORE)),
                "Installed plugins" to ListField(
                        produceList = { plugin.server.pluginManager.plugins.map { it.printableForm } },
                        getSummary = { "<${it.size} plugins>" }
                ).withCondition(InspectorConfig.shouldSendData(DataType.PLUGINS))
        )
    }

    private fun ReportField.withCondition(condition: Boolean): ReportField {
        return if (condition) this else TextField("<sending disabled>")
    }
}
