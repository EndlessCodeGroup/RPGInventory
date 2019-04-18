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

package ru.endlesscode.rpginventory.inventory;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.endlesscode.rpginventory.inventory.slot.Slot;
import ru.endlesscode.rpginventory.inventory.slot.SlotManager;
import ru.endlesscode.rpginventory.utils.ItemUtils;

/**
 * Created by OsipXD on 08.04.2016
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
public enum ArmorType {
    HELMET,
    CHESTPLATE,
    LEGGINGS,
    BOOTS,
    UNKNOWN;

    @NotNull
    public static ArmorType matchType(@NotNull ItemStack item) {
        if (ItemUtils.isEmpty(item)) {
            return UNKNOWN;
        }

        if (item.getType() == Material.ELYTRA) {
            return CHESTPLATE;
        }

        switch (item.getType()) {
            case LEATHER_HELMET:
            case CHAINMAIL_HELMET:
            case IRON_HELMET:
            case GOLD_HELMET:
            case DIAMOND_HELMET:
                return HELMET;
            case LEATHER_CHESTPLATE:
            case CHAINMAIL_CHESTPLATE:
            case IRON_CHESTPLATE:
            case GOLD_CHESTPLATE:
            case DIAMOND_CHESTPLATE:
                return CHESTPLATE;
            case LEATHER_LEGGINGS:
            case CHAINMAIL_LEGGINGS:
            case IRON_LEGGINGS:
            case GOLD_LEGGINGS:
            case DIAMOND_LEGGINGS:
                return LEGGINGS;
            case LEATHER_BOOTS:
            case CHAINMAIL_BOOTS:
            case IRON_BOOTS:
            case GOLD_BOOTS:
            case DIAMOND_BOOTS:
                return BOOTS;
        }

        return UNKNOWN;
    }

    @Nullable
    public static Slot getArmorSlotById(int id) {
        switch (id) {
            case 5:
                return SlotManager.instance().getSlot("helmet");
            case 6:
                return SlotManager.instance().getSlot("chestplate");
            case 7:
                return SlotManager.instance().getSlot("leggings");
            case 8:
                return SlotManager.instance().getSlot("boots");
            default:
                return null;
        }
    }

    @Nullable
    public ItemStack getItem(@NotNull Player player) {
        switch (this) {
            case HELMET:
                return player.getEquipment().getHelmet();
            case CHESTPLATE:
                return player.getEquipment().getChestplate();
            case LEGGINGS:
                return player.getEquipment().getLeggings();
            case BOOTS:
                return player.getEquipment().getBoots();
            default:
                return null;
        }
    }

    public int getSlot() {
        Slot temp = null;
        switch (this) {
            case HELMET:
                temp = SlotManager.instance().getSlot("helmet");
                break;
            case CHESTPLATE:
                temp = SlotManager.instance().getSlot("chestplate");
                break;
            case LEGGINGS:
                temp = SlotManager.instance().getSlot("leggings");
                break;
            case BOOTS:
                temp = SlotManager.instance().getSlot("boots");
        }

        return temp == null ? -1 : temp.getSlotId();
    }
}
