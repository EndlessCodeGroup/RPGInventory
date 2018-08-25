package ru.endlesscode.inspector.bukkit.event

import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.RegisteredListener
import ru.endlesscode.inspector.bukkit.util.EventsUtils
import java.util.logging.Logger


internal class EventsLogger(val logger: Logger, private val rules: Map<String, LogRule>) {

    companion object {
        private const val TAG = "[Event]"
    }

    fun inject(plugin: Plugin) {
        val registeredListener = object : RegisteredListener(
                EventsUtils.NULL_LISTENER,
                EventsUtils.NULL_EXECUTOR,
                EventPriority.MONITOR,
                plugin,
                false
        ) {
            override fun callEvent(event: Event) {
                val logRule = findLogRule(event.javaClass) ?: return

                logRule.onEvent()
                if (logRule.log) {
                    logEvent(event, logRule)
                    logRule.afterLog()
                }
            }
        }

        injectToAllEvents(registeredListener)
    }

    private fun findLogRule(eventClass: Class<*>): LogRule? {
        val eventName = eventClass.simpleName
        if (eventName in rules) return rules.getValue(eventName)

        return if (eventClass == Event::class.java) null else findLogRule(eventClass.superclass)
    }

    private fun logEvent(event: Event, logRule: LogRule) {
        val (hierarchy, details) = EventDetails.forEvent(event)

        val count = if (logRule.count > 1) " (skipped ${logRule.count})" else ""
        val sb = buildString {
            append("$TAG ${event.eventName}$count\n")
            append("    Hierarchy: ").append(hierarchy).append("\n    Fields:\n")
            var prefix = ""
            for (detail in details) {
                append(prefix)
                append("        ")
                append(detail)
                prefix = "\n"
            }
        }

        logger.info(sb)
    }

    private fun injectToAllEvents(registeredListener: RegisteredListener) {
        for (eventClass in EventsUtils.eventsClasses) {
            injectToEvent(eventClass, registeredListener)
        }
    }

    private fun injectToEvent(eventClass: Class<out Event>, registeredListener: RegisteredListener) {
        try {
            val handlerList = EventsUtils.getEventListeners(eventClass)
            handlerList.register(registeredListener)
        } catch (e: Exception) {
            println("${eventClass.simpleName} - ${e.message}")
        }
    }
}