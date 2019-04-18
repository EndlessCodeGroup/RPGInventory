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

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import ru.endlesscode.rpginventory.RPGInventory;
import ru.endlesscode.rpginventory.inventory.InventoryLocker;
import ru.endlesscode.rpginventory.inventory.InventoryManager;
import ru.endlesscode.rpginventory.utils.ItemUtils;
import ru.endlesscode.rpginventory.utils.PlayerUtils;

import java.util.List;

/**
 * Created by OsipXD on 18.09.2015
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
public class LockerListener implements Listener {
    @EventHandler
    public void onGameModeSwitch(@NotNull PlayerGameModeChangeEvent event) {
        Player player = event.getPlayer();

        if (!InventoryManager.playerIsLoaded(player)) {
            return;
        }

        if (event.getNewGameMode() == GameMode.CREATIVE) {
            InventoryLocker.unlockSlots(player);
        } else if (event.getPlayer().getGameMode() == GameMode.CREATIVE) {
            InventoryLocker.lockSlots(player, true);
        }
    }

    @EventHandler
    public void onInventoryClick(@NotNull InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        ItemStack currentItem = event.getCurrentItem();

        if (!InventoryManager.playerIsLoaded(player)) {
            return;
        }

        if (!ItemUtils.isEmpty(currentItem) && InventoryLocker.isLockedSlot(currentItem)) {
            int slot = event.getSlot();
            int line = InventoryLocker.getLine(slot);
            if (InventoryLocker.isBuyableSlot(currentItem, line)) {
                if (InventoryLocker.canBuySlot((Player) event.getWhoClicked(), line) && InventoryLocker.buySlot(player, line)) {
                    player.getInventory().setItem(slot, null);
                    event.setCurrentItem(null);

                    if (slot < 35) {
                        player.getInventory().setItem(slot + 1, InventoryLocker.getBuyableSlotForLine(InventoryLocker.getLine(slot + 1)));
                    }

                    InventoryManager.get(player).setBuyedSlots(InventoryManager.get(player).getBuyedGenericSlots() + 1);
                } else {
                    event.setCancelled(true);
                }
            } else {
                PlayerUtils.sendMessage(player, RPGInventory.getLanguage().getMessage("error.previous"));
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDeath(@NotNull PlayerDeathEvent event) {
        if (!InventoryManager.playerIsLoaded(event.getEntity())) {
            return;
        }

        List<ItemStack> drops = event.getDrops();
        for (int i = 0; i < drops.size(); i++) {
            ItemStack drop = drops.get(i);
            if (drop != null && (InventoryLocker.isLockedSlot(drop) || InventoryManager.isEmptySlot(drop))) {
                event.getDrops().set(i, new ItemStack(Material.AIR));
            }
        }
    }
}
