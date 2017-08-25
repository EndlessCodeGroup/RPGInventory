/*
 * This file is part of RPGInventory.
 * Copyright (C) 2017 EndlessCode Group and contributors
 *
 * RPGInventory is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * RPGInventory is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with RPGInventory.  If not, see <http://www.gnu.org/licenses/>.
 */

package ru.endlesscode.rpginventory.misc

import java.io.File
import java.io.IOException
import java.io.StringReader
import java.nio.file.Files
import java.nio.file.Path
import java.text.MessageFormat
import java.util.*

abstract class I18N protected constructor(workDir: Path, langCode: String) {

    private val localeFolder: Path

    private val locale = Properties()
    private val cache = hashMapOf<String, MessageFormat>()

    protected constructor(workDir: File, langCode: String) : this(workDir.toPath(), langCode)

    init {
        try {
            this.localeFolder = Files.createDirectories(workDir.resolve("locales"))
        } catch (e: IOException) {
            throw I18NException("Failed to create locales folder", e)
        }

        load(langCode)
    }

    fun reload(langCode: String) {
        load(langCode)
        this.cache.clear()
    }

    private fun load(langCode: String) {
        val localeFile = this.prepareLocaleFile(langCode.toLowerCase())
        try {
            StringReader(FilesUtil.readFileToString(localeFile)).use { this.locale.load(it) }
        } catch (e: IOException) {
            throw I18NException("Failed to load ${localeFile.fileName}", e)
        }
    }

    private fun prepareLocaleFile(langCode: String): Path {
        val localeFile = this.localeFolder.resolve("$langCode.lang")
        if (Files.notExists(localeFile)) {
            FilesUtil.copyResourceToFile("/locales/$langCode.lang", localeFile)
        }

        return localeFile
    }

    fun getMessage(key: String, vararg args: Any) = getMessage(key, false, *args)

    @JvmOverloads
    fun getMessage(key: String, stripColor: Boolean = false, vararg args: Any = emptyArray()): String {
        val result = this.getMessageFromCache(key).format(args)
        return if (stripColor) this.stripColor(result) else result
    }

    private fun getMessageFromCache(key: String): MessageFormat {
        if (!this.cache.containsKey(key)) {
            val mf = MessageFormat(
                    this.translateCodes(this.locale.getProperty(key, key))
            )

            this.cache.put(key, mf)
        }

        return this.cache[key]!!
    }

    protected abstract fun stripColor(message: String): String

    protected abstract fun translateCodes(message: String): String

}

@Suppress("unused")
class I18NException : RuntimeException {
    internal constructor() : super()
    internal constructor(message: String) : super(message)
    internal constructor(message: String, cause: Throwable) : super(message, cause)
    internal constructor(cause: Throwable) : super(cause)
}