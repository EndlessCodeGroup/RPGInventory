package ru.endlesscode.inspector.bukkit.report

import org.bukkit.plugin.Plugin
import ru.endlesscode.inspector.api.report.ReportEnvironment

class BukkitEnvironment(plugin: Plugin) : ReportEnvironment {

    override val fields: List<Pair<String, String>> = listOf(
            "Server core" to "${plugin.server.name} (${plugin.server.version})",
            "Plugin" to "${plugin.name} (v ${plugin.description.version})"
    )
}
