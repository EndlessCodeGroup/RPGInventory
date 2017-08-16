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

package ru.endlesscode.rpginventory.configuration.misc;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class FilesUtilTest {
    private Path testDir;
    private Path tmpDir;

    @Before
    public void setUp() throws Exception {
        this.testDir = Files.createDirectories(Paths.get("testFiles"));
        this.tmpDir = Files.createTempDirectory(testDir, "tmp");
    }

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
        try {
            this.copyResourceToFile("/resource", testDir.resolve("existingFile"));
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().startsWith("Failed to copy \"/resource\" to given target: \""));
            assertTrue(e.getCause() instanceof FileAlreadyExistsException);
        }
    }

    @Test
    public void copyResourceToFile_notExistingResourceToNewFileMustThrowException() throws Exception {
        try {
            this.copyResourceToFile("/notExistingResource", tmpDir.resolve("newFile"));
        } catch (IllegalArgumentException e) {
            assertEquals("Resource file \"/notExistingResource\" not exists", e.getMessage());
            assertNull(e.getCause());
        }
    }

    private void copyResourceToFile(String resource, Path targetFile) throws IOException {
        FilesUtil.copyResourceToFile(resource, targetFile);
        assertArrayEquals(
                new String[]{"This is a test resource file.", "Это тестовый файл ресурсов."},
                Files.readAllLines(targetFile, StandardCharsets.UTF_8).toArray()
        );
    }

    @After
    public void tearDown() throws Exception {
        Files.walk(tmpDir).forEach(path -> {
            try {
                Files.delete(path);
            } catch (IOException ignored) { }
        });
        Files.deleteIfExists(tmpDir);
    }
}