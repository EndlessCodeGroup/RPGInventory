package ru.endlesscode.inspector.bukkit

import org.bukkit.plugin.java.JavaPlugin

/**
 * Wrapper for inspector library to get possibility load it as plugin.
 */
class InspectorPlugin : JavaPlugin() {

    init {
        Inspector.init(dataFolder.resolve("config.yml"))
    }
}
