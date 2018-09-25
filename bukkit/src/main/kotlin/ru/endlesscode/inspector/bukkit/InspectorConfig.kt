package ru.endlesscode.inspector.bukkit

import ru.endlesscode.inspector.bukkit.report.DataType

object InspectorConfig {

    @JvmStatic
    val version: String = "0.6.0"

    internal var isEnabled: Boolean = true
    internal var sendData = mutableMapOf(
            DataType.CORE to true,
            DataType.PLUGINS to true
    )

    internal fun shouldSendData(dataType: DataType) = sendData.getValue(dataType)
}
