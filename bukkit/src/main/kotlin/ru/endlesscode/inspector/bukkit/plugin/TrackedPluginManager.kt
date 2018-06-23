package ru.endlesscode.inspector.bukkit.plugin

import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.plugin.AuthorNagException
import org.bukkit.plugin.EventExecutor
import org.bukkit.plugin.IllegalPluginAccessException
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.PluginManager
import org.bukkit.plugin.RegisteredListener
import ru.endlesscode.inspector.api.report.Reporter


class TrackedPluginManager(
        private val delegate: PluginManager,
        private val reporter: Reporter
) : PluginManager by delegate {

    companion object {
        // Just in case Bukkit decides to validate the parameters in the future
        private val NULL_EXECUTOR = EventExecutor { _, _ -> error("This method should never be called!") }
    }

    constructor(plugin: TrackedPlugin) : this(plugin.server.pluginManager, plugin.reporter)

    /**
     * Registers all the events in the given listener class.
     *
     * @param listener a listener to register
     * @param plugin a plugin to register
     */
    override fun registerEvents(listener: Listener, plugin: Plugin) {
        if (!plugin.isEnabled) {
            throw IllegalPluginAccessException("Plugin attempted to register $listener while not enabled")
        }

        val registeredListeners = plugin.pluginLoader.createRegisteredListeners(listener, plugin)
        for ((key, listeners) in registeredListeners) {
            val wrapped = wrapAllListeners(listeners)
            getEventListeners(getRegistrationClass(key)).registerAll(wrapped)
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
        delegate.registerEvent(event, listener, priority, wrapExecutor(executor), plugin, ignoreCanceled)
    }

    private fun wrapAllListeners(listeners: Iterable<RegisteredListener>): List<RegisteredListener> {
        val wrapped = arrayListOf<RegisteredListener>()
        val it = listeners.iterator()
        while (it.hasNext()) {
            val originalListener = it.next()
            val wrappedListener = wrapListener(originalListener)
            wrapped.add(wrappedListener)
        }

        return wrapped
    }

    private fun wrapListener(delegate: RegisteredListener): RegisteredListener {
        return object : RegisteredListener(
                delegate.listener,
                NULL_EXECUTOR,
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

    // Methods from SimplePluginManager

    private fun getEventListeners(type: Class<out Event>): HandlerList {
        try {
            val method = getRegistrationClass(type).getHandlerListMethod()
            method.isAccessible = true
            return method.invoke(null, *arrayOfNulls(0)) as HandlerList
        } catch (e: Exception) {
            throw IllegalPluginAccessException(e.toString())
        }
    }

    private fun getRegistrationClass(type: Class<out Event>): Class<out Event> {
        try {
            type.getHandlerListMethod()
            return type
        } catch (e: NoSuchMethodException) {
            if (Event::class.java != type.superclass && Event::class.java.isAssignableFrom(type.superclass)) {
                return getRegistrationClass(type.superclass.asSubclass(Event::class.java))
            }
        }

        throw IllegalPluginAccessException("Unable to find handler list for event ${type.name}")
    }

    private fun Class<out Event>.getHandlerListMethod() = getDeclaredMethod("getHandlerList", *arrayOfNulls(0))
}
