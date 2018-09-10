package ru.endlesscode.inspector.bukkit.plugin

import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.PluginCommand
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.generator.ChunkGenerator
import org.bukkit.plugin.java.JavaPlugin
import ru.endlesscode.inspector.api.report.ReportEnvironment
import ru.endlesscode.inspector.api.report.ReportedException
import ru.endlesscode.inspector.api.report.Reporter
import ru.endlesscode.inspector.api.report.ReporterFocus
import ru.endlesscode.inspector.api.report.SilentReporter
import ru.endlesscode.inspector.bukkit.Inspector
import ru.endlesscode.inspector.bukkit.report.BukkitEnvironment
import java.io.File
import java.io.InputStream
import java.io.Reader
import java.util.logging.Level


/**
 * Class that takes plugin, and watches for all exceptions.
 *
 * @param lifecycleClass The class of a plugin to track.
 */
@Suppress("LeakingThis")
abstract class TrackedPlugin @JvmOverloads constructor(
    lifecycleClass: Class<out PluginLifecycle>,
    envProperties: BukkitEnvironment.Properties = BukkitEnvironment.EMPTY_PROPERTIES
) : JavaPlugin(), ReporterFocus {

    override val focusedPackage: String = javaClass.`package`.name
    override val environment: ReportEnvironment = BukkitEnvironment(this, envProperties)

    val reporter: Reporter
    val plugin: PluginLifecycle = lifecycleClass.newInstance()

    init {
        plugin.holder = this

        reporter = if (Inspector.GLOBAL.isEnabled) createReporter() else SilentReporter
        reporter.addHandler(
                beforeReport = { message, exceptionData ->
                    logger.log(Level.WARNING, "${Inspector.TAG} $message", exceptionData.exception)
                },
                onSuccess = { _, _ ->
                    logger.warning("${Inspector.TAG} Error was reported to author!")
                },
                onError = {
                    logger.severe("${Inspector.TAG} Error on report: ${it.localizedMessage}")
                }
        )
    }

    protected abstract fun createReporter(): Reporter

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
            args: Array<out String>
    ): Boolean {
        return track("Exception occurred on command '$label' with arguments: ${args.contentToString()}") {
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
        track("Error occurred during plugin load") {
            plugin.onLoad()
        }
    }

    final override fun onEnable() {
        track("Error occurred during plugin enable") {
            plugin.onEnable()
        }
    }

    final override fun onDisable() {
        track("Error occurred during plugin disable") {
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

    private fun <T> track(message: String = "Error occurred on plugin lifecycle", block: () -> T): T {
        try {
            return block.invoke()
        } catch (e: Exception) {
            reporter.report(message, e, async = false)
            throw ReportedException(e)
        }
    }

    // Methods to make visible for PluginLifecycle

    internal fun _getFile(): File = file

    internal fun _getClassLoader(): ClassLoader = classLoader

    internal fun _getTextResource(file: String): Reader? = super.getTextResource(file)

    internal fun _getCommand(name: String): PluginCommand? = super.getCommand(name)

    internal fun _setEnabled(enabled: Boolean) {
        isEnabled = enabled
    }
}
