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
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import org.jetbrains.annotations.*;
import ru.endlesscode.rpginventory.inventory.InventoryManager;
import ru.endlesscode.rpginventory.inventory.PlayerWrapper;
import ru.endlesscode.rpginventory.utils.LocationUtils;

/**
 * Created by OsipXD on 08.04.2016
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
public class ElytraListener implements Listener {
    private boolean isGetWidthMethodAvailable = true;

    @EventHandler(ignoreCancelled = true)
    public void onPlayerFall(@NotNull PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!InventoryManager.playerIsLoaded(player) || player.isFlying()
                || player.getVehicle() != null) {
            return;
        }

        PlayerWrapper playerWrapper = InventoryManager.get(player);
        boolean endFalling = false;
        if (!player.isOnGround()) {
            if (playerIsSneakOnLadder(player) || isPlayerCanFall(player)) {
                playerWrapper.onFall();
            } else if (!player.isGliding()) {
                endFalling = true;
            }
        } else if (playerWrapper.isFalling()) {
            endFalling = true;
        }

        if (endFalling) {
            playerWrapper.setFalling(false);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityToggleGlide(@NotNull EntityToggleGlideEvent event) {
        if (event.getEntity() == null || event.getEntityType() != EntityType.PLAYER) {
            return;
        }

        Player player = (Player) event.getEntity();
        if (!InventoryManager.playerIsLoaded(player)) {
            return;
        }

        if (event.isGliding()) {
            PlayerWrapper playerWrapper = InventoryManager.get(player);
            playerWrapper.onStartGliding();
        }
    }

    private boolean isPlayerCanFall(@NotNull Player player) {
        double playerWidth = 0.6D;
        if (this.isGetWidthMethodAvailable) {
            try {
                playerWidth = player.getWidth();
            } catch (NoSuchMethodError ex) {
                this.isGetWidthMethodAvailable = false;
            }
        }
        return !LocationUtils.isUnderAnyBlockHonestly(player.getLocation(), playerWidth, 3)
                && !playerIsOnLadder(player);
    }

    private boolean playerIsSneakOnLadder(Player player) {
        return player.isSneaking() && playerIsOnLadder(player);
    }

    private boolean playerIsOnLadder(Player player) {
        return player.getLocation().getBlock().getType() == Material.LADDER;
    }
}
