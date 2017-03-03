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