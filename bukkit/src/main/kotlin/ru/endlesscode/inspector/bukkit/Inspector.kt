package ru.endlesscode.inspector.bukkit

import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import ru.endlesscode.inspector.bukkit.report.DataType
import java.io.File

object Inspector {

    @JvmStatic
    val version: String = "0.6.0"

    internal var isEnabled: Boolean = true

    private var sendData = mutableMapOf(
            DataType.CORE to true,
            DataType.PLUGINS to true
    )

    private const val configName = "config.yml"
    private var configFile: File? = null
    private var config: FileConfiguration? = null

    internal fun shouldSendData(dataType: DataType) = sendData.getValue(dataType)

    /**
     * Initialize config with the given [config file][configFile] and load it.
     */
    @JvmStatic
    fun init(configFile: File) {
        if (config != null) error("Config already initialized, use reload() instead.")

        this.configFile = configFile
        reload()
    }

    /**
     * Reload config from disk.
     */
    @JvmStatic
    fun reload() {
        this.config = YamlConfiguration.loadConfiguration(configFile).apply {
            val defaultConfigStream = javaClass.getResourceAsStream(configName) ?: return
            defaults = YamlConfiguration.loadConfiguration(defaultConfigStream.reader())
            options().copyDefaults(true)
            save(configFile)
        }

        copyValuesFromConfig()
    }

    private fun copyValuesFromConfig() {
        config?.let { config ->
            isEnabled = config.getBoolean("Reporter.enabled", true)
            sendData[DataType.CORE] = config.getBoolean("Reporter.data.core", true)
            sendData[DataType.PLUGINS] = config.getBoolean("Reporter.data.plugins", true)
        }
    }
}
