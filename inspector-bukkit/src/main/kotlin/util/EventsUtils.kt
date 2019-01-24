package ru.endlesscode.inspector.bukkit.util

import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import org.bukkit.plugin.EventExecutor
import org.bukkit.plugin.IllegalPluginAccessException


internal object EventsUtils {

    val NULL_EXECUTOR = EventExecutor { _, _ -> error("This method should never be called!") }

    // Methods from SimplePluginManager

    fun getEventListeners(type: Class<out Event>): HandlerList {
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
