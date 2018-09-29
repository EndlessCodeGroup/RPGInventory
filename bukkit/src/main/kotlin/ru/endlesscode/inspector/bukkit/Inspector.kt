package ru.endlesscode.inspector.bukkit

import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.Plugin
import ru.endlesscode.inspector.api.PublicApi
import ru.endlesscode.inspector.bukkit.report.DataType
import ru.endlesscode.inspector.bukkit.util.buildPathToFile
import java.io.File
import java.util.UUID

/**
 * Class that represents Inspector's configurations.
 */
class Inspector(private val configFile: File, private val globalConfigFile: File) {

    companion object {
        /**
         * Version of Inspector.
         */
        @JvmStatic
        val version: String = "0.7.0"
    }

    /**
     * Enabling of Inspector.
     */
    var isEnabled: Boolean = true

    /**
     * Unique ID of server. It can be used to determine what reports sent from the same server.
     */
    lateinit var serverId: UUID

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
        configName: String
    ) : this(plugin.dataFolder.resolve(configName), plugin.dataFolder.parentFile.resolve("Inspector/config.yml"))

    /**
     * Reload config from the disk.
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

    /**
     * Checks that sending of the data with specified [type][dataType], enabled in config.
     * Returns `true` if sending is enabled, otherwise `false`.
     */
    fun shouldSendData(dataType: DataType): Boolean = sendData.getValue(dataType)

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
        serverId = globalConfig?.getString("Reporter.server")?.let(UUID::fromString) ?: UUID.randomUUID()
    }

    private fun getBoolean(path: String, defValue: Boolean = true): Boolean {
        return config?.getBoolean(path, defValue) ?: defValue || globalConfig?.getBoolean(path, defValue) ?: defValue
    }
}
