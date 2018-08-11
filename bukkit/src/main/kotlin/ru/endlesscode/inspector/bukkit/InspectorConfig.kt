package ru.endlesscode.inspector.bukkit

import ru.endlesscode.inspector.bukkit.report.DataType

object InspectorConfig {

    var isEnabled: Boolean = true
    var sendData = mutableMapOf(
            DataType.CORE to true,
            DataType.PLUGINS to true
    )

    fun shouldSendData(dataType: DataType) = sendData.getValue(dataType)
}
