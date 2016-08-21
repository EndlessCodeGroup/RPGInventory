package ru.endlesscode.rpginventory.event.listener;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import ru.endlesscode.rpginventory.RPGInventory;
import ru.endlesscode.rpginventory.api.InventoryAPI;
import ru.endlesscode.rpginventory.inventory.InventoryLocker;
import ru.endlesscode.rpginventory.inventory.InventoryManager;
import ru.endlesscode.rpginventory.inventory.ResourcePackManager;
import ru.endlesscode.rpginventory.inventory.chest.ChestManager;
import ru.endlesscode.rpginventory.inventory.chest.ChestWrapper;
import ru.endlesscode.rpginventory.inventory.slot.Slot;
import ru.endlesscode.rpginventory.inventory.slot.SlotManager;
import ru.endlesscode.rpginventory.misc.Config;
import ru.endlesscode.rpginventory.utils.InventoryUtils;
import ru.endlesscode.rpginventory.utils.ItemUtils;

/**
 * Created by OsipXD on 18.09.2015
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
public class ChestListener implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryOpen(InventoryOpenEvent event) {
        Inventory inventory = event.getInventory();
        Player player = (Player) event.getPlayer();

        if (!InventoryManager.playerIsLoaded(player) || !ResourcePackManager.isLoadedResourcePack(player) || InventoryAPI.isRPGInventory(inventory)) {
            return;
        }

        if (ChestManager.chestOpened(player)) {
            ChestManager.getChest(player).keepOpen(false);
            return;
        }

        if (inventory.getType() == InventoryType.CHEST || inventory.getType() == InventoryType.ENDER_CHEST) {
            if (inventory.getViewers().size() > 1) {
                player.sendMessage(RPGInventory.getLanguage().getCaption("chest.occupied"));
                event.setCancelled(true);
                return;
            }

            ChestWrapper chestWrapper = new ChestWrapper(inventory, event.getView(), player);
            ChestManager.add(player, chestWrapper);
            player.openInventory(chestWrapper.getChestInventory());

            event.setCancelled(true);
            return;
        }

        // Block containers
        if (!ChestManager.validateContainer(inventory.getType())
                && (!RPGInventory.getPermissions().has((CommandSender) player, "rpginventory.containers")
                || !RPGInventory.getPermissions().has((CommandSender) player, "rpginventory.admin"))
                && Config.getConfig().getBoolean("containers.block")) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClose(InventoryCloseEvent event) {
        HumanEntity player = event.getPlayer();
        if (ChestManager.chestOpened(player)) {
            ChestWrapper chest = ChestManager.getChest(player);
            if (chest.isKeepOpen()) {
                return;
            }

            chest.setContents(event.getInventory().getContents());
            chest.onCloseInventory();
            ChestManager.remove(player);

            InventoryCloseEvent fakeEvent = new InventoryCloseEvent(chest.getView());
            Bukkit.getPluginManager().callEvent(fakeEvent);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        Player player = (Player) event.getWhoClicked();

        if (ChestManager.chestOpened(player)) {
            for (int slot : event.getRawSlots()) {
                if (slot == ChestManager.PREV || slot == ChestManager.NONE || slot == ChestManager.NEXT) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.LOW)
    public void onInventoryClick(final InventoryClickEvent event) {
        final Player player = (Player) event.getWhoClicked();

        if (ChestManager.chestOpened(player)) {
            ChestWrapper chest = ChestManager.getChest(player);
            if (event.getView().equals(chest.getView())) {
                return;
            }

            InventoryAction action = event.getAction();
            Inventory inventory = event.getInventory();
            final ItemStack currentItem = event.getCurrentItem();
            int rawSlot = event.getRawSlot();

            if (rawSlot == ChestManager.PREV) {
                chest.keepOpen(true);
                chest.setContents(event.getInventory().getContents());
                player.openInventory(chest.getPrevPage());
                event.setCancelled(true);
            } else if (rawSlot == ChestManager.NEXT) {
                chest.keepOpen(true);
                chest.setContents(event.getInventory().getContents());
                player.openInventory(chest.getNextPage());
                event.setCancelled(true);
            } else if (rawSlot == ChestManager.NONE || ChestManager.isCapSlot(currentItem)) {
                event.setCancelled(true);
            } else if (rawSlot < 6 && rawSlot != -999) {
                InventoryClickEvent fakeEvent = new InventoryClickEvent(chest.getView(), event.getSlotType(), chest.convertSlot(rawSlot), event.getClick(), event.getAction());
                Bukkit.getPluginManager().callEvent(fakeEvent);
                event.setCancelled(fakeEvent.isCancelled());
            }

            if (event.isCancelled()) {
                return;
            }

            if (player.getGameMode() != GameMode.CREATIVE && action == InventoryAction.MOVE_TO_OTHER_INVENTORY && !ItemUtils.isEmpty(currentItem)
                    && !InventoryManager.isQuickEmptySlot(event.getCurrentItem()) && !InventoryLocker.isLockedSlot(event.getCurrentItem())) {
                ItemStack current = null;

                if (rawSlot < inventory.getSize()) {
                    if (InventoryUtils.countEmptySlots(event.getView().getBottomInventory()) > 0) {
                        player.getInventory().addItem(currentItem);
                    } else {
                        event.setCancelled(true);
                        return;
                    }
                } else {
                    if (InventoryUtils.countEmptySlots(inventory) - 3 > 0) {
                        if (event.getSlotType() == InventoryType.SlotType.QUICKBAR) {
                            Slot slot = SlotManager.getSlotManager().getSlot(event.getSlot(), InventoryType.SlotType.QUICKBAR);
                            if (slot != null && slot.isQuick()) {
                                if (player.getInventory().getHeldItemSlot() == slot.getQuickSlot()) {
                                    InventoryUtils.heldFreeSlot(player, slot.getQuickSlot(), InventoryUtils.SearchType.NEXT);
                                }

                                current = slot.getCup();
                            }
                        }

                        inventory.addItem(currentItem);
                    } else {
                        event.setCancelled(true);
                        return;
                    }
                }

                event.setCurrentItem(current);
                player.updateInventory();
            }
        }
    }
}
