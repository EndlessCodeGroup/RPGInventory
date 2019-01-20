package ru.endlesscode.inspector.bukkit.plugin

import org.bukkit.Server
import org.bukkit.plugin.PluginManager
import org.bukkit.scheduler.BukkitScheduler
import ru.endlesscode.inspector.PublicApi
import ru.endlesscode.inspector.report.Reporter
import ru.endlesscode.inspector.bukkit.scheduler.TrackedScheduler

class TrackedServer(
    @PublicApi
    val server: Server,
    reporter: Reporter
) : Server by server {

    constructor(plugin: TrackedPlugin) : this(plugin.server, plugin.reporter)

    private val trackedPluginManager: PluginManager by lazy { TrackedPluginManager(server.pluginManager, reporter) }
    private val trackedScheduler: BukkitScheduler by lazy { TrackedScheduler(server.scheduler, reporter) }

    override fun getPluginManager(): PluginManager {
        return trackedPluginManager
    }

    override fun getScheduler(): BukkitScheduler {
        return trackedScheduler
    }
}
