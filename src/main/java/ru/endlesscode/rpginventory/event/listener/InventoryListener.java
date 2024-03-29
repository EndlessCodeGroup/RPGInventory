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

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.endlesscode.inspector.bukkit.scheduler.TrackedBukkitRunnable;
import ru.endlesscode.rpginventory.RPGInventory;
import ru.endlesscode.rpginventory.api.InventoryAPI;
import ru.endlesscode.rpginventory.compat.SoundCompat;
import ru.endlesscode.rpginventory.compat.mypet.MyPetManager;
import ru.endlesscode.rpginventory.event.PlayerInventoryLoadEvent;
import ru.endlesscode.rpginventory.inventory.ActionType;
import ru.endlesscode.rpginventory.inventory.InventoryLocker;
import ru.endlesscode.rpginventory.inventory.InventoryManager;
import ru.endlesscode.rpginventory.inventory.PlayerWrapper;
import ru.endlesscode.rpginventory.inventory.backpack.BackpackManager;
import ru.endlesscode.rpginventory.inventory.slot.ActionSlot;
import ru.endlesscode.rpginventory.inventory.slot.Slot;
import ru.endlesscode.rpginventory.inventory.slot.SlotManager;
import ru.endlesscode.rpginventory.item.ItemManager;
import ru.endlesscode.rpginventory.misc.config.Config;
import ru.endlesscode.rpginventory.misc.config.VanillaSlotAction;
import ru.endlesscode.rpginventory.pet.PetManager;
import ru.endlesscode.rpginventory.utils.InventoryUtils;
import ru.endlesscode.rpginventory.utils.ItemUtils;
import ru.endlesscode.rpginventory.utils.PlayerUtils;

