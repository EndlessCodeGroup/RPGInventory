package ru.endlesscode.inspector.bukkit.report

import org.bukkit.plugin.Plugin
import ru.endlesscode.inspector.api.report.ListField
import ru.endlesscode.inspector.api.report.ReportEnvironment
import ru.endlesscode.inspector.api.report.ReportField
import ru.endlesscode.inspector.api.report.TextField

class BukkitEnvironment(plugin: Plugin) : ReportEnvironment {

    override val fields: List<Pair<String, ReportField>>

    init {
        fields = listOf(
                "Plugin" to TextField(plugin.printableForm),
                "Server core" to TextField("${plugin.server.name} (${plugin.server.version})"),
                "Installed plugins" to ListField(
                        getList = { plugin.server.pluginManager.plugins .map { it.printableForm } },
                        getShortValue = { "<${it.size} plugins>" }
                )
        )
    }

    private val Plugin.printableForm get() = "$name v${description.version}"
}
