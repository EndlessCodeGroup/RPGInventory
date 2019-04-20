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
import org.bukkit.inventory.EntityEquipment;
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
    public static ArmorType matchType(ItemStack item) {
        if (ItemUtils.isEmpty(item)) {
            return UNKNOWN;
        }

        if (item.getType() == Material.ELYTRA) {
            return CHESTPLATE;
        }

        String[] typeParts = item.getType().name().split("_");
        String armorType = typeParts[typeParts.length - 1];

        try {
            return ArmorType.valueOf(armorType);
        } catch (IllegalArgumentException exception) {
            return UNKNOWN;
        }
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
        EntityEquipment equipment = player.getEquipment();
        if (equipment == null) {
            return null;
        }

        switch (this) {
            case HELMET:
                return equipment.getHelmet();
            case CHESTPLATE:
                return equipment.getChestplate();
            case LEGGINGS:
                return equipment.getLeggings();
            case BOOTS:
                return equipment.getBoots();
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
