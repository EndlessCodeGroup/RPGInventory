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

import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import ru.endlesscode.rpginventory.FileTestBase;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.*;

public class FilesUtilTest extends FileTestBase {

    @Test
    public void copyResourceToFile_existingResourceToNewFileMustBeSuccessful() throws Exception {
        this.copyResourceToFile("/resource", tmpDir.resolve("resource"));
    }

    @Test
    public void copyResourceToFile_resourceWithoutStartingSlashMustBeSuccessful() throws Exception {
        this.copyResourceToFile("resource", tmpDir.resolve("resource"));
    }

    @Test
    public void copyResourceToFile_existingResourceToExistingFileMustThrowException() throws Exception {
        Path target = testDir.resolve("existingFile");
        try {
            this.copyResourceToFile("/resource", target);
        } catch (IllegalArgumentException e) {
            String expectedMessage = String.format(
                    "Failed to copy \"/resource\" to given target: \"%s\"",
                    target.toAbsolutePath().toString()
            );
            assertEquals(expectedMessage, e.getMessage());
            assertThat(e.getCause(), instanceOf(FileAlreadyExistsException.class));
            return;
        }

        fail();
    }

    @Test
    public void copyResourceToFile_notExistingResourceToNewFileMustThrowException() throws Exception {
        try {
            this.copyResourceToFile("/notExistingResource", tmpDir.resolve("newFile"));
        } catch (IllegalArgumentException e) {
            assertEquals("Resource file \"/notExistingResource\" not exists", e.getMessage());
            assertNull(e.getCause());
            return;
        }

        fail();
    }

    private void copyResourceToFile(@NotNull String resource, @NotNull Path targetFile) throws IOException {
        FilesUtil.copyResourceToFile(resource, targetFile);
        assertArrayEquals(
                new String[]{"This is a test resource file.", "Это тестовый файл ресурсов."},
                Files.readAllLines(targetFile, StandardCharsets.UTF_8).toArray()
        );
    }

    @Test
    public void readFileToString_existingFileMustBeSuccessful() {
        Path target = testDir.resolve("existingFile");
        String expected = "Multi-line\nexisting\nfile.\nС русским\nтекстом.";

        assertEquals(expected, FilesUtil.readFileToString(target));
    }

    @Test
    public void readFileToString_notExistingFileMustThrowException() {
        Path target = testDir.resolve("notExistingFile");
        try {
            FilesUtil.readFileToString(target);
        } catch (IllegalArgumentException e) {
            String expectedMessage = String.format(
                    "Given file \"%s\" can't be read",
                    target.toAbsolutePath().toString()
            );
            assertEquals(expectedMessage, e.getMessage());
            assertThat(e.getCause(), instanceOf(NoSuchFileException.class));
            return;
        }

        fail();
    }
}