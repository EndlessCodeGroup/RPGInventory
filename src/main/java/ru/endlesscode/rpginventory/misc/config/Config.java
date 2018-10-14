/*
 * This file is part of RPGInventory.
 * Copyright (C) 2018 EndlessCode Group and contributors
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

package ru.endlesscode.rpginventory.misc.config;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import ru.endlesscode.rpginventory.RPGInventory;
import ru.endlesscode.rpginventory.utils.Log;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class Config {

    /* Config options */
    public static VanillaSlotAction craftSlotsAction = VanillaSlotAction.RPGINV;
    public static VanillaSlotAction armorSlotsAction = VanillaSlotAction.DEFAULT;

    private static FileConfiguration config = new YamlConfiguration();
    private static Path configFile;

    public static void init(RPGInventory plugin) {
        configFile = plugin.getDataPath().resolve("config.yml");
        plugin.saveDefaultConfig();

        final InputStream defaultConfigStream = plugin.getResource("config.yml");
        if (defaultConfigStream != null) {
            loadDefaultConfig(defaultConfigStream);
        }

        reload();
    }

    public static FileConfiguration getConfig() {
        return config;
    }

    public static void reload() {
        try {
            config.load(configFile.toFile());
        } catch (IOException | InvalidConfigurationException e) {
            Log.w(e, "Error on load config.yml");
        }

        copyOptionsFromConfig();
        save();
    }

    public static void save() {
        try {
            config.save(configFile.toFile());
        } catch (IOException e) {
            Log.w(e, "Error on save config.yml");
        }
    }

    private static void loadDefaultConfig(@NotNull InputStream defaultConfigStream) {
        final Path exampleConfigPath = configFile.getParent().resolve("config-example.yml");

        // Update example config
        try (final InputStream stream = defaultConfigStream) {
            Files.copy(stream, exampleConfigPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            Log.w(e, "Error on copying config-example.yml");
        }

        config.setDefaults(YamlConfiguration.loadConfiguration(exampleConfigPath.toFile()));
        config.options().copyDefaults(true);
    }

    private static void copyOptionsFromConfig() {
        craftSlotsAction = VanillaSlotAction.parseString(config.getString("craft-slots-action"));
        armorSlotsAction = VanillaSlotAction.parseString(config.getString("armor-slots-action"));
    }
}
