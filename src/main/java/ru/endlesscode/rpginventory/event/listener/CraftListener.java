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

import com.comphenix.packetwrapper.WrapperPlayServerWindowItems;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import ru.endlesscode.rpginventory.inventory.InventoryManager;
import ru.endlesscode.rpginventory.inventory.craft.CraftExtension;
import ru.endlesscode.rpginventory.inventory.craft.CraftManager;
import ru.endlesscode.rpginventory.misc.config.Config;
import ru.endlesscode.rpginventory.utils.PlayerUtils;

import java.util.List;

/**
 * Created by OsipXD on 29.08.2016
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
public class CraftListener extends PacketAdapter implements Listener {

    public CraftListener(@NotNull Plugin plugin) {
        super(plugin, WrapperPlayServerWindowItems.TYPE);

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void onPacketSending(@NotNull PacketEvent event) {
        Player player = event.getPlayer();
        //noinspection ConstantConditions
        if (event.isCancelled() || !InventoryManager.playerIsLoaded(player)
                || isExtensionsNotNeededHere(player)) {
            return;
        }

        WrapperPlayServerWindowItems packet = new WrapperPlayServerWindowItems(event.getPacket());
        if (player.getOpenInventory().getType() == InventoryType.WORKBENCH) {
            List<ItemStack> contents = packet.getSlotData();

            List<CraftExtension> extensions = CraftManager.getExtensions(player);
            for (CraftExtension extension : extensions) {
                for (int slot : extension.getSlots()) {
                    contents.set(slot, extension.getCapItem());
                }
            }

            packet.setSlotData(contents);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onInventoryOpen(@NotNull InventoryOpenEvent event) {
        final Player player = (Player) event.getPlayer();
        if (!InventoryManager.playerIsLoaded(player)
                || event.getInventory().getType() != InventoryType.WORKBENCH
                || isExtensionsNotNeededHere(player)) {
            return;
        }

        //noinspection deprecation
        player.updateInventory();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onInventoryClick(@NotNull InventoryClickEvent event) {
        final Player player = (Player) event.getWhoClicked();
        //noinspection ConstantConditions
        if (!InventoryManager.playerIsLoaded(player)
                || event.getInventory().getType() != InventoryType.WORKBENCH
                || isExtensionsNotNeededHere(player)) {
            return;
        }

        List<CraftExtension> extensions = CraftManager.getExtensions(player);
        for (CraftExtension extension : extensions) {
            for (int slot : extension.getSlots()) {
                if (slot == event.getRawSlot()) {
                    event.setCancelled(true);
                    PlayerUtils.updateInventory(player);
                    return;
                }
            }
        }
    }

    /**
     * Checks that inventory extensions not needed there.
     * It always should be used after `InventoryManager.playerIsLoaded(player)` check.
     *
     * @param player Player to check
     */
    private boolean isExtensionsNotNeededHere(Player player) {
        return !InventoryManager.get(player).isPocketCraft()
                && !Config.getConfig().getBoolean("craft.workbench", true);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorkbenchClosed(@NotNull InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        if (!InventoryManager.playerIsLoaded(player)) {
            return;
        }

        if (event.getInventory().getType() == InventoryType.WORKBENCH) {
            //noinspection ConstantConditions
            InventoryManager.get(player).onWorkbenchClosed();
        }
    }
}
