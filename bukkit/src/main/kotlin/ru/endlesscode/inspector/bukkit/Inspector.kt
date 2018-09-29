package ru.endlesscode.inspector.bukkit

import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import ru.endlesscode.inspector.api.PublicApi
import ru.endlesscode.inspector.bukkit.report.DataType
import java.io.File

class Inspector(private val configFile: File) {

    companion object {
        /**
         * Used Inspector version
         */
        @JvmStatic
        val version: String = "0.7.0"
    }

    internal var isEnabled: Boolean = true

    private var sendData = mutableMapOf(
            DataType.CORE to true,
            DataType.PLUGINS to true
    )

    private var config: FileConfiguration? = null

    init {
        reload()
    }

    /**
     * Reload config from disk.
     */
    @PublicApi
    fun reload() {
        this.config = YamlConfiguration.loadConfiguration(configFile).apply {
            val defaultConfigStream = javaClass.getResourceAsStream("config.yml") ?: return
            defaults = YamlConfiguration.loadConfiguration(defaultConfigStream.reader())
            options().copyDefaults(true)
            save(configFile)
        }

        copyValuesFromConfig()
    }

    internal fun shouldSendData(dataType: DataType) = sendData.getValue(dataType)

    private fun copyValuesFromConfig() {
        config?.let { config ->
            isEnabled = config.getBoolean("Reporter.enabled", true)
            sendData[DataType.CORE] = config.getBoolean("Reporter.data.core", true)
            sendData[DataType.PLUGINS] = config.getBoolean("Reporter.data.plugins", true)
        }
    }
}
