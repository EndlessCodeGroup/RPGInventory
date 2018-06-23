package ru.endlesscode.inspector.bukkit.plugin

import org.bukkit.Server
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.PluginCommand
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.generator.ChunkGenerator
import org.bukkit.plugin.PluginBase
import org.bukkit.plugin.PluginDescriptionFile
import org.bukkit.plugin.PluginLoader
import java.io.File
import java.io.InputStream
import java.util.*
import java.util.logging.Logger

open class PluginLifecycle : PluginBase() {

    internal lateinit var holder: TrackedPlugin

    private val trackedServer by lazy { TrackedServer(holder) }

    final override fun getDataFolder(): File {
        return holder.dataFolder
    }

    final override fun getPluginLoader(): PluginLoader {
        return holder.pluginLoader
    }

    final override fun getServer(): Server {
        return trackedServer
    }

    final override fun isEnabled(): Boolean {
        return holder.isEnabled
    }

    final override fun getDescription(): PluginDescriptionFile {
        return holder.description
    }

    final override fun getConfig(): FileConfiguration {
        return holder.config
    }

    final override fun reloadConfig() {
        holder.reloadConfig()
    }

    final override fun saveConfig() {
        holder.saveConfig()
    }

    final override fun saveDefaultConfig() {
        holder.saveDefaultConfig()
    }

    final override fun saveResource(resourcePath: String?, replace: Boolean) {
        holder.saveResource(resourcePath, replace)
    }

    final override fun getResource(filename: String?): InputStream? {
        return holder.getResource(filename)
    }

    override fun onCommand(
            sender: CommandSender?,
            command: Command?,
            label: String?,
            args: Array<out String>?
    ): Boolean {
        return false
    }

    override fun onTabComplete(
            sender: CommandSender?,
            command: Command?,
            alias: String?,
            args: Array<out String>?
    ): MutableList<String>? {
        return null
    }

    fun getCommand(name: String): PluginCommand? {
        // Default implementation from JavaPlugin
        val alias = name.toLowerCase(Locale.ENGLISH)
        var command: PluginCommand? = this.server.getPluginCommand(alias)
        if (command == null || command.plugin !== this) {
            command = this.server.getPluginCommand(this.description.name.toLowerCase(Locale.ENGLISH) + ":" + alias)
        }

        return if (command != null && command.plugin === this) command else null
    }

    override fun onLoad() {}

    override fun onEnable() {}

    override fun onDisable() {}

    override fun getDefaultWorldGenerator(worldName: String?, id: String?): ChunkGenerator? {
        return null
    }

    final override fun isNaggable(): Boolean {
        return holder.isNaggable
    }

    final override fun setNaggable(canNag: Boolean) {
        holder.isNaggable = canNag
    }

    final override fun getLogger(): Logger {
        return holder.logger
    }

    override fun toString(): String {
        return this.description.fullName
    }
}
