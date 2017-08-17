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

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Properties;

@SuppressWarnings({"unused", "WeakerAccess"})
public abstract class I18N {

    private final Path localeFolder;

    private final Properties locale = new Properties();
    private final HashMap<String, MessageFormat> cache = new HashMap<>();

    protected I18N(@NotNull File workDir, @NotNull String langCode) {
        this(workDir.toPath(), langCode);
    }

    protected I18N(@NotNull Path workDir, @NotNull String langCode) {
        try {
            this.localeFolder = Files.createDirectories(workDir.resolve("locales"));
        } catch (IOException e) {
            throw new I18NException("Failed to create locales folder", e);
        }

        load(langCode);
    }

    public void reload(@NotNull String langCode) {
        load(langCode);
        this.cache.clear();
    }

    private void load(String langCode) {
        Path localeFile = this.prepareLocaleFile(langCode.toLowerCase());
        try (StringReader sr = new StringReader(FilesUtil.readFileToString(localeFile))) {
            this.locale.load(sr);
        } catch (IOException e) {
            throw new I18NException(String.format("Failed to load %s", localeFile.getFileName()), e);
        }
    }

    private Path prepareLocaleFile(String langCode) {
        Path localeFile = this.localeFolder.resolve(langCode.concat(".lang"));
        if (Files.notExists(localeFile)) {
            FilesUtil.copyResourceToFile(String.format("/locales/%s.lang", langCode), localeFile);
        }

        return localeFile;
    }

    public String getMessage(@NotNull String key) {
        return this.getMessage(key, false);
    }

    public String getMessage(@NotNull String key, boolean stripColor) {
        return this.getMessage(key, stripColor, (Object[]) null);
    }

    public String getMessage(@NotNull String key, Object... args) {
        return this.getMessage(key, false, args);
    }

    public String getMessage(@NotNull String key, boolean stripColor, Object... args) {
        String result = this.getMessageFromCache(key).format(args);
        return stripColor ? this.stripColor(result) : result;
    }

    private MessageFormat getMessageFromCache(String key) {
        if (!this.cache.containsKey(key)) {
            MessageFormat mf = new MessageFormat(
                    this.translateCodes(this.locale.getProperty(key, key))
            );
            this.cache.put(key, mf);
        }

        return this.cache.get(key);
    }

    @NotNull
    protected abstract String stripColor(String message);

    @NotNull
    protected abstract String translateCodes(String message);

}
