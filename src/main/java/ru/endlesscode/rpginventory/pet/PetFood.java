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

package ru.endlesscode.rpginventory.pet;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import ru.endlesscode.rpginventory.RPGInventory;
import ru.endlesscode.rpginventory.item.TexturedItem;
import ru.endlesscode.rpginventory.misc.FileLanguage;
import ru.endlesscode.rpginventory.utils.ItemUtils;
import ru.endlesscode.rpginventory.utils.StringUtils;

/**
 * Created by OsipXD on 28.08.2015
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
public class PetFood extends TexturedItem {
    @NotNull
    private final String name;
    @NotNull
    private final List<String> lore;

    private final double value;
    private final List<String> eaters;

    private ItemStack foodItem;

    PetFood(ConfigurationSection config) {
        super(config.getString("item"));

        this.name = StringUtils.coloredLine(config.getString("name"));
        this.lore = StringUtils.coloredLines(config.getStringList("lore"));
        this.value = config.getDouble("value");
        this.eaters = config.getStringList("eaters");

        this.createFoodItem(config.getName());
    }

    @Contract("null -> false")
    public static boolean isFoodItem(ItemStack itemStack) {
        return !ItemUtils.isEmpty(itemStack) && ItemUtils.hasTag(itemStack, ItemUtils.FOOD_TAG);
    }

    private void createFoodItem(String id) {
        // Set texture
        ItemStack spawnItem = ItemUtils.getTexturedItem(this.texture);

        // Set lore and display itemName
        ItemMeta meta = spawnItem.getItemMeta();
        meta.setDisplayName(this.name);

        FileLanguage lang = RPGInventory.getLanguage();
        List<String> lore = new ArrayList<>(this.lore);
        lore.add(lang.getMessage("pet.food.value", (int) (this.value)));
        meta.setLore(lore);
        spawnItem.setItemMeta(meta);

        this.foodItem = ItemUtils.setTag(spawnItem, ItemUtils.FOOD_TAG, id);
    }

    public ItemStack getFoodItem() {
        return this.foodItem;
    }

    public double getValue() {
        return value;
    }

    public boolean canBeEaten(@NotNull LivingEntity pet) {
        String petType = pet.getType().toString();
        return petType != null && this.eaters.contains(petType);
    }
}
