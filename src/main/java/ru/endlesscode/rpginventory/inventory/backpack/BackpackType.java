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

package ru.endlesscode.rpginventory.inventory.backpack;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import ru.endlesscode.rpginventory.RPGInventory;
import ru.endlesscode.rpginventory.item.Texture;
import ru.endlesscode.rpginventory.item.TexturedItem;
import ru.endlesscode.rpginventory.misc.FileLanguage;
import ru.endlesscode.rpginventory.utils.ItemUtils;
import ru.endlesscode.rpginventory.utils.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Created by OsipXD on 05.10.2015
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
public class BackpackType extends TexturedItem {
    private final String id;
    @NotNull
    private final String name;
    @NotNull
    private final List<String> lore;
    private final int size;

    private ItemStack item;

    BackpackType(Texture texture, ConfigurationSection config) {
        super(texture);

        this.id = config.getName();
        this.name = StringUtils.coloredLine(config.getString("name", id));
        this.lore = StringUtils.coloredLines(config.getStringList("lore"));
        this.size = config.getInt("size", 56) < 56 ? config.getInt("size") : 56;

        this.createItem();
    }

    private void createItem() {
        ItemStack spawnItem = this.texture.getItemStack();

        ItemMeta meta = spawnItem.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(this.name);

            FileLanguage lang = RPGInventory.getLanguage();
            List<String> lore = new ArrayList<>();
            lore.addAll(Arrays.asList(lang.getMessage("backpack.desc").split("\n")));
            lore.addAll(this.lore);
            lore.add(lang.getMessage("backpack.size", this.size));

            meta.setLore(lore);
            spawnItem.setItemMeta(meta);
        }

        this.item = ItemUtils.setTag(spawnItem, ItemUtils.BACKPACK_TAG, this.id);
    }

    @NotNull Backpack createBackpack(UUID uuid) {
        return new Backpack(this, uuid);
    }

    @NotNull Backpack createBackpack() {
        return new Backpack(this);
    }

    public int getSize() {
        return this.size;
    }

    @NotNull String getTitle() {
        return this.name;
    }

    public ItemStack getItem() {
        return this.item;
    }

    public String getId() {
        return this.id;
    }
}
