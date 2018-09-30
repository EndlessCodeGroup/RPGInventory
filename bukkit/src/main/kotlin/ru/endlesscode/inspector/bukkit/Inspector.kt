package ru.endlesscode.inspector.bukkit

import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.Plugin
import ru.endlesscode.inspector.api.PublicApi
import ru.endlesscode.inspector.bukkit.report.DataType
import ru.endlesscode.inspector.bukkit.util.FileUtil
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

        // Preserved value for case if global config not contains server ID yet
        private val newServerId by lazy { UUID.randomUUID() }
    }

    /**
     * Enabling of Inspector.
     */
    var isEnabled: Boolean = true

    /**
     * Unique ID of server. It can be used to determine what reports sent from the same server.
     */
    var serverId: UUID

    private var sendData = mutableMapOf(
        DataType.CORE to true,
        DataType.PLUGINS to true
    )

    private val config = YamlConfiguration()
    private val globalConfig = YamlConfiguration()

    init {
        FileUtil.createFileIfNotExists(configFile)
        FileUtil.createFileIfNotExists(globalConfigFile)

        serverId = readServerId()

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
        config.load(configFile)
        globalConfig.load(globalConfigFile)

        readValuesFromConfig()

        config.save(configFile)
        globalConfig.save(globalConfigFile)
    }

    /**
     * Checks that sending of the data with specified [type][dataType], enabled in config.
     * @return `true` if sending is enabled, otherwise `false`.
     */
    fun shouldSendData(dataType: DataType): Boolean = sendData.getValue(dataType)

    private fun readValuesFromConfig() {
        isEnabled = readBoolean("Reporter.enabled")
        sendData[DataType.CORE] = readBoolean("Reporter.data.core")
        sendData[DataType.PLUGINS] = readBoolean("Reporter.data.plugins")
        serverId = readServerId()
    }

    private fun readBoolean(path: String): Boolean {
        // Assumes that `false` more important than `true` and `true` is default value.
        return config.getBoolean(path, true) || globalConfig.getBoolean(path, true)
    }

    private fun readServerId(): UUID {
        return globalConfig.getString("Reporter.server")?.let(UUID::fromString) ?: newServerId
    }
}
