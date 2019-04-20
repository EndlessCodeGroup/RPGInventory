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

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.jetbrains.annotations.NotNull;
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
    public void onCommand(@NotNull PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (InventoryManager.isAllowedWorld(player.getWorld()) && !InventoryManager.playerIsLoaded(player)) {
            PlayerUtils.sendMessage(player, RPGInventory.getLanguage().getMessage("error.rp.denied"));
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerDead(@NotNull PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (InventoryManager.playerIsLoaded(player)) {
            InventorySaver.save(player, event.getDrops(),
                    RPGInventory.getPermissions().has(player, "rpginventory.keep.items") || event.getKeepInventory(),
                    RPGInventory.getPermissions().has(player, "rpginventory.keep.armor") || event.getKeepInventory(),
                    RPGInventory.getPermissions().has(player, "rpginventory.keep.rpginv") || event.getKeepInventory());
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerRespawn(@NotNull PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if (InventoryManager.playerIsLoaded(player)) {
            InventorySaver.restore(player);
        }
    }
}
