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

import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.spy
import com.nhaarman.mockito_kotlin.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import ru.endlesscode.rpginventory.FileTestBase
import java.io.File

class I18NTest : FileTestBase() {

    private lateinit var i18n: SimpleI18N

    @Before
    override fun setUp() {
        super.setUp()

        this.i18n = spy(SimpleI18N(tmpDir.toFile()))
    }

    @Test
    fun constructor_creatingDirectoryWitExistingFileMustThrowException() {
        try {
            SimpleI18N(testDir.toFile())
        } catch (e: I18NException) {
            assertEquals("Failed to create locales folder", e.message)
            return
        }

        fail()
    }

    @Test
    fun reload_reloadingExistingLocaleMustBeSuccessful() = i18n.reload("test")

    @Test
    fun reload_reloadingMustBeCaseInsensitive() = i18n.reload("TeSt")

    @Test
    fun getMessage_byKey() {
        assertEquals("Something value", i18n.getMessage("key"))
        verify(i18n, never()).stripColor(anyString())
    }

    @Test
    fun getMessage_byKeyWithStripColor() {
        i18n.getMessage("key", true)
        verify(i18n).stripColor(anyString())
    }

    @Test
    fun getMessage_notExistingKeyMustReturnKey() {
        val key = "not.existing.key"
        assertEquals(key, i18n.getMessage(key))
    }

    @Test
    fun getMessage_byKeyWithArgs() {
        assertEquals(
                "Args: Text, 1",
                i18n.getMessage("with.args", "Text", 1)
        )
    }

}

open class SimpleI18N internal constructor(workDir: File) : I18N(workDir, "test") {
    public override fun stripColor(message: String) = message
    public override fun translateCodes(message: String) = message
}