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

import org.apache.commons.io.FileUtils;
import org.bukkit.ChatColor;
import ru.endlesscode.rpginventory.RPGInventory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;

@SuppressWarnings({"ResultOfMethodCallIgnored", "unused"})
public class I18N {

    private final Properties locale = new Properties();
    private final HashMap<String, MessageFormat> cache = new HashMap<>();

    public I18N(RPGInventory instance) {
        this(instance, instance.getConfiguration().getLocale());
    }

    private I18N(RPGInventory instance, String locale) {
        File localeFolder = new File(instance.getDataFolder(), "locales");
        if (!localeFolder.exists()) {
            localeFolder.mkdir();
        }
        File localeFile = new File(localeFolder, locale.concat(".lang"));

        if (!localeFile.exists()) {
            //TODO: Catch in main class?
            try (InputStream is = instance.getResource(String.format("locales/%s.lang", locale))) {
                FileUtils.copyInputStreamToFile(is, localeFile);
            } catch (IOException ex) {
                instance.getLogger().log(Level.WARNING, String.format(
                        "Failed to copy %s to locales folder",
                        localeFile.getName()
                ), ex);
            }
        }

        try (StringReader sr = new StringReader(FilesUtil.readFileToString(localeFile, StandardCharsets.UTF_8))) {
            this.locale.load(sr);
        } catch (IOException e) {
            instance.getLogger().log(Level.WARNING, String.format("Failed to load %s", localeFile.getName()), e);
        }
    }

    public String getMessage(String key) {
        return this.getMessage(key, false);
    }

    public String getMessage(String key, boolean stripColor) {
        return this.getMessage(key, stripColor, (Object[]) null);
    }

    public String getMessage(String key, Object... args) {
        return this.getMessage(key, false, args);
    }

    public String getMessage(String key, boolean stripColor, Object... args) {
        if (!this.cache.containsKey(key)) {
            MessageFormat mf = new MessageFormat(
                    ChatColor.translateAlternateColorCodes('&', this.locale.getProperty(key, key))
            );
            this.cache.put(key, mf);
        }

        String result = this.cache.get(key).format(args);
        return stripColor ? ChatColor.stripColor(result) : result;
    }
}
