package ru.endlesscode.inspector.bukkit

import ru.endlesscode.inspector.bukkit.report.DataType

class InspectorConfig {

    internal var isEnabled: Boolean = true
    internal var sendData = mutableMapOf(
            DataType.CORE to true,
            DataType.PLUGINS to true
    )

    fun shouldSendData(dataType: DataType) = sendData.getValue(dataType)
}
