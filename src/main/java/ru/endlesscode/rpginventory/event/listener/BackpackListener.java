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
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import ru.endlesscode.rpginventory.RPGInventory;
import ru.endlesscode.rpginventory.api.InventoryAPI;
import ru.endlesscode.rpginventory.inventory.ActionType;
import ru.endlesscode.rpginventory.inventory.InventoryManager;
import ru.endlesscode.rpginventory.inventory.PlayerWrapper;
import ru.endlesscode.rpginventory.inventory.backpack.Backpack;
import ru.endlesscode.rpginventory.inventory.backpack.BackpackHolder;
import ru.endlesscode.rpginventory.inventory.backpack.BackpackManager;
import ru.endlesscode.rpginventory.inventory.backpack.BackpackUpdater;
import ru.endlesscode.rpginventory.inventory.slot.SlotManager;
import ru.endlesscode.rpginventory.utils.ItemUtils;
import ru.endlesscode.rpginventory.utils.PlayerUtils;

/**
 * Created by OsipXD on 19.10.2015
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
public class BackpackListener implements Listener {
    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.LOWEST)
    public void onUseBackpack(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (!event.hasItem() || !ItemUtils.hasTag(item, ItemUtils.BACKPACK_TAG)) {
            return;
        }

        Player player = event.getPlayer();
        Action action = event.getAction();
        if ((action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK)
                && InventoryManager.isQuickSlot(player.getInventory().getHeldItemSlot())) {
            BackpackManager.open(player, item);
        }

        event.setCancelled(true);
        player.updateInventory();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onBackpackClick(final InventoryClickEvent event) {
        final Inventory inventory = event.getInventory();
        final Player player = (Player) event.getWhoClicked();

        if (!InventoryManager.playerIsLoaded(player)) {
            return;
        }

        if (inventory.getHolder() instanceof BackpackHolder) {
            // Click inside backpack
            if (BackpackManager.isBackpack(event.getCurrentItem())
                    || BackpackManager.isBackpack(event.getCursor())
                    || InventoryManager.isFilledSlot(event.getCurrentItem())
                    || InventoryManager.isFilledSlot(event.getCursor())) {
                event.setCancelled(true);
                return;
            }

            // Save changes
            if (event.getAction() == InventoryAction.NOTHING) {
                return;
            }

            BackpackUpdater.update(inventory, InventoryManager.get(player).getBackpack());
        } else if ((event.getRawSlot() >= event.getView().getTopInventory().getSize()
                || event.getSlot() == SlotManager.instance().getBackpackSlot().getSlotId()
                        && InventoryAPI.isRPGInventory(event.getInventory()))
                && !BackpackManager.playerCanTakeBackpack(player)
                && BackpackManager.isBackpack(event.getCursor())
                && ActionType.getTypeOfAction(event.getAction()) == ActionType.SET) {
            // Prevent placing new backpack in bottom inventory if player can't take backpack
            int limit = BackpackManager.getLimit();
            String message = RPGInventory.getLanguage().getMessage("backpack.limit", limit);
            PlayerUtils.sendMessage(player, message);
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBackpackClose(InventoryCloseEvent event) {
        Inventory inventory = event.getInventory();
        Player player = (Player) event.getPlayer();

        if (!InventoryManager.playerIsLoaded(player)
                || !(inventory.getHolder() instanceof BackpackHolder)) {
            return;
        }

        PlayerWrapper playerWrapper = InventoryManager.get(player);
        Backpack backpack = playerWrapper.getBackpack();

        if (backpack == null) {
            return;
        }

        backpack.onUse();
        playerWrapper.setBackpack(null);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBackpackPickup(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        if (!InventoryManager.playerIsLoaded(player)) {
            return;
        }

        if (BackpackManager.isBackpack(event.getItem().getItemStack())
                && !BackpackManager.playerCanTakeBackpack(player)) {
            int limit = BackpackManager.getLimit();
            String message = RPGInventory.getLanguage().getMessage("backpack.limit", limit);
            PlayerUtils.sendMessage(player, message);
            event.setCancelled(true);
        }
    }
}
