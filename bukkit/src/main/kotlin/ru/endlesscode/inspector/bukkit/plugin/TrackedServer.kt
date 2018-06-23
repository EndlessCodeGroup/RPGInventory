package ru.endlesscode.inspector.bukkit.plugin

import org.bukkit.Server
import org.bukkit.plugin.PluginManager
import ru.endlesscode.inspector.api.report.Reporter

class TrackedServer(val server: Server, reporter: Reporter) : Server by server {

    constructor(plugin: TrackedPlugin) : this(plugin.server, plugin.reporter)

    private val pluginManager by lazy { TrackedPluginManager(server.pluginManager, reporter) }

    override fun getPluginManager(): PluginManager {
        return pluginManager
    }
}
