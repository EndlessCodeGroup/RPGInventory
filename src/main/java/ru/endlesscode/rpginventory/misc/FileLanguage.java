/*
 * This file is part of RPGInventory.
 * Copyright (C) 2015-2017 Osip Fatkullin
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

import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileLanguage {
    @NotNull
    private final Plugin plugin;
    private final HashMap<String, MessageFormat> messageCache = new HashMap<>();
    private final Properties language = new Properties();
    private final File langFile;

    public FileLanguage(@NotNull Plugin plugin) {
        this.plugin = plugin;
        String locale = Config.getConfig().getString("language");
        this.langFile = new File(
                this.plugin.getDataFolder(),
                String.format("lang/%s.lang", locale)
        );
        this.saveDefault();
        this.checkAndUpdateLocaleFile();
        this.load();
    }

    private void saveDefault() {
        if (this.langFile.exists()) {
            return;
        }

        String path = "lang/" + this.langFile.getName();
        try {
            this.plugin.saveResource(path, true);
        } catch (Exception ex) {
            this.plugin.getLogger().log(
                    Level.WARNING, "Failed to load {0}: {1}; using en.lang",
                    new Object[]{this.langFile.getName(), ex.getLocalizedMessage()}
            );

            try (InputStream is = this.plugin.getResource("lang/en.lang")) {
                Files.copy(is, Paths.get(this.langFile.toURI()), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                this.plugin.getLogger().log(
                        Level.WARNING,
                        "Failed to write default locale to {0}: {1}; continue without localization.",
                        new Object[]{this.langFile.getName(), e.getLocalizedMessage()}
                );
            }
        }
    }

    private void load() {
        try (FileInputStream fis = new FileInputStream(this.langFile);
             InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8)) {
            this.language.load(isr);
        } catch (IOException e) {
            this.plugin.getLogger().log(
                    Level.WARNING,
                    "Failed to load locale file: {0}; continue without localization.",
                    e.getLocalizedMessage()
            );
        }
    }

    //Oh crap.
    private void checkAndUpdateLocaleFile() {
        final Path path = Paths.get(this.langFile.toURI());
        List<String> lines;
        try {
            lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            this.plugin.getLogger().log(Level.WARNING,
                    "Failed to read locale file: {0}; continue without localization.",
                    e.getLocalizedMessage()
            );
            return;
        }

        if (lines.get(0).startsWith("#version")) {
            return;
        }

        final Pattern pattern = Pattern.compile("%(s|d|.2f)");
        final LinkedList<String> newLines = new LinkedList<>();
        newLines.add("#version: 2.0 | Do not remove this line!");

        for (int i1 = 0; i1 < lines.size(); i1++) {
            String line = lines.get(i1);
            String newLine = line.replace("\"", "");
            Matcher m;
            for (int i = 0; (m = pattern.matcher(newLine)).find(); i++) {
                newLine = m.replaceFirst("{" + i + "}");
            }

            if (lines.size() > i1 + 1) {
                String nextLine = lines.get(i1 + 1);
                if ("\n ".length() < nextLine.length()
                        && (!nextLine.contains(":") || nextLine.indexOf(':') > nextLine.indexOf(' '))) {
                    newLine = newLine + "\\";
                }
            }

            newLines.add(newLine);
        }

        try {
            Files.write(
                    path, newLines, StandardCharsets.UTF_8,
                    StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING
            );
        } catch (IOException e) {
            this.plugin.getLogger().log(Level.WARNING,
                    "Failed to save locale file: {0}; continue without localization.",
                    e.getLocalizedMessage()
            );
        }
    }

    @Deprecated
    public String getCaption(String name, Object... args) {
        return this.getMessage(name, args);
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
        if (!this.messageCache.containsKey(key)) {
            this.messageCache.put(key, new MessageFormat(
                    ChatColor.translateAlternateColorCodes(
                            '&', this.language.getProperty(key, key)
                    )
            ));
        }
        String out = this.messageCache.get(key).format(args);
        return stripColor ? ChatColor.stripColor(out) : out;
    }
}
