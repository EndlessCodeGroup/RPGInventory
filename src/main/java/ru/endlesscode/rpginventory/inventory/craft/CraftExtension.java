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

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;
import ru.endlesscode.rpginventory.RPGInventory;
import ru.endlesscode.rpginventory.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by OsipXD on 29.08.2016
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
public class CraftExtension {
    private final String name;
    private final ItemStack capItem;
    private final List<Integer> slots;
    @Nullable
    private final List<CraftExtension> includes;

    CraftExtension(String name, ConfigurationSection config) {
        this.name = name;
        this.capItem = CraftManager.getCapItem().clone();
        ItemMeta meta = capItem.getItemMeta();
        meta.setDisplayName(StringUtils.coloredLine(config.getString("name")));
        if (config.contains("lore")) {
            meta.setLore(StringUtils.coloredLines(config.getStringList("lore")));
        }
        this.capItem.setItemMeta(meta);
        this.slots = config.getIntegerList("slots");

        if (config.contains("includes")) {
            this.includes = new ArrayList<>();

            for (String childName : config.getStringList("includes")) {
                CraftExtension child = CraftManager.getByName(childName);
                if (child != null) {
                    includes.add(child);
                }
            }
        } else {
            this.includes = null;
        }
    }

    public ItemStack getCapItem() {
        return capItem;
    }

    public String getName() {
        return name;
    }

    public List<Integer> getSlots() {
        return slots;
    }

    boolean isUnlockedForPlayer(Player player) {
        return RPGInventory.getPermissions().has(player, "rpginventory.craft." + this.name);
    }

    void registerExtension(List<CraftExtension> extensions) {
        extensions.remove(this);

        if (this.includes == null) {
            return;
        }

        for (CraftExtension extension : this.includes) {
            extension.registerExtension(extensions);
        }
    }
}
