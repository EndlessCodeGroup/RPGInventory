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

import java.io.IOException
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

object FilesUtil {

    @JvmStatic
    @JvmOverloads
    fun readFileToString(file: Path, charset: Charset = StandardCharsets.UTF_8): String {
        try {
            return Files.readAllBytes(file).toString(charset)
        } catch (e: IOException) {
            throw IllegalArgumentException("Given file \"${file.toAbsolutePath()}\" can't be read", e)
        }
    }

    @JvmStatic
    fun copyResourceToFile(resource: String, file: Path) {
        val validResourcePath = if (resource.startsWith("/")) resource else "/$resource"

        try {
            javaClass.getResourceAsStream(validResourcePath).use {
                Files.copy(
                        it ?: throw IllegalArgumentException("Resource file \"$validResourcePath\" not exists"),
                        file
                )
            }
        } catch (e: IOException) {
            throw IllegalArgumentException(
                    "Failed to copy \"$validResourcePath\" to given target: \"${file.toAbsolutePath()}\"", e
            )
        }
    }

}
