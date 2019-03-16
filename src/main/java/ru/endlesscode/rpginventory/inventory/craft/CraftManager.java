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
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemorySection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.endlesscode.rpginventory.RPGInventory;
import ru.endlesscode.rpginventory.event.listener.CraftListener;
import ru.endlesscode.rpginventory.item.Texture;
import ru.endlesscode.rpginventory.misc.config.Config;
import ru.endlesscode.rpginventory.utils.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by OsipXD on 29.08.2016
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
public class CraftManager {
    @NotNull
    private static List<CraftExtension> EXTENSIONS = new ArrayList<>();
    private static Texture textureOfExtendable;

    private CraftManager() {
    }

    public static boolean init(@NotNull RPGInventory instance) {
        MemorySection config = (MemorySection) Config.getConfig().get("craft");
        if (!config.getBoolean("enabled") || !config.contains("extensions")) {
            return false;
        }

        try {
            Texture texture = Texture.parseTexture(config.getString("extendable"));
            if (texture.isEmpty()) {
                Log.s("Invalid texture in ''craft.extendable''");
                return false;
            }
            textureOfExtendable = texture;

            @Nullable final ConfigurationSection extensions = config.getConfigurationSection("extensions");
            if (extensions == null) {
                Log.s("Section ''craft.extensions'' not found in config.yml");
                return false;
            }

            EXTENSIONS.clear();
            for (String extensionName : extensions.getKeys(false)) {
                EXTENSIONS.add(new CraftExtension(extensionName, extensions.getConfigurationSection(extensionName)));
            }

            // Register listeners
            ProtocolLibrary.getProtocolManager().addPacketListener(new CraftListener(instance));
            return true;
        } catch (Exception e) {
            instance.getReporter().report("Error on CraftManager initialization", e);
            return false;
        }
    }

    @NotNull
    public static List<CraftExtension> getExtensions(Player player) {
        List<CraftExtension> extensions = new ArrayList<>(EXTENSIONS);
        for (CraftExtension extension : EXTENSIONS) {
            if (extension.isUnlockedForPlayer(player)) {
                extension.registerExtension(extensions);
            }
        }

        return extensions;
    }

    public static Texture getTextureOfExtendable() {
        return textureOfExtendable;
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
