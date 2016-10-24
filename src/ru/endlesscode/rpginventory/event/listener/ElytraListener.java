package ru.endlesscode.rpginventory.event.listener;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import ru.endlesscode.rpginventory.inventory.InventoryManager;
import ru.endlesscode.rpginventory.inventory.PlayerWrapper;

/**
 * Created by OsipXD on 08.04.2016
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
public class ElytraListener implements Listener {
    @EventHandler
    public void onPlayerFall(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!InventoryManager.playerIsLoaded(player) || player.isFlying()) {
            return;
        }

        PlayerWrapper playerWrapper = InventoryManager.get(player);
        if (player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() == Material.AIR) {
            if (!playerWrapper.isFalling() && event.getFrom().getY() > event.getTo().getY()) {
                playerWrapper.setFalling(true);
            } else if (playerWrapper.isFalling()) {
                playerWrapper.onFall();
            }
        } else if (playerWrapper.isFalling()) {
            playerWrapper.setFalling(false);
        }
    }
}