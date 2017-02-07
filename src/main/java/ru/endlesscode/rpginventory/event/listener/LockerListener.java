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
    public void onGameModeSwitch(PlayerGameModeChangeEvent event) {
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
    public void onInventoryClick(InventoryClickEvent event) {
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
                PlayerUtils.sendMessage(player, RPGInventory.getLanguage().getCaption("error.previous"));
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
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
