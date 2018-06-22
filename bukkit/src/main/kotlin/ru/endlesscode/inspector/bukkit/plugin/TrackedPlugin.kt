package ru.endlesscode.inspector.bukkit.plugin

import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.PluginCommand
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.generator.ChunkGenerator
import org.bukkit.plugin.java.JavaPlugin
import ru.endlesscode.inspector.api.report.ReporterFocus
import java.io.InputStream


/**
 * Class that takes plugin, and watches for all exceptions.
 *
 * @param pluginClass class of plugin to track
 */
open class TrackedPlugin(pluginClass: Class<out InnerPlugin>) : JavaPlugin(), ReporterFocus {

    override val focusedPackage: String = javaClass.`package`.name

    val plugin: InnerPlugin = pluginClass.newInstance()

    init {
        plugin.holder = this
    }

    final override fun getConfig(): FileConfiguration {
        return super.getConfig()
    }

    final override fun reloadConfig() {
        super.reloadConfig()
    }

    final override fun saveConfig() {
        super.saveConfig()
    }

    final override fun saveDefaultConfig() {
        super.saveDefaultConfig()
    }

    final override fun saveResource(resourcePath: String?, replace: Boolean) {
        super.saveResource(resourcePath, replace)
    }

    final override fun getResource(filename: String?): InputStream? {
        return super.getResource(filename)
    }

    final override fun getCommand(name: String): PluginCommand? {
        return track {
            plugin.getCommand(name)
        }
    }

    final override fun onCommand(
            sender: CommandSender?,
            command: Command?,
            label: String?,
            args: Array<out String>?
    ): Boolean {
        return track {
            plugin.onCommand(sender, command, label, args)
        }
    }

    final override fun onTabComplete(
            sender: CommandSender?,
            command: Command?,
            alias: String?,
            args: Array<out String>?
    ): MutableList<String>? {
        return track {
            plugin.onTabComplete(sender, command, alias, args)
        }
    }

    final override fun onLoad() {
        track {
            plugin.onLoad()
        }
    }

    final override fun onEnable() {
        track {
            plugin.onEnable()
        }
    }

    final override fun onDisable() {
        track {
            plugin.onDisable()
        }
    }

    final override fun getDefaultWorldGenerator(worldName: String?, id: String?): ChunkGenerator? {
        return track {
            plugin.getDefaultWorldGenerator(worldName, id)
        }
    }

    override fun toString(): String {
        return "$plugin [Tracked]"
    }

    private fun <T> track(block: () -> T): T {
        try {
            return block.invoke()
        } catch (e: Exception) {
            logger.warning("Inspector tracked exception!")
            throw e
        }
    }
}
