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

import com.comphenix.packetwrapper.included.WrapperPlayClientResourcePackStatus;
import com.comphenix.packetwrapper.included.WrapperPlayServerResourcePackSend;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import ru.endlesscode.rpginventory.RPGInventory;
import ru.endlesscode.rpginventory.inventory.InventoryManager;
import ru.endlesscode.rpginventory.misc.Config;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by OsipXD on 02.10.2015
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
public class PlayerLoader extends PacketAdapter implements Listener {
    private static final Map<UUID, LoadData> loadList = new HashMap<>();

    public PlayerLoader(Plugin plugin) {
        super(plugin, WrapperPlayClientResourcePackStatus.TYPE, WrapperPlayServerResourcePackSend.TYPE);
    }

    static void setDamageForPlayer(Player player, double damage) {
        if (loadList.containsKey(player.getUniqueId())) {
            loadList.get(player.getUniqueId()).setDamage(damage);
        }
    }

    static boolean isPreparedPlayer(Player player) {
        return loadList.containsKey(player.getUniqueId()) && loadList.get(player.getUniqueId()).isPrepared();
    }

    static void removePlayer(Player player) {
        if (loadList.containsKey(player.getUniqueId())) {
            loadList.remove(player.getUniqueId());
        }
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        WrapperPlayServerResourcePackSend packet = new WrapperPlayServerResourcePackSend(event.getPacket());
        if (!packet.getUrl().equals(Config.getConfig().getString("resource-pack.url"))) {
            return;
        }

        final Player player = event.getPlayer();
        packet.setHash(Config.getConfig().getString("resource-pack.hash"));
        final LoadData loadData = new LoadData();
        loadList.put(player.getUniqueId(), loadData);

        new BukkitRunnable() {
            @Override
            public void run() {
                loadData.setPrepared();
            }
        }.runTaskLater(RPGInventory.getInstance(), Config.getConfig().getInt("resource-pack.delay") * 20);
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {
        WrapperPlayClientResourcePackStatus packet = new WrapperPlayClientResourcePackStatus(event.getPacket());

        final Player player = event.getPlayer();
        if (!loadList.containsKey(player.getUniqueId())) {
            return;
        }

        switch (packet.getResult()) {
            case ACCEPTED:
                return;
            case SUCCESSFULLY_LOADED:
                final double damage = loadList.get(player.getUniqueId()).getDamage();
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        InventoryManager.loadPlayerInventory(player);
                        //noinspection deprecation
                        EntityDamageEvent event = new EntityDamageEvent(player, EntityDamageEvent.DamageCause.FALL, damage);
                        Bukkit.getPluginManager().callEvent(event);
                        player.removePotionEffect(PotionEffectType.BLINDNESS);
                    }
                }.runTaskLater(RPGInventory.getInstance(), 1);
                loadList.remove(player.getUniqueId());

                break;
            case DECLINED:
            case FAILED_DOWNLOAD:
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        player.kickPlayer(RPGInventory.getLanguage().getCaption("error.rp.denied"));
                    }
                }.runTaskLater(this.plugin, 20);
                loadList.remove(player.getUniqueId());
        }
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
