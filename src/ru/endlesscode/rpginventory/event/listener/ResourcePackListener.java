package ru.endlesscode.rpginventory.event.listener;

import com.comphenix.packetwrapper.included.WrapperPlayClientResourcePackStatus;
import com.comphenix.packetwrapper.included.WrapperPlayServerResourcePackSend;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
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
public class ResourcePackListener extends PacketAdapter implements Listener {
    private static final Map<UUID, Boolean> preparedPlayers = new HashMap<>();

    public ResourcePackListener(Plugin plugin) {
        super(plugin, WrapperPlayClientResourcePackStatus.TYPE, WrapperPlayServerResourcePackSend.TYPE);
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        WrapperPlayServerResourcePackSend packet = new WrapperPlayServerResourcePackSend(event.getPacket());
        if (packet.getUrl().equals(Config.getConfig().getString("resource-pack.url"))) {
            final Player player = event.getPlayer();
            packet.setHash(Config.getConfig().getString("resource-pack.hash"));
            preparedPlayers.put(player.getUniqueId(), false);

            new BukkitRunnable() {
                @Override
                public void run() {
                    preparedPlayers.put(player.getUniqueId(), true);
                }
            }.runTaskLater(RPGInventory.getInstance(), 40);
        }
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {
        WrapperPlayClientResourcePackStatus packet = new WrapperPlayClientResourcePackStatus(event.getPacket());

        final Player player = event.getPlayer();
        if (preparedPlayers.containsKey(player.getUniqueId())) {
            switch (packet.getResult()) {
                case ACCEPTED:
                    return;
                case SUCCESSFULLY_LOADED:
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            InventoryManager.loadPlayerInventory(player);
                        }
                    }.runTaskLater(RPGInventory.getInstance(), 1);
                    preparedPlayers.remove(player.getUniqueId());

                    break;
                case DECLINED:
                case FAILED_DOWNLOAD:
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            player.kickPlayer(RPGInventory.getLanguage().getCaption("error.rp.denied"));
                        }
                    }.runTaskLater(this.plugin, 20);
                    preparedPlayers.remove(player.getUniqueId());
            }
        }
    }

    static boolean isPreparedPlayer(Player player) {
        return preparedPlayers.containsKey(player.getUniqueId()) && preparedPlayers.get(player.getUniqueId());
    }

    static void removePlayer(Player player) {
        if (preparedPlayers.containsKey(player.getUniqueId())) {
            preparedPlayers.remove(player.getUniqueId());
        }
    }
}
