package ru.endlesscode.inspector.bukkit.util

import java.io.File
import java.nio.file.Files

internal object FileUtil {

    /**
     * Creates [file] if it isn't exists. Also builds path to file.
     */
    fun createFileIfNotExists(file: File) {
        if (file.exists()) return

        val path = file.toPath()
        Files.createDirectories(path.parent)
        try {
            Files.createFile(path)
        } catch (ignored: FileAlreadyExistsException) {
        }
    }

}
