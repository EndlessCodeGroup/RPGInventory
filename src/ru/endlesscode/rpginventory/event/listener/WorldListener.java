package ru.endlesscode.rpginventory.event.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldSaveEvent;
import ru.endlesscode.rpginventory.inventory.InventoryManager;
import ru.endlesscode.rpginventory.inventory.backpack.BackpackManager;

/**
 * Created by OsipXD on 24.11.2015
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2015 © «EndlessCode Group»
 */
public class WorldListener implements Listener {
    @EventHandler
    public void onWorldSave(WorldSaveEvent event) {
        BackpackManager.saveBackpacks();

        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            InventoryManager.savePlayerInventory(player);
        }
    }
}
