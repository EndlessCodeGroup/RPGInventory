package ru.endlesscode.inspector.bukkit

object InspectorConfig {

    // Constants

    const val CORE = "core"
    const val PLUGINS = "plugins"


    var isEnabled: Boolean = true
    var sendData = mutableMapOf(
            "core" to true,
            "plugins" to true
    )
}
