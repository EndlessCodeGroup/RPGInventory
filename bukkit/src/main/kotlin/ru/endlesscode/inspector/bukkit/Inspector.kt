package ru.endlesscode.inspector.bukkit

import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.Plugin
import ru.endlesscode.inspector.api.PublicApi
import ru.endlesscode.inspector.bukkit.report.DataType
import ru.endlesscode.inspector.bukkit.util.buildPathToFile
import java.io.File
import java.util.UUID

class Inspector(private val configFile: File, private val globalConfigFile: File) {

    companion object {
        /**
         * Used Inspector version
         */
        @JvmStatic
        val version: String = "0.7.0"

        private const val DEFAULT_CONFIG_NAME = "inspector.yml"
    }

    internal var isEnabled: Boolean = true
    lateinit var reporterId: UUID

    private var sendData = mutableMapOf(
            DataType.CORE to true,
            DataType.PLUGINS to true
    )

    private var config: FileConfiguration? = null
    private var globalConfig: FileConfiguration? = null

    init {
        configFile.buildPathToFile()
        globalConfigFile.buildPathToFile()

        reload()
    }

    constructor(
        plugin: Plugin,
        configName: String = DEFAULT_CONFIG_NAME
    ) : this(plugin.dataFolder.resolve(configName), plugin.dataFolder.parentFile.resolve("Inspector/config.yml"))

    /**
     * Reload config from disk.
     */
    @PublicApi
    fun reload() {
        val defaultConfigStream = javaClass.getResourceAsStream("config.yml") ?: return
        defaultConfigStream.use {
            val defaultConfig = YamlConfiguration.loadConfiguration(it.reader())
            config = loadConfig(configFile, defaultConfig)
            globalConfig = loadConfig(globalConfigFile, defaultConfig)
        }

        copyValuesFromConfig()

        config?.save(configFile)
        globalConfig?.save(globalConfigFile)
    }

    internal fun shouldSendData(dataType: DataType) = sendData.getValue(dataType)

    private fun loadConfig(configFile: File, defaultConfig: YamlConfiguration): FileConfiguration {
        return YamlConfiguration.loadConfiguration(configFile).apply {
            defaults = defaultConfig
            options().copyDefaults(true)
        }
    }

    private fun copyValuesFromConfig() {
        isEnabled = getBoolean("Reporter.enabled", true)
        sendData[DataType.CORE] = getBoolean("Reporter.data.core", true)
        sendData[DataType.PLUGINS] = getBoolean("Reporter.data.plugins", true)
        reporterId = globalConfig?.getString("Reporter.id")?.let(UUID::fromString) ?: UUID.randomUUID()
    }

    private fun getBoolean(path: String, defValue: Boolean = true): Boolean {
        return config?.getBoolean(path, defValue) ?: defValue || globalConfig?.getBoolean(path, defValue) ?: defValue
    }
}
