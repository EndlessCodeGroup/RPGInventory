package ru.endlesscode.rpginventory.event.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import ru.endlesscode.rpginventory.inventory.InventoryManager;
import ru.endlesscode.rpginventory.inventory.PlayerWrapper;
import ru.endlesscode.rpginventory.inventory.backpack.Backpack;
import ru.endlesscode.rpginventory.inventory.backpack.BackpackHolder;
import ru.endlesscode.rpginventory.inventory.backpack.BackpackManager;
import ru.endlesscode.rpginventory.inventory.chest.ChestManager;
import ru.endlesscode.rpginventory.utils.ItemUtils;

import java.util.Arrays;

/**
 * Created by OsipXD on 19.10.2015
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2015 © «EndlessCode Group»
 */
public class BackpackListener implements Listener {
    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.LOWEST)
    public void onUseBackpack(PlayerInteractEvent event) {
        if (event.hasItem() && ItemUtils.hasTag(event.getItem().clone(), ItemUtils.BACKPACK_TAG)) {
            Player player = event.getPlayer();
            if ((event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)
                    && InventoryManager.isQuickSlot(player.getInventory().getHeldItemSlot())) {
                BackpackManager.open(player, event.getItem());
            }

            event.setCancelled(true);
            event.getPlayer().updateInventory();
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inventory = event.getInventory();
        Player player = (Player) event.getWhoClicked();

        if (!InventoryManager.playerIsLoaded(player) || !(inventory.getHolder() instanceof BackpackHolder)) {
            return;
        }

        if (BackpackManager.isBackpack(event.getCurrentItem()) || BackpackManager.isBackpack(event.getCursor()) ||
                ChestManager.isCapSlot(event.getCurrentItem()) || ChestManager.isCapSlot(event.getCursor())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Inventory inventory = event.getInventory();
        Player player = (Player) event.getPlayer();

        if (!InventoryManager.playerIsLoaded(player) || !(inventory.getHolder() instanceof BackpackHolder)) {
            return;
        }

        PlayerWrapper playerWrapper = InventoryManager.get(player);
        Backpack backpack = playerWrapper.getBackpack();

        if (backpack == null) {
            return;
        }

        backpack.setContents(Arrays.copyOfRange(inventory.getContents(), 0, backpack.getType().getSize()));
        backpack.onClose();
        playerWrapper.setBackpack(null);
    }
}
