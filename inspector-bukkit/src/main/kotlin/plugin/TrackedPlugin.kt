package ru.endlesscode.inspector.bukkit.plugin

import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.PluginCommand
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.generator.ChunkGenerator
import org.bukkit.plugin.java.JavaPlugin
import ru.endlesscode.inspector.PublicApi
import ru.endlesscode.inspector.bukkit.report.BukkitEnvironment
import ru.endlesscode.inspector.bukkit.report.BukkitUnwrapReporter
import ru.endlesscode.inspector.report.ReportEnvironment
import ru.endlesscode.inspector.report.ReportedException
import ru.endlesscode.inspector.report.Reporter
import ru.endlesscode.inspector.report.ReporterFocus
import ru.endlesscode.inspector.util.stackTraceText
import java.io.File
import java.io.InputStream
import java.io.Reader
import java.nio.file.Files
import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.util.Date
import java.util.logging.FileHandler
import java.util.logging.Formatter
import java.util.logging.Level
import java.util.logging.LogRecord


/**
 * Class that takes plugin, and watches for all exceptions.
 *
 * @param lifecycleClass The class of a plugin to track.
 */
@Suppress("LeakingThis")
abstract class TrackedPlugin @JvmOverloads constructor(
    lifecycleClass: Class<out PluginLifecycle>,
    envProperties: BukkitEnvironment.Properties = BukkitEnvironment.DEFAULT_PROPERTIES
) : JavaPlugin(), ReporterFocus {

    companion object {
        private const val TAG = "[Inspector]"
    }

    val reporter: Reporter

    override val focusedPackage: String = javaClass.`package`.name
    override val environment: ReportEnvironment = BukkitEnvironment(this, envProperties)

    @PublicApi
    val lifecycle: PluginLifecycle

    init {
        initLogger()

        try {
            lifecycle = lifecycleClass.newInstance()
            lifecycle.holder = this
            lifecycle.init()
        } catch (e: UninitializedPropertyAccessException) {
            logger.severe("$TAG Looks like you trying to use plugin's methods on initialization.")
            logger.severe("$TAG Instead of this, overload method init() and do the work within.")
            throw e
        }

        reporter = BukkitUnwrapReporter(createReporter())
        reporter.enabled = environment.isInspectorEnabled
        reporter.addHandler(
            beforeReport = { message, data ->
                logger.log(Level.FINE, "$TAG $message", data.exception)
            },
            onSuccess = { message, _ ->
                logger.warning("$TAG $message")
                logger.warning("$TAG Error was reported to author!")
            },
            onError = {
                logger.warning("$TAG Error on report: ${it.localizedMessage}")
                logger.log(Level.FINE, TAG, it)
            }
        )
    }

    private fun initLogger() {
        val logsDir = Files.createDirectories(Paths.get("${dataFolder.path}/logs"))
        val fileHandler = FileHandler("$logsDir/latest.log", true).apply {
            level = Level.ALL
            formatter = object : Formatter() {
                val timeFormatter = SimpleDateFormat("dd/MM/yy HH:mm:ss")

                override fun format(record: LogRecord): String {
                    val message = "[${timeFormatter.format(Date(record.millis))} ${record.level}]: ${record.message}\n"
                    return record.thrown?.stackTraceText?.let { "$message$it" } ?: message
                }
            }
        }
        logger.addHandler(fileHandler)
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
            lifecycle.getCommand(name)
        }
    }

    final override fun onCommand(
            sender: CommandSender?,
            command: Command?,
            label: String?,
            args: Array<out String>
    ): Boolean {
        return track("Exception occurred on command '$label' with arguments: ${args.contentToString()}") {
            lifecycle.onCommand(sender, command, label, args)
        }
    }

    final override fun onTabComplete(
            sender: CommandSender?,
            command: Command?,
            alias: String?,
            args: Array<out String>?
    ): MutableList<String>? {
        return track {
            lifecycle.onTabComplete(sender, command, alias, args)
        }
    }

    final override fun onLoad() {
        track("Error occurred during plugin load") {
            lifecycle.onLoad()
        }
    }

    final override fun onEnable() {
        track("Error occurred during plugin enable") {
            lifecycle.onEnable()
        }
    }

    final override fun onDisable() {
        track("Error occurred during plugin disable") {
            lifecycle.onDisable()
        }
    }

    final override fun getDefaultWorldGenerator(worldName: String?, id: String?): ChunkGenerator? {
        return track {
            lifecycle.getDefaultWorldGenerator(worldName, id)
        }
    }

    override fun toString(): String {
        return "$lifecycle [Tracked]"
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

    internal fun directGetFile(): File = file

    internal fun directGetClassLoader(): ClassLoader = classLoader

    internal fun directGetTextResource(file: String): Reader? = super.getTextResource(file)

    internal fun directGetCommand(name: String): PluginCommand? = super.getCommand(name)

    internal fun directSetEnabled(enabled: Boolean) {
        isEnabled = enabled
    }
}
