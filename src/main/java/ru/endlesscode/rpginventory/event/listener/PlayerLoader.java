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

import com.comphenix.packetwrapper.WrapperPlayClientResourcePackStatus;
import com.comphenix.packetwrapper.WrapperPlayServerResourcePackSend;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import ru.endlesscode.inspector.bukkit.scheduler.TrackedBukkitRunnable;
import ru.endlesscode.rpginventory.RPGInventory;
import ru.endlesscode.rpginventory.inventory.InventoryManager;
import ru.endlesscode.rpginventory.misc.config.Config;
import ru.endlesscode.rpginventory.utils.PlayerUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by OsipXD on 02.10.2015
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
public class PlayerLoader extends PacketAdapter implements Listener {
    private final Map<UUID, LoadData> loadList = new HashMap<>();

    public PlayerLoader(Plugin plugin) {
        super(plugin, WrapperPlayClientResourcePackStatus.TYPE, WrapperPlayServerResourcePackSend.TYPE);

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void onPacketSending(@NotNull PacketEvent event) {
        WrapperPlayServerResourcePackSend packet = new WrapperPlayServerResourcePackSend(event.getPacket());
        if (!packet.getUrl().equals(Config.getConfig().getString("resource-pack.url"))) {
            return;
        }

        final Player player = event.getPlayer();
        packet.setHash(Config.getConfig().getString("resource-pack.hash"));
        final LoadData loadData = new LoadData();
        loadList.put(player.getUniqueId(), loadData);

        new TrackedBukkitRunnable() {
            @Override
            public void run() {
                loadData.setPrepared();
            }
        }.runTaskLater(RPGInventory.getInstance(), Config.getConfig().getInt("resource-pack.delay") * 20);
    }

    @Override
    public void onPacketReceiving(@NotNull PacketEvent event) {
        WrapperPlayClientResourcePackStatus packet = new WrapperPlayClientResourcePackStatus(event.getPacket());

        final Player player = event.getPlayer();
        final UUID playerId = player.getUniqueId();
        if (loadList.containsKey(playerId)) {
            switch (packet.getResult()) {
                case ACCEPTED:
                    break;
                case SUCCESSFULLY_LOADED:
                    final double damage = loadList.get(playerId).getDamage();
                    new TrackedBukkitRunnable() {
                        @Override
                        public void run() {
                            InventoryManager.loadPlayerInventory(player);
                            if (damage > 0) {
                                EntityDamageEvent event = new EntityDamageEvent(player, EntityDamageEvent.DamageCause.FALL, damage);
                                Bukkit.getPluginManager().callEvent(event);
                            }
                            player.removePotionEffect(PotionEffectType.BLINDNESS);
                        }
                    }.runTaskLater(RPGInventory.getInstance(), 1);
                    loadList.remove(playerId);

                    break;
                case DECLINED:
                case FAILED_DOWNLOAD:
                    new TrackedBukkitRunnable() {
                        @Override
                        public void run() {
                            player.kickPlayer(RPGInventory.getLanguage().getMessage("error.rp.denied"));
                        }
                    }.runTaskLater(this.plugin, 20);
                    loadList.remove(playerId);
            }
        }
    }


    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onDamageWhenPlayerNotLoaded(@NotNull EntityDamageEvent event) {
        if (event.getEntity() == null
                || event.getEntityType() != EntityType.PLAYER) {
            return;
        }

        Player player = (Player) event.getEntity();
        if (InventoryManager.playerIsLoaded(player)) {
            return;
        }

        if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            this.setDamageForPlayer(player, event.getFinalDamage());
        }

        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onTargetWhenPlayerNotLoaded(@NotNull EntityTargetLivingEntityEvent event) {
        if (event.getTarget() == null || event.getTarget().getType() != EntityType.PLAYER) {
            return;
        }

        if (!InventoryManager.playerIsLoaded((Player) event.getTarget())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onPlayerMoveWhenNotLoaded(@NotNull PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (!InventoryManager.isAllowedWorld(player.getWorld()) || InventoryManager.playerIsLoaded(player)) {
            return;
        }

        if (this.isPreparedPlayer(player)) {
            this.removePlayerFromLoadList(player);
            player.kickPlayer(RPGInventory.getLanguage().getMessage("error.rp.denied"));
            event.setCancelled(true);
        } else {
            Location toLocation = event.getTo();
            Location newLocation = event.getFrom().clone();
            if (!player.isOnGround()) {
                newLocation.setY(toLocation.getY());
            }

            newLocation.setPitch(toLocation.getPitch());
            newLocation.setYaw(toLocation.getYaw());
            event.setTo(newLocation);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerInteractWhenNotLoaded(@NotNull PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (InventoryManager.isAllowedWorld(player.getWorld()) && !InventoryManager.playerIsLoaded(player)) {
            PlayerUtils.sendMessage(player, RPGInventory.getLanguage().getMessage("error.rp.denied"));
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerQuit(@NotNull PlayerQuitEvent event) {
        this.removePlayerFromLoadList(event.getPlayer());
    }

    private void setDamageForPlayer(@NotNull Player player, double damage) {
        if (loadList.containsKey(player.getUniqueId())) {
            loadList.get(player.getUniqueId()).setDamage(damage);
        }
    }

    private boolean isPreparedPlayer(@NotNull Player player) {
        return loadList.containsKey(player.getUniqueId()) && loadList.get(player.getUniqueId()).isPrepared();
    }

    private void removePlayerFromLoadList(@NotNull Player player) {
        loadList.remove(player.getUniqueId());
    }


    private static class LoadData {
        boolean prepared = false;
        double damage = 0;

        double getDamage() {
            return damage;
        }

        void setDamage(double damage) {
            if (this.damage == 0) {
                this.damage = damage;
            }
        }

        boolean isPrepared() {
            return prepared;
        }

        void setPrepared() {
            this.prepared = true;
        }
    }
}
