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

package ru.endlesscode.rpginventory.event.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.*;
import ru.endlesscode.rpginventory.inventory.ActionType;
import ru.endlesscode.rpginventory.inventory.InventoryManager;
import ru.endlesscode.rpginventory.inventory.slot.Slot;
import ru.endlesscode.rpginventory.inventory.slot.SlotManager;
import ru.endlesscode.rpginventory.item.ItemManager;
import ru.endlesscode.rpginventory.utils.ItemUtils;

/**
 * Created by OsipXD on 07.04.2016
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
public class HandSwapListener implements Listener {
    @EventHandler
    public void onHandSwap(@NotNull PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();

        Slot offHandSlot = SlotManager.instance().getShieldSlot();
        Slot mainHandSlot = SlotManager.instance().getSlot(player.getInventory().getHeldItemSlot(), InventoryType.SlotType.QUICKBAR);
        ItemStack newOffHandItem = event.getOffHandItem();
        ItemStack newMainHandItem = event.getMainHandItem();

        if (offHandSlot == null && mainHandSlot == null) {
            return;
        }

        if (offHandSlot != null) {
            if (!ItemUtils.isEmpty(newOffHandItem)
                    && !InventoryManager.validateUpdate(player, ActionType.SET, offHandSlot, newOffHandItem)) {
                event.setCancelled(true);
                return;
            }
        }

        if (mainHandSlot != null) {
            if (!ItemUtils.isEmpty(newOffHandItem) && mainHandSlot.isCup(newOffHandItem)) {
                event.setCancelled(true);
                return;
            }

            if (!InventoryManager.validateUpdate(player, ActionType.SET, mainHandSlot, newMainHandItem)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void afterHandSwap(@NotNull PlayerSwapHandItemsEvent event) {
        ItemManager.updateStats(event.getPlayer());
    }
}
