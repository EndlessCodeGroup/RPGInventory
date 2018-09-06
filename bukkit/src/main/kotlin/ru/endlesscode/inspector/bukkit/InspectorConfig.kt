package ru.endlesscode.inspector.bukkit

import ru.endlesscode.inspector.bukkit.report.DataType

class InspectorConfig(
        val version: String
) {

    internal var isEnabled: Boolean = true
    internal var sendData = mutableMapOf(
            DataType.CORE to true,
            DataType.PLUGINS to true
    )
    internal var isEventsLoggerEnabled: Boolean = false
    internal var isPacketsLoggerEnabled: Boolean = false

    fun shouldSendData(dataType: DataType) = sendData.getValue(dataType)
}
