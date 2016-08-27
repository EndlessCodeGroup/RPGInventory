package ru.endlesscode.rpginventory.event.listener;

import com.comphenix.packetwrapper.included.WrapperPlayClientResourcePackStatus;
import com.comphenix.packetwrapper.included.WrapperPlayServerResourcePackSend;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import ru.endlesscode.rpginventory.RPGInventory;
import ru.endlesscode.rpginventory.inventory.InventoryManager;
import ru.endlesscode.rpginventory.inventory.ResourcePackManager;
import ru.endlesscode.rpginventory.misc.Config;
import ru.endlesscode.rpginventory.nms.VersionHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by OsipXD on 02.10.2015
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
public class ResourcePackListener extends PacketAdapter implements Listener {
    private final List<UUID> preparedPlayers = new ArrayList<>();

    public ResourcePackListener(Plugin plugin) {
        super(plugin, WrapperPlayClientResourcePackStatus.TYPE, WrapperPlayServerResourcePackSend.TYPE);
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        WrapperPlayServerResourcePackSend packet = new WrapperPlayServerResourcePackSend(event.getPacket());
        if (packet.getUrl().equals(Config.getConfig().getString("resource-pack.url"))) {
            packet.setHash(Config.getConfig().getString("resource-pack.hash"));
            preparedPlayers.add(event.getPlayer().getUniqueId());
        }
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {
        if (ResourcePackManager.getMode() == ResourcePackManager.Mode.DISABLED) {
            return;
        }

        WrapperPlayClientResourcePackStatus packet = new WrapperPlayClientResourcePackStatus(event.getPacket());

        final Player player = event.getPlayer();
        if (preparedPlayers.contains(player.getUniqueId())) {
            switch (packet.getResult()) {
                case ACCEPTED:
                    return;
                case SUCCESSFULLY_LOADED:
                    ResourcePackManager.loadedResourcePack(player, true);
                    preparedPlayers.remove(player.getUniqueId());
                    break;
                case FAILED_DOWNLOAD:
                    if (VersionHandler.is1_8_R1()) {
                        preparedPlayers.remove(player.getUniqueId());
                        return;
                    }
                case DECLINED:
                    if (ResourcePackManager.getMode() == ResourcePackManager.Mode.FORCE || ResourcePackManager.getMode() == ResourcePackManager.Mode.EXPERIMENTAL) {
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                player.kickPlayer(RPGInventory.getLanguage().getCaption("error.rp.denied"));
                            }
                        }.runTaskLater(this.plugin, 20);
                    }

                    ResourcePackManager.loadedResourcePack(player, false);
                    preparedPlayers.remove(player.getUniqueId());
            }

            new InventoryUpdater(player).runTaskLater(this.plugin, 5);
        } else {
            if (packet.getResult() == EnumWrappers.ResourcePackStatus.SUCCESSFULLY_LOADED) {
                ResourcePackManager.loadedResourcePack(player, false);
                new InventoryUpdater(player).runTaskLater(this.plugin, 5);
            }
        }
    }

    private class InventoryUpdater extends BukkitRunnable {
        private final Player player;

        InventoryUpdater(Player player) {
            this.player = player;
        }

        @Override
        public void run() {
            InventoryManager.unloadPlayerInventory(this.player);
            InventoryManager.loadPlayerInventory(this.player);
        }
    }
}
