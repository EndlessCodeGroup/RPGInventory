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

package ru.endlesscode.rpginventory.api;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.endlesscode.rpginventory.inventory.InventoryManager;
import ru.endlesscode.rpginventory.inventory.PlayerWrapper;
import ru.endlesscode.rpginventory.inventory.slot.Slot;
import ru.endlesscode.rpginventory.inventory.slot.SlotManager;
import ru.endlesscode.rpginventory.utils.ItemUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by OsipXD on 03.09.2015
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
@SuppressWarnings({"unused"})
public class InventoryAPI {
    /**
     * Checks if opened inventory is RPGInventory.
     *
     * @param inventory - opened inventory
     * @return true - if opened RPGInventory, false - otherwise
     */
    public static boolean isRPGInventory(@NotNull Inventory inventory) {
        InventoryHolder holder = inventory.getHolder();
        return holder instanceof PlayerWrapper;
    }

    /**
     * Get all passive item from RPGInventory of specific player.
     *
     * @param player - the player
     * @return List of not null passive item
     */
    @NotNull
    public static List<ItemStack> getPassiveItems(@Nullable Player player) {
        List<ItemStack> passiveItems = new ArrayList<>();

        if (!InventoryManager.playerIsLoaded(player)) {
            return passiveItems;
        }

        Inventory inventory = InventoryManager.get(player).getInventory();
        for (Slot slot : SlotManager.instance().getPassiveSlots()) {
            for (int slotId : slot.getSlotIds()) {
                ItemStack item = inventory.getItem(slotId);
                if (ItemUtils.isNotEmpty(item) && !InventoryManager.isEmptySlot(item)) {
                    passiveItems.add(item);
                }
            }
        }

        return passiveItems;
    }

    /**
     * Get all active item from RPGInventory of specific player.
     *
     * @param player - the player
     * @return List of not null active item
     */
    @NotNull
    public static List<ItemStack> getActiveItems(@Nullable Player player) {
        List<ItemStack> activeItems = new ArrayList<>();

        if (!InventoryManager.playerIsLoaded(player)) {
            return activeItems;
        }

        Inventory inventory = InventoryManager.get(player).getInventory();
        for (Slot slot : SlotManager.instance().getActiveSlots()) {
            ItemStack item = inventory.getItem(slot.getSlotId());

            if (ItemUtils.isNotEmpty(item) && !InventoryManager.isQuickEmptySlot(item)) {
                activeItems.add(item);
            }
        }

        return activeItems;
    }
}
