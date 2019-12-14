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

package ru.endlesscode.rpginventory.resourcepack;

import com.comphenix.packetwrapper.WrapperPlayClientResourcePackStatus;
import com.comphenix.packetwrapper.WrapperPlayServerResourcePackSend;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.endlesscode.inspector.bukkit.scheduler.TrackedBukkitRunnable;
import ru.endlesscode.rpginventory.RPGInventory;
import ru.endlesscode.rpginventory.inventory.InventoryManager;
import ru.endlesscode.rpginventory.misc.config.Config;
import ru.endlesscode.rpginventory.utils.EffectUtils;
import ru.endlesscode.rpginventory.utils.Log;
import ru.endlesscode.rpginventory.utils.PlayerUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by OsipXD on 02.10.2015
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
public class ResourcePackModule implements Listener {

    private static final int TICKS_IN_SECOND = 20;

    private final Plugin plugin;
    private final String resourcePackUrl;
    private final String resourcePackHash;

    private final List<UUID> loadList = new ArrayList<>();

    private ResourcePackModule(Plugin plugin, @NotNull String resourcePackUrl, String resourcePackHash) {
        this.plugin = plugin;
        this.resourcePackUrl = resourcePackUrl;
        this.resourcePackHash = resourcePackHash;
    }

    @Nullable
    public static ResourcePackModule init(Plugin plugin) {
        final FileConfiguration config = Config.getConfig();
        if (!config.getBoolean("resource-pack.enabled", false)) {
            Log.i("Resource-pack is disabled in config");
            return null;
        }

        final String rpUrl = Config.getConfig().getString("resource-pack.url");
        final String rpHash = Config.getConfig().getString("resource-pack.hash");
        ResourcePackValidator validator = new ResourcePackValidator();
        boolean isLegalUrlAndHash = validator.validateUrlAndHash(rpUrl, rpHash);
        printErrorsIfNotEmpty(validator.getErrors());
        if (!isLegalUrlAndHash) {
            Log.s("Resource-pack can not be enabled");
            return null;
        }

        ResourcePackModule resourcePackModule = new ResourcePackModule(plugin, rpUrl, rpHash);
        plugin.getServer().getPluginManager().registerEvents(resourcePackModule, plugin);
        ProtocolLibrary.getProtocolManager().addPacketListener(resourcePackModule.new ResourcePackPacketAdapter(plugin));
        return resourcePackModule;
    }

    private static void printErrorsIfNotEmpty(@NotNull List<String> messages) {
        if (messages.isEmpty()) {
            return;
        }

        Log.w("");
        Log.w("######### Something wrong with RP settings! ##########");
        for (String message : messages) {
            Log.w("# {0}", message);
        }
        Log.w("######################################################");
        Log.w("");
    }

    public void loadResourcePack(@NotNull Player player, boolean skipJoinMessage) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 1));

        if (skipJoinMessage) {
            this.sendResourcePack(player);
        } else if (InventoryManager.isNewPlayer(player)) {
            if (!EffectUtils.showJoinMessage(player, "rp-info", () -> this.sendResourcePack(player))) {
                this.sendResourcePack(player);
            }
        } else {
            EffectUtils.showDefaultJoinMessage(player);
            this.sendResourcePack(player);
        }
    }

    private void sendResourcePack(@NotNull final Player player) {
        new TrackedBukkitRunnable() {
            @Override
            public void run() {
                player.setResourcePack(resourcePackUrl);
            }
        }.runTaskLater(this.plugin, TICKS_IN_SECOND);
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
        Player player = event.getPlayer();
        if (InventoryManager.isAllowedWorld(player.getWorld()) && !InventoryManager.playerIsLoaded(player)) {
            player.removePotionEffect(PotionEffectType.BLINDNESS);
            this.removePlayerFromLoadList(event.getPlayer());
        }
    }

    private void removePlayerFromLoadList(@NotNull Player player) {
        loadList.remove(player.getUniqueId());
    }


    class ResourcePackPacketAdapter extends PacketAdapter {

        ResourcePackPacketAdapter(Plugin plugin) {
            super(plugin, WrapperPlayClientResourcePackStatus.TYPE, WrapperPlayServerResourcePackSend.TYPE);
        }

        @Override
        public void onPacketSending(@NotNull PacketEvent event) {
            WrapperPlayServerResourcePackSend packet = new WrapperPlayServerResourcePackSend(event.getPacket());
            if (!packet.getUrl().equals(resourcePackUrl)) {
                return;
            }

            packet.setHash(resourcePackHash);
            final Player player = event.getPlayer();
            loadList.add(player.getUniqueId());
        }

        @Override
        public void onPacketReceiving(@NotNull PacketEvent event) {
            WrapperPlayClientResourcePackStatus packet = new WrapperPlayClientResourcePackStatus(event.getPacket());
            final Player player = event.getPlayer();
            if (loadList.contains(player.getUniqueId())) {
                switch (packet.getResult()) {
                    case ACCEPTED:
                        break;
                    case SUCCESSFULLY_LOADED:
                        onSuccessfullyLoaded(player);
                        break;
                    case DECLINED:
                    case FAILED_DOWNLOAD:
                        onFailedOrDeclined(player);
                }
            }
        }

        private void onSuccessfullyLoaded(@NotNull Player player) {
            final UUID playerId = player.getUniqueId();
            new TrackedBukkitRunnable() {
                @Override
                public void run() {
                    InventoryManager.loadPlayerInventory(player);
                    player.removePotionEffect(PotionEffectType.BLINDNESS);
                }
            }.runTaskLater(this.plugin, 1);
            loadList.remove(playerId);
        }

        private void onFailedOrDeclined(@NotNull Player player) {
            new TrackedBukkitRunnable() {
                @Override
                public void run() {
                    player.kickPlayer(RPGInventory.getLanguage().getMessage("error.rp.denied"));
                }
            }.runTaskLater(this.plugin, TICKS_IN_SECOND);
            loadList.remove(player.getUniqueId());
        }
    }
}
