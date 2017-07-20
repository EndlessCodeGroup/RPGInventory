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

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import ru.endlesscode.rpginventory.RPGInventory;
import ru.endlesscode.rpginventory.inventory.InventoryManager;
import ru.endlesscode.rpginventory.inventory.InventorySaver;
import ru.endlesscode.rpginventory.utils.PlayerUtils;

/**
 * Created by OsipXD on 02.09.2016
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
public class PlayerListener implements Listener {
    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (InventoryManager.isAllowedWorld(player.getWorld()) && !InventoryManager.playerIsLoaded(player)) {
            PlayerUtils.sendMessage(player, RPGInventory.getLanguage().getCaption("error.rp.denied"));
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onDamageWhenPlayerNotLoaded(EntityDamageEvent event) {
        if (event.isCancelled() || event.getEntityType() != EntityType.PLAYER) {
            return;
        }

        Player player = (Player) event.getEntity();
        if (InventoryManager.playerIsLoaded(player)) {
            return;
        }

        if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            PlayerLoader.setDamageForPlayer(player, event.getFinalDamage());
        }

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onTargetWhenPlayerNotLoaded(EntityTargetLivingEntityEvent event) {
        if (event.isCancelled() || event.getTarget() == null || event.getTarget().getType() != EntityType.PLAYER) {
            return;
        }

        if (!InventoryManager.playerIsLoaded((Player) event.getTarget())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onPlayerMoveWhenNotLoaded(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (!InventoryManager.isAllowedWorld(player.getWorld()) || InventoryManager.playerIsLoaded(player)) {
            return;
        }

        if (PlayerLoader.isPreparedPlayer(player)) {
            PlayerLoader.removePlayer(player);
            player.kickPlayer(RPGInventory.getLanguage().getCaption("error.rp.denied"));
            event.setCancelled(true);
        } else {
            Location toLocation = event.getTo();
            Location newLocation = event.getFrom().clone();
            //noinspection deprecation
            if (!player.isOnGround()) {
                newLocation.setY(toLocation.getY());
            }

            newLocation.setPitch(toLocation.getPitch());
            newLocation.setYaw(toLocation.getYaw());
            event.setTo(newLocation);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerInteractWhenNotLoaded(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (InventoryManager.isAllowedWorld(player.getWorld()) && !InventoryManager.playerIsLoaded(player)) {
            PlayerUtils.sendMessage(player, RPGInventory.getLanguage().getCaption("error.rp.denied"));
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerDead(PlayerDeathEvent event) {
        Player player = event.getEntity();

        if (!InventoryManager.playerIsLoaded(player)) {
            return;
        }

        InventorySaver.save(player, event.getDrops(),
                RPGInventory.getPermissions().has(player, "rpginventory.keep.items") || event.getKeepInventory(),
                RPGInventory.getPermissions().has(player, "rpginventory.keep.armor") || event.getKeepInventory(),
                RPGInventory.getPermissions().has(player, "rpginventory.keep.rpginv") || event.getKeepInventory());
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        if (!InventoryManager.playerIsLoaded(player)) {
            return;
        }

        InventorySaver.restore(player);
    }
}
