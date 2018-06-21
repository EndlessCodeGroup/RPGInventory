package ru.endlesscode.inspector.bukkit.plugin

import org.bukkit.Server
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.PluginManager
import java.util.logging.Logger

class TrackedServer(val server: Server, logger: Logger) : Server by server {

    constructor(plugin: Plugin) : this(plugin.server, plugin.logger)

    private val pluginManager by lazy { TrackedPluginManager(server.pluginManager, logger) }

    override fun getPluginManager(): PluginManager {
        return pluginManager
    }
}
