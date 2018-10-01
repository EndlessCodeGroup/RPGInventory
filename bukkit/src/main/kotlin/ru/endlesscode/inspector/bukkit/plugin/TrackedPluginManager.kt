package ru.endlesscode.inspector.bukkit.plugin

import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.plugin.AuthorNagException
import org.bukkit.plugin.EventExecutor
import org.bukkit.plugin.IllegalPluginAccessException
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.PluginManager
import org.bukkit.plugin.RegisteredListener
import org.bukkit.plugin.TimedRegisteredListener
import ru.endlesscode.inspector.api.PublicApi
import ru.endlesscode.inspector.api.report.Reporter
import ru.endlesscode.inspector.bukkit.util.EventsUtils
import ru.endlesscode.inspector.bukkit.util.realPlugin


class TrackedPluginManager(
        private val delegate: PluginManager,
        private val reporter: Reporter
) : PluginManager by delegate {

    @PublicApi
    constructor(plugin: TrackedPlugin) : this(plugin.server.pluginManager, plugin.reporter)

    /**
     * Registers all the events in the given listener class.
     *
     * @param listener a listener to register
     * @param plugin a plugin to register
     */
    override fun registerEvents(listener: Listener, plugin: Plugin) {
        val realPlugin = plugin.realPlugin
        if (!realPlugin.isEnabled) {
            throw IllegalPluginAccessException("Plugin attempted to register $listener while not enabled")
        }

        val registeredListeners = realPlugin.pluginLoader.createRegisteredListeners(listener, realPlugin)
        for ((key, listeners) in registeredListeners) {
            val wrapped = listeners.map(::wrapListener)
            EventsUtils.getEventListeners(key).registerAll(wrapped)
        }
    }

    override fun registerEvent(
            event: Class<out Event>,
            listener: Listener,
            priority: EventPriority,
            executor: EventExecutor,
            plugin: Plugin
    ) {
        registerEvent(event, listener, priority, executor, plugin, false)
    }

    override fun registerEvent(
            event: Class<out Event>,
            listener: Listener,
            priority: EventPriority,
            executor: EventExecutor,
            plugin: Plugin,
            ignoreCanceled: Boolean
    ) {
        delegate.registerEvent(event, listener, priority, wrapExecutor(executor), plugin.realPlugin, ignoreCanceled)
    }

    override fun isPluginEnabled(plugin: Plugin?): Boolean {
        return delegate.isPluginEnabled(plugin?.realPlugin)
    }

    override fun enablePlugin(plugin: Plugin) {
        delegate.enablePlugin(plugin.realPlugin)
    }

    override fun disablePlugin(plugin: Plugin) {
        delegate.disablePlugin(plugin.realPlugin)
    }

    private fun wrapListener(delegate: RegisteredListener): RegisteredListener {
        return when (delegate) {
            is TimedRegisteredListener -> wrapTimedRegisteredListener(delegate)
            else -> wrapRegisteredListener(delegate)
        }
    }

    private fun wrapRegisteredListener(delegate: RegisteredListener): RegisteredListener {
        return object : RegisteredListener(
            delegate.listener,
            EventsUtils.NULL_EXECUTOR,
            delegate.priority,
            delegate.plugin,
            delegate.isIgnoringCancelled
        ) {
            override fun callEvent(event: Event) {
                trackEvent(event) {
                    delegate.callEvent(event)
                }
            }
        }
    }

    private fun wrapTimedRegisteredListener(delegate: TimedRegisteredListener): TimedRegisteredListener {
        return object : TimedRegisteredListener(
            delegate.listener,
            EventsUtils.NULL_EXECUTOR,
            delegate.priority,
            delegate.plugin,
            delegate.isIgnoringCancelled
        ) {
            override fun callEvent(event: Event) {
                trackEvent(event) {
                    delegate.callEvent(event)
                }
            }
        }
    }

    private fun wrapExecutor(executor: EventExecutor): EventExecutor {
        return EventExecutor { listener, event ->
            trackEvent(event) {
                executor.execute(listener, event)
            }
        }
    }

    private fun trackEvent(event: Event, block: () -> Unit) {
        try {
            block.invoke()
        } catch (e: AuthorNagException) {
            throw e
        } catch (e: Exception) {
            reporter.report("Error occurred on ${event.eventName}", e)
        }
    }
}
