package ru.endlesscode.inspector.bukkit.plugin

import org.bukkit.event.Event
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.PluginLoader
import org.bukkit.plugin.RegisteredListener
import ru.endlesscode.inspector.bukkit.util.realPlugin

class TrackedPluginLoader(
        private val delegate: PluginLoader
) : PluginLoader by delegate {

    override fun createRegisteredListeners(
            listener: Listener,
            plugin: Plugin
    ): MutableMap<Class<out Event>, MutableSet<RegisteredListener>> {
        return delegate.createRegisteredListeners(listener, plugin.realPlugin)
    }

    override fun enablePlugin(plugin: Plugin) {
        delegate.enablePlugin(plugin.realPlugin)
    }

    override fun disablePlugin(plugin: Plugin) {
        delegate.disablePlugin(plugin.realPlugin)
    }
}
