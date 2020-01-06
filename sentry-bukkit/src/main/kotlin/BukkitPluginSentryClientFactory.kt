package ru.endlesscode.inspector.sentry.bukkit

import io.sentry.DefaultSentryClientFactory
import io.sentry.SentryClient
import io.sentry.config.Lookup
import io.sentry.context.ContextManager
import io.sentry.context.SingletonContextManager
import io.sentry.dsn.Dsn
import io.sentry.event.EventBuilder
import io.sentry.event.helper.EventBuilderHelper
import org.bukkit.Server
import org.bukkit.plugin.Plugin

/**
 * SentryClientFactory that handles Bukkit-specific construction, like logging
 * server and plugin information.
 */
class BukkitPluginSentryClientFactory @JvmOverloads constructor(
    plugin: Plugin,
    lookup: Lookup = Lookup.getDefault()
) : DefaultSentryClientFactory(lookup) {

    private val buildHelper = BukkitPluginBuilderHelper(plugin)

    override fun createSentryClient(dsn: Dsn?): SentryClient {
        return super.createSentryClient(dsn).apply {
            addBuilderHelper(buildHelper)
        }
    }

    override fun getContextManager(dsn: Dsn?): ContextManager = SingletonContextManager()
}

private class BukkitPluginBuilderHelper(private val plugin: Plugin) : EventBuilderHelper {

    companion object {
        private val KNOWN_SERVERS = listOf("Paper", "Spigot", "CraftBukkit")
    }

    private val Server.knownName: String
        get() = (KNOWN_SERVERS.find { it in version } ?: "Unknown").toLowerCase()
    private val Server.minecraftVersion: String
        get() = bukkitVersion.substringBefore('-')

    override fun helpBuildingEvent(eventBuilder: EventBuilder): Unit = with (eventBuilder) {
        withSdkIntegration("bukkit")
        withRelease(plugin.description.version)
        withServerName(plugin.server.knownName)
        withTag("minecraft", plugin.server.minecraftVersion)
        withContexts(getContexts())
    }

    private fun getContexts(): Map<String, Map<String, Any>> {
        val serverMap = mapOf(
            "Name" to plugin.server.knownName,
            "Version" to plugin.server.version,
            "Minecraft Version" to plugin.server.minecraftVersion
        )
        val pluginMap = mapOf(
            "Name" to plugin.description.name,
            "Version" to plugin.description.version,
            "API Version" to plugin.description.apiVersion.toString()
        )
        return mapOf("server" to serverMap, "plugin" to pluginMap)
    }
}
