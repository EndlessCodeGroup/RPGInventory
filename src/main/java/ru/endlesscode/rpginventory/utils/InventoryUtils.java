/*
 * This file is part of RPGInventory.
 * Copyright (C) 2015-2017 osipf
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

package ru.endlesscode.rpginventory.utils;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.endlesscode.rpginventory.api.InventoryAPI;
import ru.endlesscode.rpginventory.inventory.ArmorType;
import ru.endlesscode.rpginventory.inventory.InventoryManager;
import ru.endlesscode.rpginventory.inventory.slot.Slot;
import ru.endlesscode.rpginventory.item.CustomItem;
import ru.endlesscode.rpginventory.item.ItemManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InventoryUtils {
    public static void heldFreeSlot(@NotNull Player player, int start, SearchType type) {
        if (type == SearchType.NEXT) {
            for (int i = start + 1; i < start + 9; i++) {
                int index = i % 9;
                if (!InventoryManager.isQuickEmptySlot(player.getInventory().getItem(index))) {
                    player.getInventory().setHeldItemSlot(index);
                    return;
                }
            }
        } else {
            for (int i = start - 1; i > start - 9; i--) {
                int index = (i + 9) % 9;
                if (!InventoryManager.isQuickEmptySlot(player.getInventory().getItem(index))) {
                    player.getInventory().setHeldItemSlot(index);
                    return;
                }
            }
        }
    }

    @Contract(pure = true)
    public static int getQuickSlot(int slotId) {
        return slotId % 9;
    }

    public static boolean playerNeedArmor(Player player, ArmorType armorType) {
        ItemStack armorItem = armorType.getItem(player);
        return ItemUtils.isEmpty(armorItem) && armorType != ArmorType.UNKNOWN;
    }

    public static int getArmorSlotId(Slot slot) {
        switch (slot.getName()) {
            case "helmet":
                return 5;
            case "chestplate":
                return 6;
            case "leggings":
                return 7;
            default:
                return 8;
        }
    }

    @Contract(pure = true)
    public static InventoryType.SlotType getSlotType(InventoryType.SlotType slotType, int rawSlot) {
        if (rawSlot > 80) {
            return InventoryType.SlotType.QUICKBAR;
        }

        return slotType;
    }

    @NotNull
    public static List<ItemStack> collectEffectiveItems(@NotNull Player player, boolean notifyPlayer) {
        List<ItemStack> items = new ArrayList<>(InventoryAPI.getPassiveItems(player));
        Collections.addAll(items, player.getInventory().getArmorContents());

        ItemStack itemInHand = player.getEquipment().getItemInMainHand();
        if (CustomItem.isCustomItem(itemInHand) && ItemManager.allowedForPlayer(player, itemInHand, notifyPlayer)) {
            items.add(itemInHand);
        }

        itemInHand = player.getEquipment().getItemInOffHand();
        if (CustomItem.isCustomItem(itemInHand) && ItemManager.allowedForPlayer(player, itemInHand, notifyPlayer)) {
            items.add(itemInHand);
        }

        return items;
    }

    public enum SearchType {
        NEXT,
        PREV
    }
}
