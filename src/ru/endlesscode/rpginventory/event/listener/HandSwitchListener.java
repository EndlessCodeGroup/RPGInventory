package ru.endlesscode.rpginventory.event.listener;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import ru.endlesscode.rpginventory.inventory.InventoryManager;
import ru.endlesscode.rpginventory.inventory.slot.Slot;
import ru.endlesscode.rpginventory.inventory.slot.SlotManager;
import ru.endlesscode.rpginventory.item.ItemManager;
import ru.endlesscode.rpginventory.utils.InventoryUtils;

/**
 * Created by OsipXD on 07.04.2016
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
public class HandSwitchListener implements Listener {
    @EventHandler
    public void onHandSwitch(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();

        Slot offHandSlot = SlotManager.getSlotManager().getShieldSlot();
        Slot mainHandSlot = SlotManager.getSlotManager().getSlot(
                player.getInventory().getHeldItemSlot(), InventoryType.SlotType.QUICKBAR);
        ItemStack newOffHandItem = event.getOffHandItem();
        ItemStack newMainHandItem = event.getMainHandItem();

        if (offHandSlot == null && mainHandSlot == null) {
            return;
        }

        if (offHandSlot != null) {
            if (newOffHandItem != null && newOffHandItem.getType() != Material.AIR
                    && !InventoryManager.validateUpdate(player, InventoryUtils.ActionType.SET, offHandSlot, newOffHandItem)) {
                event.setCancelled(true);
                return;
            }
        }

        if (InventoryManager.isInventoryOpenItem(newOffHandItem)) {
            event.setCancelled(true);
            return;
        }

        if (mainHandSlot != null) {
            if (newOffHandItem != null && mainHandSlot.isCup(newOffHandItem)) {
                event.setCancelled(true);
                return;
            }

            if (!InventoryManager.validateUpdate(player, InventoryUtils.ActionType.SET, mainHandSlot, newMainHandItem)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void afterHandSwitch(PlayerSwapHandItemsEvent event) {
        ItemManager.updateStats(event.getPlayer());
    }
}
