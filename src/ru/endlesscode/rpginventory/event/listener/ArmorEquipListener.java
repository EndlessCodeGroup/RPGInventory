package ru.endlesscode.rpginventory.event.listener;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import ru.endlesscode.rpginventory.RPGInventory;
import ru.endlesscode.rpginventory.api.InventoryAPI;
import ru.endlesscode.rpginventory.inventory.InventoryManager;
import ru.endlesscode.rpginventory.inventory.slot.ArmorType;
import ru.endlesscode.rpginventory.inventory.slot.Slot;
import ru.endlesscode.rpginventory.inventory.slot.SlotManager;
import ru.endlesscode.rpginventory.utils.InventoryUtils;

/**
 * Created by OsipXD on 08.04.2016
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
public class ArmorEquipListener implements Listener {
    @EventHandler
    public void onQuickEquip(PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        if (!InventoryManager.playerIsLoaded(player)) {
            return;
        }

        ItemStack item = event.getItem();
        if (item == null || item.getType() == Material.AIR) {
            return;
        }

        ArmorType armorType = ArmorType.matchType(item);
        if (armorType != ArmorType.UNKNOWN
                && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
            Slot armorSlot = SlotManager.getSlotManager().getSlot(armorType.name());
            if (armorSlot == null) {
                return;
            }

            event.setCancelled(!InventoryManager.validateArmor(InventoryAction.PLACE_ONE, armorSlot, item));
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                //noinspection deprecation
                player.updateInventory();
            }
        }.runTaskLater(RPGInventory.getInstance(), 1);
    }

    @EventHandler
    public void onDragEquip(InventoryDragEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (!InventoryManager.playerIsLoaded(player) || InventoryAPI.isRPGInventory(event.getInventory())) {
            return;
        }

        if (event.getRawSlots().size() == 1) {
            int rawSlot = (int) event.getRawSlots().toArray()[0];
            event.setCancelled(event.getInventory().getType() == InventoryType.CRAFTING && rawSlot >= 5 && rawSlot <= 8);
        }
    }

    @EventHandler
    public void onNormalEquip(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (!InventoryManager.playerIsLoaded(player) || InventoryAPI.isRPGInventory(event.getInventory())) {
            return;
        }

        InventoryUtils.ActionType actionType = InventoryUtils.getTypeOfAction(event.getAction());
        if (actionType == InventoryUtils.ActionType.SET && event.getSlotType() != InventoryType.SlotType.ARMOR) {
            return;
        }

        // Validate classic method
        if (actionType == InventoryUtils.ActionType.SET) {
            ItemStack item = event.getCursor();
            Slot armorSlot = InventoryUtils.getArmorSlotById(event.getRawSlot());
            if (item == null || item.getType() == Material.AIR || armorSlot == null) {
                return;
            }
            event.setCancelled(!InventoryManager.validateArmor(event.getAction(), armorSlot, item));
            return;
        }

        // Validate Shift-Click
        ItemStack item = event.getCurrentItem();
        ArmorType armorType = ArmorType.matchType(item);
        if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
            if (armorType == ArmorType.UNKNOWN) {
                event.setCancelled(true);
                return;
            }

            Slot armorSlot = SlotManager.getSlotManager().getSlot(armorType.name());
            if (armorSlot == null) {
                return;
            }

            ItemStack armorItem = InventoryUtils.getArmorItemById(player, armorType.getSlot());
            if (armorItem != null && armorItem.getType() != Material.AIR) {
                return;
            }

            event.setCancelled(!InventoryManager.validateArmor(InventoryAction.PLACE_ONE, armorSlot, item));
        }
    }

    @EventHandler
    public void onDispenseEquip(BlockDispenseEvent event) {
        ArmorType type = ArmorType.matchType(event.getItem());
        if (type != null) {
            Location loc = event.getBlock().getLocation();
            for (Player player : loc.getWorld().getPlayers()) {
                if (loc.getBlockY() - player.getLocation().getBlockY() >= -1 && loc.getBlockY() - player.getLocation().getBlockY() <= 1) {
                    if (player.getInventory().getHelmet() == null && type == ArmorType.HELMET
                            || player.getInventory().getChestplate() == null && type == ArmorType.CHESTPLATE
                            || player.getInventory().getLeggings() == null && type == ArmorType.LEGGINGS
                            || player.getInventory().getBoots() == null && type == ArmorType.BOOTS) {
                        org.bukkit.block.Dispenser dispenser = (org.bukkit.block.Dispenser) event.getBlock().getState();
                        org.bukkit.material.Dispenser dis = (org.bukkit.material.Dispenser) dispenser.getData();
                        BlockFace directionFacing = dis.getFacing();
                        // Someone told me not to do big if checks because it's hard to read, look at me doing it -_-
                        if (directionFacing == BlockFace.EAST && player.getLocation().getBlockX() != loc.getBlockX()
                                && player.getLocation().getX() <= loc.getX() + 2.3 && player.getLocation().getX() >= loc.getX()
                                || directionFacing == BlockFace.WEST && player.getLocation().getX() >= loc.getX() - 1.3
                                && player.getLocation().getX() <= loc.getX()
                                || directionFacing == BlockFace.SOUTH
                                && player.getLocation().getBlockZ() != loc.getBlockZ()
                                && player.getLocation().getZ() <= loc.getZ() + 2.3 && player.getLocation().getZ() >= loc.getZ()
                                || directionFacing == BlockFace.NORTH && player.getLocation().getZ() >= loc.getZ() - 1.3
                                && player.getLocation().getZ() <= loc.getZ()) {
                            if (!InventoryManager.playerIsLoaded(player)) {
                                return;
                            }

                            Slot armorSlot = SlotManager.getSlotManager().getSlot(type.name());
                            event.setCancelled(armorSlot != null && !InventoryManager.validateArmor(InventoryAction.PLACE_ONE, armorSlot, event.getItem()));
                            return;
                        }
                    }
                }
            }
        }
    }
}
