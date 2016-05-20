package ru.endlesscode.rpginventory.event.updater;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import ru.endlesscode.rpginventory.inventory.InventoryManager;
import ru.endlesscode.rpginventory.inventory.PlayerWrapper;
import ru.endlesscode.rpginventory.item.ItemManager;
import ru.endlesscode.rpginventory.item.ItemStat;

/**
 * Created by OsipXD on 21.09.2015
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2015 © «EndlessCode Group»
 */
public class StatsUpdater extends BukkitRunnable {
    private final Player player;

    public StatsUpdater(Player player) {
        this.player = player;
    }

    @Override
    public void run() {
        if (!InventoryManager.playerIsLoaded(this.player)) {
            return;
        }

        PlayerWrapper playerWrapper = InventoryManager.get(this.player);
        playerWrapper.updatePermissions();

        // Update speed
        this.player.setWalkSpeed(playerWrapper.getBaseSpeed() * (float) ItemManager.getModifier(this.player, ItemStat.StatType.SPEED).getMultiplier());

        // Update info slots
        InventoryManager.syncInfoSlots(player, playerWrapper.getInventory());
    }
}