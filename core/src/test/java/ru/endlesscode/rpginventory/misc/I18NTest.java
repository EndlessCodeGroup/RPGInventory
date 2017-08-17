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

package ru.endlesscode.rpginventory.misc;

import org.junit.Before;
import org.junit.Test;
import ru.endlesscode.rpginventory.FileTestBase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class I18NTest extends FileTestBase {

    private I18N i18n;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        this.i18n = spy(new SimpleI18N(tmpDir.toFile()));
    }

    @Test
    public void constructor_creatingDirectoryWitExistingFileMustThrowException() {
        try {
            new SimpleI18N(testDir.toFile());
        } catch (I18NException e) {
            assertEquals("Failed to create locales folder", e.getMessage());
            return;
        }

        fail();
    }

    @Test
    public void reload_reloadingExistingLocaleMustBeSuccessful() throws Exception {
        i18n.reload("test");
    }

    @Test
    public void reload_reloadingMustBeCaseInsensitive() throws Exception {
        i18n.reload("TeSt");
    }

    @Test
    public void getMessage_byKey() throws Exception {
        assertEquals("Something value", i18n.getMessage("key"));
        verify(i18n, never()).stripColor(anyString());
    }

    @Test
    public void getMessage_byKeyWithStripColor() throws Exception {
        i18n.getMessage("key", true);
        verify(i18n).stripColor(anyString());
    }

    @Test
    public void getMessage_notExistingKeyMustReturnKey() {
        String key = "not.existing.key";
        assertEquals(key, i18n.getMessage(key));
    }

    @Test
    public void getMessage_byKeyWithArgs() throws Exception {
        assertEquals(
                "Args: Text, 1",
                i18n.getMessage("with.args", "Text", 1)
        );
    }

}