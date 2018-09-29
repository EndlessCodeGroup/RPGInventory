package ru.endlesscode.inspector.bukkit.util

import java.io.File
import java.nio.file.Files

internal fun File.buildPathToFile() {
    Files.createDirectories(this.parentFile.toPath())
}
