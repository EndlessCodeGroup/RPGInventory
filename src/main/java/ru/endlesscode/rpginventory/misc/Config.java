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

/**
 * Created by OsipXD on 22.08.2015.
 * It is part of the RpgInventory.
 * Copyright © 2015 «EndlessCode Group»
 */
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
