package ru.endlesscode.inspector.bukkit

import org.bukkit.plugin.AuthorNagException
import org.bukkit.plugin.java.JavaPlugin

/**
 * Wrapper for the Inspector to make impossible load it as plugin.
 */
class InspectorPlugin : JavaPlugin() {

    override fun onLoad() {
        logger.severe("")
        logger.severe("Since v0.7.0 Inspector can't be loaded as a plugin more!")
        logger.severe("")
        logger.severe("What to do?")
        logger.severe("  Please report about it to author of the plugin, that requires Inspector.")
        logger.severe("  He must bundle it to the plugin or you can use Inspector v0.6.0.")
        logger.severe("")

        throw AuthorNagException("Aggrh!!! Throwing an exception is only one possible way to disable plugin in onLoad()")
    }
}
