package ru.endlesscode.rpginventory.event.listener;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import ru.endlesscode.rpginventory.RPGInventory;
import ru.endlesscode.rpginventory.inventory.InventoryManager;

/**
 * Created by OsipXD on 02.09.2016
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
public class PlayerListener implements Listener {
    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event){
        Player player = event.getPlayer();
        if (InventoryManager.isAllowedWorld(player.getWorld()) && !InventoryManager.playerIsLoaded(player)) {
            player.sendMessage(RPGInventory.getLanguage().getCaption("error.rp.denied"));
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (InventoryManager.playerIsLoaded(player)) {
            return;
        }

        if (ResourcePackListener.isPreparedPlayer(player)) {
            ResourcePackListener.removePlayer(player);
            player.kickPlayer(RPGInventory.getLanguage().getCaption("error.rp.denied"));
            event.setCancelled(true);
        } else {
            Location newLocation = event.getFrom().clone();
            newLocation.setPitch(event.getTo().getPitch());
            newLocation.setYaw(event.getTo().getYaw());
            event.setTo(newLocation);
        }
    }
}
