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

import org.hamcrest.CoreMatchers.instanceOf
import org.junit.Assert.*
import org.junit.Test
import ru.endlesscode.rpginventory.FileTestBase
import java.nio.charset.StandardCharsets
import java.nio.file.FileAlreadyExistsException
import java.nio.file.Files
import java.nio.file.NoSuchFileException
import java.nio.file.Path

class FilesUtilTest : FileTestBase() {

    @Test
    fun copyResourceToFile_existingResourceToNewFileMustBeSuccessful() {
        this.copyResourceToFile("/resource", tmpDir.resolve("resource"))
    }

    @Test
    fun copyResourceToFile_resourceWithoutStartingSlashMustBeSuccessful() {
        this.copyResourceToFile("resource", tmpDir.resolve("resource"))
    }

    @Test
    fun copyResourceToFile_existingResourceToExistingFileMustThrowException() {
        val target = testDir.resolve("existingFile")
        try {
            this.copyResourceToFile("/resource", target)
        } catch (e: IllegalArgumentException) {
            assertEquals("Failed to copy \"/resource\" to given target: \"${target.toAbsolutePath()}\"", e.message)
            assertThat<Throwable>(e.cause, instanceOf(FileAlreadyExistsException::class.java))
            return
        }

        fail()
    }

    @Test
    fun copyResourceToFile_notExistingResourceToNewFileMustThrowException() {
        try {
            this.copyResourceToFile("/notExistingResource", tmpDir.resolve("newFile"))
        } catch (e: IllegalArgumentException) {
            assertEquals("Resource file \"/notExistingResource\" not exists", e.message)
            assertNull(e.cause)
            return
        }

        fail()
    }

    private fun copyResourceToFile(resource: String, targetFile: Path) {
        FilesUtil.copyResourceToFile(resource, targetFile)
        assertArrayEquals(
                arrayOf("This is a test resource file.", "Это тестовый файл ресурсов."),
                Files.readAllLines(targetFile, StandardCharsets.UTF_8).toTypedArray()
        )
    }

    @Test
    fun readFileToString_existingFileMustBeSuccessful() {
        val target = testDir.resolve("existingFile")
        val expected = "Multi-line\nexisting\nfile.\nС русским\nтекстом."

        assertEquals(expected, FilesUtil.readFileToString(target))
    }

    @Test
    fun readFileToString_notExistingFileMustThrowException() {
        val target = testDir.resolve("notExistingFile")
        try {
            FilesUtil.readFileToString(target)
        } catch (e: IllegalArgumentException) {
            assertEquals("Given file \"${target.toAbsolutePath()}\" can't be read", e.message)
            assertThat(e.cause, instanceOf(NoSuchFileException::class.java))
            return
        }

        fail()
    }
}