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

package ru.endlesscode.rpginventory;

import org.bukkit.plugin.java.JavaPlugin;
import ru.endlesscode.rpginventory.configuration.ConfigurationProvider;

import java.io.File;

/**
 * This class is entry point to plugin
 */
public class RPGInventory extends JavaPlugin {

    private ConfigurationProvider configProvider;

    @Override
    public void onEnable() {
        //config.hocon ?
        this.configProvider = new ConfigurationProvider(new File(this.getDataFolder(), "config.json"));
    }

    @Override
    public void onDisable() {

    }
}
