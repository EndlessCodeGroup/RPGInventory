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

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDispenseArmorEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
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

import java.util.Objects;

/**
 * Created by OsipXD on 08.04.2016
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
public class ArmorEquipListener implements Listener {
    @EventHandler(priority = EventPriority.HIGH)
    public void onQuickEquip(@NotNull PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        boolean isNotInterestAction = event.getAction() != Action.RIGHT_CLICK_AIR &&
                event.getAction() != Action.RIGHT_CLICK_BLOCK;

        if (!InventoryManager.playerIsLoaded(player) || isNotInterestAction) {
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

            boolean armorIsValid = InventoryManager.validateArmor(player, InventoryAction.PLACE_ONE, armorSlot, item);
            if (!armorIsValid) {
                event.setUseItemInHand(Event.Result.DENY);
                PlayerUtils.updateInventory(player);
            }
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

    @EventHandler
    public void onNormalEquip(@NotNull InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (!InventoryManager.playerIsLoaded(player) || InventoryAPI.isRPGInventory(event.getInventory())) {
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

            if (Config.armorSlotsAction == VanillaSlotAction.RPGINV || armorSlot == null || ItemUtils.isEmpty(item)) {
                return;
            }

            event.setCancelled(!InventoryManager.validateArmor(player, event.getAction(), armorSlot, item));
        } else if (event.getAction() == InventoryAction.HOTBAR_SWAP) {
            int hotbarSlot = event.getHotbarButton();
            if (hotbarSlot == -1) {
                handleShieldShiftClick(event, player, event.getCurrentItem());
            } else if (event.getInventory().getType() == InventoryType.CRAFTING) {
                // Prevent method when player press number
                if (ItemUtils.isNotEmpty(player.getInventory().getItem(hotbarSlot))) {
                    event.setCancelled(true);
                }
            }
        } else if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
            // Validate Shift-Click
            ItemStack item = event.getCurrentItem();
            if (item != null && item.getType() == Material.SHIELD) {
                handleShieldShiftClick(event, player, item);
            } else {
                handleArmorShiftClick(event, player, item);
            }
        }
    }

    private void handleShieldShiftClick(InventoryClickEvent event, Player player, ItemStack item) {
        Slot shieldSlot = SlotManager.instance().getShieldSlot();
        EntityEquipment equipment = Objects.requireNonNull(player.getEquipment(), "Player always have equipment.");

        if (shieldSlot != null && (ItemUtils.isEmpty(equipment.getItemInOffHand()) || !ItemUtils.isEmpty(item))) {
            event.setCancelled(!InventoryManager.validateUpdate(player, ActionType.SET, shieldSlot, item));
        }
    }

    private void handleArmorShiftClick(InventoryClickEvent event, Player player, ItemStack item) {
        ArmorType armorType = ArmorType.matchType(item);

        if (armorType == ArmorType.UNKNOWN) {
            return;
        }

        Slot armorSlot = SlotManager.instance().getSlot(armorType.name());
        if (armorSlot != null && InventoryUtils.playerNeedArmor(player, armorType)) {
            event.setCancelled(!InventoryManager.validateArmor(player, InventoryAction.PLACE_ONE, armorSlot, item));
        }
    }

    @EventHandler
    public void onDispenseEquip(BlockDispenseArmorEvent event) {
        if (event.getTargetEntity().getType() == EntityType.PLAYER) {
            ArmorType type = ArmorType.matchType(event.getItem());
            Player player = (Player) event.getTargetEntity();

            if (this.hasInventoryArmorByType(type, player)) {
                return;
            }
            if (InventoryManager.playerIsLoaded(player)) {
                Slot armorSlot = SlotManager.instance().getSlot(type.name());
                event.setCancelled(armorSlot != null
                        && !InventoryManager.validateArmor(player, InventoryAction.PLACE_ONE, armorSlot, event.getItem())
                );
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
}
