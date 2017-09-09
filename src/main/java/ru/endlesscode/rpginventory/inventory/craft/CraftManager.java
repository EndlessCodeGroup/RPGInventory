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

package ru.endlesscode.rpginventory.inventory.craft;

import com.comphenix.protocol.ProtocolLibrary;
import org.bukkit.configuration.MemorySection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import ru.endlesscode.rpginventory.RPGInventory;
import ru.endlesscode.rpginventory.event.listener.CraftListener;
import ru.endlesscode.rpginventory.misc.Config;
import ru.endlesscode.rpginventory.utils.ItemUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by OsipXD on 29.08.2016
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
public class CraftManager {
    private static List<CraftExtension> EXTENSIONS = new ArrayList<>();
    private static ItemStack capItem;

    private CraftManager() {
    }

    public static boolean init(RPGInventory instance) {
        MemorySection config = (MemorySection) Config.getConfig().get("craft");
        if (!config.getBoolean("enabled") || !config.contains("extensions")) {
            return false;
        }

        try {
            capItem = ItemUtils.getTexturedItem(config.getString("extendable"));

            Set<String> extensionNames = config.getConfigurationSection("extensions").getKeys(false);
            for (String extensionName : extensionNames) {
                EXTENSIONS.add(new CraftExtension(extensionName, config.getConfigurationSection("extensions." + extensionName)));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        // Register listeners
        ProtocolLibrary.getProtocolManager().addPacketListener(new CraftListener(instance));
        return true;
    }

    public static List<CraftExtension> getExtensions(Player player) {
        List<CraftExtension> extensions = new ArrayList<>(EXTENSIONS);
        for (CraftExtension extension : EXTENSIONS) {
            if (extension.isUnlockedForPlayer(player)) {
                extension.registerExtension(extensions);
            }
        }

        return extensions;
    }

    static ItemStack getCapItem() {
        return capItem;
    }

    @Nullable
    static CraftExtension getByName(String childName) {
        for (CraftExtension extension : EXTENSIONS) {
            if (extension.getName().equals(childName)) {
                return extension;
            }
        }

        return null;
    }
}