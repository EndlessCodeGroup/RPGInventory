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

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import ru.endlesscode.rpginventory.RPGInventory;
import ru.endlesscode.rpginventory.api.InventoryAPI;
import ru.endlesscode.rpginventory.inventory.ActionType;
import ru.endlesscode.rpginventory.inventory.ArmorType;
import ru.endlesscode.rpginventory.inventory.InventoryManager;
import ru.endlesscode.rpginventory.inventory.slot.Slot;
import ru.endlesscode.rpginventory.inventory.slot.SlotManager;
import ru.endlesscode.rpginventory.misc.config.Config;
import ru.endlesscode.rpginventory.misc.config.VanillaSlotAction;
import ru.endlesscode.rpginventory.utils.InventoryUtils;
import ru.endlesscode.rpginventory.utils.ItemUtils;
import ru.endlesscode.rpginventory.utils.PlayerUtils;

import java.util.Collection;

/**
 * Created by OsipXD on 08.04.2016
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
public class ArmorEquipListener implements Listener {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onQuickEquip(@NotNull PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        if (!InventoryManager.playerIsLoaded(player) || event.getAction() != Action.RIGHT_CLICK_AIR &&
                event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_BLOCK &&
                !event.getClickedBlock().getState().getClass().getSimpleName().contains("BlockState")) {
            return;
        }

        ItemStack item = event.getItem();
        if (ItemUtils.isEmpty(item)) {
            return;
        }

        ArmorType armorType = ArmorType.matchType(item);
        if (InventoryUtils.playerNeedArmor(player, armorType)) {
            Slot armorSlot = SlotManager.instance().getSlot(armorType.name());
            if (armorSlot == null) {
                return;
            }

            event.setCancelled(!InventoryManager.validateArmor(player, InventoryAction.PLACE_ONE, armorSlot, item));

            PlayerUtils.updateInventory(player);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDragEquip(@NotNull InventoryDragEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (!InventoryManager.playerIsLoaded(player) || InventoryAPI.isRPGInventory(event.getInventory())) {
            return;
        }

        if (event.getRawSlots().size() == 1) {
            int slot = (int) event.getRawSlots().toArray()[0];

            event.setCancelled(slot >= 5 && slot <= 8 && event.getInventory().getType() == InventoryType.CRAFTING);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onNormalEquip(@NotNull InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (!InventoryManager.playerIsLoaded(player) || InventoryAPI.isRPGInventory(event.getInventory())
                || Config.armorSlotsAction == VanillaSlotAction.RPGINV) {
            return;
        }

        ActionType actionType = ActionType.getTypeOfAction(event.getAction());
        if (actionType == ActionType.SET && event.getSlotType() != InventoryType.SlotType.ARMOR) {
            return;
        }

        if (InventoryManager.get(player).isFlying()) {
            PlayerUtils.sendMessage(player, RPGInventory.getLanguage().getMessage("error.fall"));
            event.setCancelled(true);
            return;
        }

        if (actionType == ActionType.SET) {
            // Validate classic method
            ItemStack item = event.getCursor();
            Slot armorSlot = ArmorType.getArmorSlotById(event.getRawSlot());

            if (armorSlot == null || ItemUtils.isEmpty(item)) {
                return;
            }

            event.setCancelled(!InventoryManager.validateArmor(player, event.getAction(), armorSlot, item));
        } else if (event.getAction() == InventoryAction.HOTBAR_SWAP) {
            // Prevent method when player press number
            if (event.getInventory().getType() == InventoryType.CRAFTING) {
                ItemStack hotbarItem = player.getInventory().getItem(event.getHotbarButton());
                if (!ItemUtils.isEmpty(hotbarItem)) {
                    event.setCancelled(true);
                }
            }
        } else if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
            // Validate Shift-Click
            ItemStack item = event.getCurrentItem();
            ArmorType armorType = ArmorType.matchType(item);

            if (armorType == ArmorType.UNKNOWN) {
                return;
            }

            Slot armorSlot = SlotManager.instance().getSlot(armorType.name());
            if (armorSlot != null && InventoryUtils.playerNeedArmor(player, armorType)) {
                event.setCancelled(!InventoryManager.validateArmor(player, InventoryAction.PLACE_ONE, armorSlot, item));
            }
        }
    }

    @EventHandler
    public void onDispenseEquip(BlockDispenseEvent event) {
        ArmorType type = ArmorType.matchType(event.getItem());
        Location blockLoc = event.getBlock().getLocation();
        Collection<Entity> nearbyEntities = blockLoc.getWorld().getNearbyEntities(blockLoc, 3D, 1.2D, 3D);
        if (nearbyEntities.isEmpty()) {
            return;
        }

        for (Entity entity : nearbyEntities) {
            if (entity.getType() != EntityType.PLAYER) {
                continue;
            }

            Player player = (Player) entity;
            if (!this.isPlayerInRightPosition(event.getBlock(), player)) {
                continue;
            }
            if (this.hasInventoryArmorByType(type, player)) {
                continue;
            }
            if (InventoryManager.playerIsLoaded(player)) {
                Slot armorSlot = SlotManager.instance().getSlot(type.name());
                event.setCancelled(armorSlot != null
                        && !InventoryManager.validateArmor(player, InventoryAction.PLACE_ONE, armorSlot, event.getItem())
                );
                return;
            }
        }
    }

    //Read helpers \:D/
    private boolean hasInventoryArmorByType(ArmorType type, Player player) {
        switch (type) {
            case HELMET:
                return player.getInventory().getHelmet() != null;
            case CHESTPLATE:
                return player.getInventory().getChestplate() != null;
            case LEGGINGS:
                return player.getInventory().getLeggings() != null;
            case BOOTS:
                return player.getInventory().getBoots() != null;
            case UNKNOWN:
            default:
                return true; //Why no?
        }
    }

    private boolean isPlayerInRightPosition(Block block, Player player) {
        if (!(block.getState() instanceof org.bukkit.block.Dispenser)) {
            return false;
        }
        final Location blockLoc = block.getLocation();
        final Location playerLoc = player.getLocation();
        org.bukkit.block.Dispenser dispenser = (org.bukkit.block.Dispenser) block.getState();
        org.bukkit.material.Dispenser dispenserData = (org.bukkit.material.Dispenser) dispenser.getData();
        /*
            From old 'if' statement
         // Someone told me not to do big if checks because it's hard to read, look at me doing it -_-

            directionFacing == BlockFace.EAST && playerLoc.getBlockX() != blockLoc.getBlockX() && playerLoc.getX() <= blockLoc.getX() + 2.3 && playerLoc.getX() >= blockLoc.getX()
         || directionFacing == BlockFace.WEST && playerLoc.getX() >= blockLoc.getX() - 1.3 && playerLoc.getX() <= blockLoc.getX()
         || directionFacing == BlockFace.SOUTH && playerLoc.getBlockZ() != blockLoc.getBlockZ() && playerLoc.getZ() <= blockLoc.getZ() + 2.3 && playerLoc.getZ() >= lockLoc.getZ()
         || directionFacing == BlockFace.NORTH && playerLoc.getZ() >= blockLoc.getZ() - 1.3 && playerLoc.getZ() <= blockLoc.getZ()+
         */
        switch (dispenserData.getFacing()) {
            case EAST:
                return playerLoc.getBlockX() != blockLoc.getBlockX() && playerLoc.getX() <= blockLoc.getX() + 2.3 && playerLoc.getX() >= blockLoc.getX();
            case WEST:
                return playerLoc.getX() >= blockLoc.getX() - 1.3 && playerLoc.getX() <= blockLoc.getX();
            case SOUTH:
                return playerLoc.getBlockZ() != blockLoc.getBlockZ() && playerLoc.getZ() <= blockLoc.getZ() + 2.3 && playerLoc.getZ() >= blockLoc.getZ();
            case NORTH:
                return playerLoc.getZ() >= blockLoc.getZ() - 1.3 && playerLoc.getZ() <= blockLoc.getZ();
            default:
                return false;

        }
    }
}
