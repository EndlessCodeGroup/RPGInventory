package ru.endlesscode.rpginventory.item;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import ru.endlesscode.rpginventory.inventory.InventoryManager;
import ru.endlesscode.rpginventory.inventory.PlayerWrapper;
import ru.endlesscode.rpginventory.nms.VersionHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by OsipXD on 21.09.2015
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2015 © «EndlessCode Group»
 */
class StatsUpdater extends BukkitRunnable {
    private final Player player;

    StatsUpdater(Player player) {
        this.player = player;
    }

    @Override
    public void run() {
        if (!InventoryManager.playerIsLoaded(this.player)) {
            return;
        }

        PlayerWrapper playerWrapper = InventoryManager.get(this.player);

        // Update permissions
        playerWrapper.clearPermissions();
        List<ItemStack> items = new ArrayList<>(playerWrapper.getInventory().getContents().length + 1);
        Collections.addAll(items, playerWrapper.getInventory().getContents());
        if (VersionHandler.is1_9()) {
            items.add(this.player.getEquipment().getItemInOffHand());
            items.add(this.player.getEquipment().getItemInMainHand());
        } else {
            //noinspection deprecation
            items.add(this.player.getItemInHand());
        }
        for (ItemStack item : items) {
            if (CustomItem.isCustomItem(item)) {
                CustomItem customItem = ItemManager.getCustomItem(item);
                customItem.onEquip(this.player);
            }
        }

        playerWrapper.updateHealth();

        // Update speed
        this.player.setWalkSpeed(playerWrapper.getBaseSpeed() * ItemManager.getModifier(this.player, ItemStat.StatType.SPEED, false).getMultiplier());
    }
}