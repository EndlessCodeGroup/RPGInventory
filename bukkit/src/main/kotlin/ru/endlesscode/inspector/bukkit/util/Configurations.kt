package ru.endlesscode.inspector.bukkit.util

import org.bukkit.configuration.file.FileConfiguration
import java.util.UUID

internal fun FileConfiguration.getBooleanOrPut(path: String, defaultValue: Boolean): Boolean {
    return when {
        this.contains(path) -> this.getBoolean(path)
        else -> defaultValue.also { this.set(path, it) }
    }
}

internal fun FileConfiguration.getUuidOrPut(path: String, defaultValue: () -> UUID): UUID {
    return when {
        this.contains(path) -> UUID.fromString(this.getString(path))
        else -> defaultValue.invoke().also { this.set(path, it.toString()) }
    }
}