/**
 * Created by OsipXD on 18.09.2015
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
public class InventoryListener implements Listener {

    private final static int QUICKBAR_SIZE = 9;

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(@NotNull final PlayerJoinEvent event) {
        final Player player = event.getPlayer();

        if (InventoryManager.isAllowedWorld(player.getWorld())) {
            InventoryManager.initPlayer(player, false);
        }
    }

    @EventHandler
    public void onPlayerQuit(@NotNull PlayerQuitEvent event) {
        InventoryManager.unloadPlayerInventory(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLoadInventory(@NotNull PlayerInventoryLoadEvent.Post event) {
        Player player = event.getPlayer();
        ItemManager.updateStats(player);
        PlayerInventory playerInventory = player.getInventory();

        // Sync armor
        playerInventory.setArmorContents(ItemUtils.syncItems(playerInventory.getArmorContents()));

        // Sync inventory
        playerInventory.setContents(ItemUtils.syncItems(playerInventory.getContents()));

        // Sync RPG Inventory
        Inventory inventory = InventoryManager.get(player).getInventory();
        inventory.setContents(ItemUtils.syncItems(inventory.getContents()));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void prePlayerRespawn(@NotNull PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        if (InventoryManager.playerIsLoaded(player)) {
            InventoryLocker.lockSlots(player);
        }
    }

    @EventHandler
    public void onQuickSlotHeld(@NotNull PlayerItemHeldEvent event) {
        Player player = event.getPlayer();

        if (!InventoryManager.playerIsLoaded(player)) {
            return;
        }

        int slotId = event.getNewSlot();
        Slot slot = InventoryManager.getQuickSlot(slotId);
        if (slot != null && slot.isCup(player.getInventory().getItem(slotId))) {
            event.setCancelled(true);
            InventoryUtils.heldFreeSlot(player, slotId,
                    (event.getPreviousSlot() + 1) % QUICKBAR_SIZE == slotId ? InventoryUtils.SearchType.NEXT : InventoryUtils.SearchType.PREV);
        }
    }

    @EventHandler
    public void onBreakItem(@NotNull PlayerItemBreakEvent event) {
        this.onItemDisappeared(event, event.getBrokenItem());
    }

    @EventHandler
    public void onDropQuickSlot(@NotNull PlayerDropItemEvent event) {
        this.onItemDisappeared(event, event.getItemDrop().getItemStack());
    }

    private void onItemDisappeared(PlayerEvent event, @NotNull ItemStack item) {
        final Player player = event.getPlayer();
        final PlayerInventory inventory = player.getInventory();
        final int slotId = inventory.getHeldItemSlot();

        if (!InventoryManager.playerIsLoaded(player)) {
            return;
        }

        if (ItemUtils.isEmpty(inventory.getItemInMainHand()) || item.equals(inventory.getItemInMainHand())) {
            final Slot slot = InventoryManager.getQuickSlot(slotId);
            if (slot != null) {
                new TrackedBukkitRunnable() {
                    @Override
                    public void run() {
                        InventoryUtils.heldFreeSlot(player, slotId, InventoryUtils.SearchType.NEXT);
                        inventory.setItem(slotId, slot.getCup());
                    }
                }.runTaskLater(RPGInventory.getInstance(), 1);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPickupToQuickSlot(@NotNull EntityPickupItemEvent event) {
        if (event.getEntityType() != EntityType.PLAYER) {
            return;
        }

        Player player = (Player) event.getEntity();
        if (!InventoryManager.playerIsLoaded(player) || !ItemManager.allowedForPlayer(player, event.getItem().getItemStack(), false)) {
            return;
        }

        for (Slot quickSlot : SlotManager.instance().getQuickSlots()) {
            int slotId = quickSlot.getQuickSlot();
            if (quickSlot.isCup(player.getInventory().getItem(slotId)) && quickSlot.isValidItem(event.getItem().getItemStack())) {
                player.getInventory().setItem(slotId, event.getItem().getItemStack());
                event.getItem().remove();

                player.playSound(player.getLocation(), SoundCompat.ITEM_PICKUP.get(), .3f, 1.7f);
                if (Config.getConfig().getBoolean("attack.auto-held")) {
                    player.getInventory().setHeldItemSlot(quickSlot.getQuickSlot());
                }

                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryDrag(@NotNull InventoryDragEvent event) {
        Player player = (Player) event.getWhoClicked();

        if (!InventoryManager.playerIsLoaded(player)) {
            return;
        }

        for (Integer rawSlotId : event.getRawSlots()) {
            ItemStack cursor = event.getOldCursor();
            Inventory inventory = event.getInventory();

            if (PetManager.isPetItem(cursor)) {
                event.setCancelled(true);
                return;
            }

            if (inventory.getType() == InventoryType.CRAFTING) {
                if (InventoryManager.get(player).isOpened()) {
                    return;
                }

                final boolean isCraftSlot = rawSlotId >= 1 && rawSlotId <= 4;
                if (rawSlotId == Slot.SHIELD_RAW_SLOT_ID
                        || isCraftSlot && Config.craftSlotsAction == VanillaSlotAction.RPGINV) {
                    event.setCancelled(true);
                    return;
                }
            } else if (InventoryAPI.isRPGInventory(inventory)) {
                if (rawSlotId < 54) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onInventoryClick(@NotNull final InventoryClickEvent event) {
        final Player player = (Player) event.getWhoClicked();

        if (!InventoryManager.playerIsLoaded(player)) {
            return;
        }

        final InventoryType.SlotType slotType = event.getSlotType();
        if (slotType == InventoryType.SlotType.OUTSIDE) {
            return;
        }

        final InventoryAction action = event.getAction();
        if ((action == InventoryAction.HOTBAR_SWAP || action == InventoryAction.HOTBAR_MOVE_AND_READD)
                && SlotManager.instance().getSlot(event.getHotbarButton(), InventoryType.SlotType.QUICKBAR) != null) {
            event.setCancelled(true);
            return;
        }

        // Crafting inventory is Player's vanilla inventory
        final Inventory inventory = event.getInventory();
        final int rawSlot = event.getRawSlot();
        if (inventory.getType() == InventoryType.CRAFTING) {
            PlayerWrapper playerWrapper = InventoryManager.get(player);

            if (playerWrapper.isOpened()) {
                return;
            }

            boolean openRpgInventory = false;
            switch (slotType) {
                case CRAFTING:
                case RESULT:
                    openRpgInventory = Config.craftSlotsAction == VanillaSlotAction.RPGINV;
                    break;
                case QUICKBAR:
                    openRpgInventory = rawSlot == Slot.SHIELD_RAW_SLOT_ID && Config.armorSlotsAction == VanillaSlotAction.RPGINV;
                    break;
                case ARMOR:
                    openRpgInventory = Config.armorSlotsAction == VanillaSlotAction.RPGINV;
            }

            if (openRpgInventory) {
                playerWrapper.openInventoryDeferred(true);
                event.setCancelled(true);
                return;
            }
        }

        // In RPG Inventory or quick slot
        final Slot slot = SlotManager.instance().getSlot(event.getSlot(), slotType);
        final boolean isRpgInventory = InventoryAPI.isRPGInventory(inventory);
        final boolean isQuickSlot = slotType == InventoryType.SlotType.QUICKBAR && slot != null
                && (slot.isQuick() || slot.getSlotType() == Slot.SlotType.SHIELD);
        if ((isRpgInventory || isQuickSlot) && player.getGameMode() != GameMode.CREATIVE) {
            final boolean isInInventory = rawSlot < inventory.getSize();
            if (isInInventory && slot == null || action == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                event.setCancelled(true);
                return;
            }

            if (slot == null || !isQuickSlot && !isInInventory) {
                return;
            }

            PlayerWrapper playerWrapper = null;
            if (isRpgInventory) {
                playerWrapper = (PlayerWrapper) inventory.getHolder();

                // Check flying
                if (playerWrapper != null && playerWrapper.isFlying()) {
                    PlayerUtils.sendMessage(player, RPGInventory.getLanguage().getMessage("error.fall"));
                    event.setCancelled(true);
                    return;
                }
            }

            final ItemStack currentItem = event.getCurrentItem();
            final ActionType actionType = ActionType.getTypeOfAction(action);

            if (!validateClick(player, playerWrapper, slot, actionType, currentItem, slotType) || slot.getSlotType() == Slot.SlotType.INFO) {
                event.setCancelled(true);
                return;
            }

            if (slot.getSlotType() == Slot.SlotType.ACTION) {
                ((ActionSlot) slot).preformAction(player);
                event.setCancelled(true);
                return;
            }

            final ItemStack cursor = event.getCursor();
            if (playerWrapper != null && slot.getSlotType() == Slot.SlotType.ARMOR) {
                onArmorSlotClick(event, playerWrapper, slot, cursor, currentItem);
                return;
            }

            if (slot.getSlotType() == Slot.SlotType.ACTIVE || slot.getSlotType() == Slot.SlotType.PASSIVE
                    || slot.getSlotType() == Slot.SlotType.SHIELD || slot.getSlotType() == Slot.SlotType.ELYTRA) {
                event.setCancelled(!InventoryManager.validateUpdate(player, actionType, slot, cursor));

                if ((slot.getSlotType() == Slot.SlotType.SHIELD || slot.getSlotType() == Slot.SlotType.ELYTRA) &&
                        actionType == ActionType.DROP) {
                    event.setCancelled(true);
                }
            } else if (slot.getSlotType() == Slot.SlotType.PET) {
                if (RPGInventory.isMyPetHooked()) {
                    event.setCancelled(!MyPetManager.validatePet(player, action, currentItem, cursor));
                } else {
                    event.setCancelled(!InventoryManager.validatePet(player, action, currentItem, cursor));
                }
            } else if (slot.getSlotType() == Slot.SlotType.BACKPACK) {
                if (event.getClick() == ClickType.RIGHT && BackpackManager.open(player, currentItem)) {
                    event.setCancelled(true);
                } else if (actionType != ActionType.GET) {
                    event.setCancelled(!BackpackManager.isBackpack(cursor));
                }
            }

            if (!event.isCancelled()) {
                BukkitRunnable cupPlacer = new TrackedBukkitRunnable() {
                    @Override
                    public void run() {
                        ItemStack currentItem = inventory.getItem(rawSlot);
                        if (currentItem == null || currentItem.getType() == Material.AIR) {
                            inventory.setItem(rawSlot, slot.getCup());
                            player.updateInventory();
                        }
                    }
                };

                if (slot.isQuick()) {
                    InventoryManager.updateQuickSlot(player, inventory, slot, event.getSlot(), slotType,
                            action, currentItem, cursor);
                    event.setCancelled(true);
                } else if (slot.getSlotType() == Slot.SlotType.SHIELD) {
                    InventoryManager.updateShieldSlot(player, inventory, slot, event.getSlot(), slotType,
                            action, currentItem, cursor);
                    event.setCancelled(true);

                    if (isRpgInventory && (actionType == ActionType.GET || actionType == ActionType.DROP)) {
                        cupPlacer.runTaskLater(RPGInventory.getInstance(), 1);
                    }
                } else if (actionType == ActionType.GET || actionType == ActionType.DROP) {
                    cupPlacer.runTaskLater(RPGInventory.getInstance(), 1);
                } else if (slot.isCup(currentItem)) {
                    event.setCurrentItem(null);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void afterInventoryClick(@NotNull final InventoryClickEvent event) {
        if (event.getClick() == ClickType.SWAP_OFFHAND && event.isCancelled()) {
            syncOffhandSlot((Player) event.getWhoClicked());
        }
    }

    // We should manually sync offhand slot after SWAP_OFFHAND event
    // Issue: https://hub.spigotmc.org/jira/browse/SPIGOT-6145
    private void syncOffhandSlot(Player player) {
        final PlayerInventory inventory = player.getInventory();
        new TrackedBukkitRunnable() {
            @Override
            public void run() {
                inventory.setItemInOffHand(inventory.getItemInOffHand());
            }
        }.runTaskLater(RPGInventory.getInstance(), 1);
    }

    /**
     * Check
     *
     * @return Click is valid
     */
    private boolean validateClick(Player player, @Nullable PlayerWrapper playerWrapper, @NotNull Slot slot,
                                  ActionType actionType, ItemStack currentItem, InventoryType.SlotType slotType) {
        if (playerWrapper != null) {
            if (player != playerWrapper.getPlayer()) {
                return false;
            }

            if (!PlayerUtils.checkLevel(player, slot.getRequiredLevel())) {
                PlayerUtils.sendMessage(player, RPGInventory.getLanguage().getMessage("error.level", slot.getRequiredLevel()));
                return false;
            }

            if (!slot.isFree() && !playerWrapper.isBuyedSlot(slot.getName()) && !InventoryManager.buySlot(player, playerWrapper, slot)) {
                return false;
            }
        }

        return !((actionType == ActionType.GET && slot.getSlotType() != Slot.SlotType.ACTION
                || actionType == ActionType.DROP) && slot.isCup(currentItem) && slotType != InventoryType.SlotType.QUICKBAR);
    }

    private void onArmorSlotClick(InventoryClickEvent event, PlayerWrapper playerWrapper, @NotNull final Slot slot,
                                  @NotNull ItemStack cursor, ItemStack currentItem) {
        final Player player = playerWrapper.getPlayer().getPlayer();

        final Inventory inventory = event.getInventory();
        final int rawSlot = event.getRawSlot();
        InventoryAction action = event.getAction();
        ActionType actionType = ActionType.getTypeOfAction(action);

        if (InventoryManager.validateArmor(player, action, slot, cursor) && playerWrapper.getInventoryView() != null) {
            // Event of equip armor
            InventoryClickEvent fakeEvent = new InventoryClickEvent(playerWrapper.getInventoryView(),
                    InventoryType.SlotType.ARMOR, InventoryUtils.getArmorSlotId(slot), event.getClick(), action);
            Bukkit.getPluginManager().callEvent(fakeEvent);

            if (fakeEvent.isCancelled()) {
                event.setCancelled(true);
                return;
            }

            InventoryManager.updateArmor(player, inventory, slot, rawSlot, action, currentItem, cursor);

            if (actionType == ActionType.GET) {
                inventory.setItem(rawSlot, slot.getCup());
            } else if (slot.isCup(currentItem)) {
                player.setItemOnCursor(new ItemStack(Material.AIR));
            }

            player.updateInventory();
        }

        if (actionType == ActionType.DROP) {
            new TrackedBukkitRunnable() {
                @Override
                public void run() {
                    inventory.setItem(rawSlot, slot.getCup());
                    player.updateInventory();
                }
            }.runTaskLater(RPGInventory.getInstance(), 1);
        } else {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onInventoryOpen(@NotNull InventoryOpenEvent event) {
        Inventory inventory = event.getInventory();
        HumanEntity player = event.getPlayer();

        if (!InventoryManager.playerIsLoaded(player)) {
            return;
        }

        if (InventoryAPI.isRPGInventory(inventory)) {
            PlayerWrapper playerWrapper = (PlayerWrapper) inventory.getHolder();
            InventoryManager.syncQuickSlots(playerWrapper);
            InventoryManager.syncInfoSlots(playerWrapper);
            InventoryManager.syncShieldSlot(playerWrapper);
            InventoryManager.syncArmor(playerWrapper);
        }
    }

    @EventHandler
    public void onInventoryClose(@NotNull InventoryCloseEvent event) {
        if (InventoryAPI.isRPGInventory(event.getInventory())) {
            PlayerWrapper playerWrapper = (PlayerWrapper) event.getInventory().getHolder();
            if (playerWrapper == null || event.getPlayer() != playerWrapper.getPlayer()) {
                return;
            }

            playerWrapper.onClose();
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onWorldChanged(@NotNull PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        if (!InventoryManager.playerIsLoaded(player)) {
            if (InventoryManager.isAllowedWorld(player.getWorld())) {
                InventoryManager.initPlayer(player, InventoryManager.isAllowedWorld(event.getFrom()));
            }

            return;
        }

        if (!InventoryManager.isAllowedWorld(player.getWorld())) {
            InventoryManager.unloadPlayerInventory(player);
        } else if (InventoryManager.get(player).hasPet()) {
            PetManager.respawnPet(player);
        }
    }
}
