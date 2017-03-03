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

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import ru.endlesscode.rpginventory.RPGInventory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class Config {
    private static FileConfiguration config;
    private static File configFile;

    public static void loadConfig(@NotNull Plugin plugin) {
        configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            plugin.saveDefaultConfig();

            try {
                Files.copy(configFile.toPath(), new File(plugin.getDataFolder(), "config-example.yml").toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        plugin.reloadConfig();
        Config.config = plugin.getConfig();
    }

    public static FileConfiguration getConfig() {
        return Config.config;
    }

    public static void reload() {
        Config.config = YamlConfiguration.loadConfiguration(configFile);
    }

    public static void save() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            RPGInventory.getPluginLogger().warning(e.getMessage());
        }
    }
}
