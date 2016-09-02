package ru.endlesscode.rpginventory.event.listener;

import com.comphenix.packetwrapper.included.WrapperPlayServerWindowItems;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import ru.endlesscode.rpginventory.RPGInventory;
import ru.endlesscode.rpginventory.inventory.InventoryManager;
import ru.endlesscode.rpginventory.inventory.craft.CraftExtension;
import ru.endlesscode.rpginventory.inventory.craft.CraftManager;

import java.util.List;

/**
 * Created by OsipXD on 29.08.2016
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
public class CraftListener extends PacketAdapter implements Listener {
    public CraftListener(Plugin plugin) {
        super(plugin, WrapperPlayServerWindowItems.TYPE);

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        Player player = event.getPlayer();
        if (!InventoryManager.playerIsLoaded(player)) {
            return;
        }

        WrapperPlayServerWindowItems packet = new WrapperPlayServerWindowItems(event.getPacket());
        if (player.getOpenInventory().getType() == InventoryType.WORKBENCH) {
            ItemStack[] contents = packet.getSlotData();

            List<CraftExtension> extensions = CraftManager.getExtensions(player);
            for (CraftExtension extension : extensions) {
                for (int slot : extension.getSlots()) {
                    contents[slot] = extension.getCapItem();
                }
            }

            packet.setSlotData(contents);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        final Player player = (Player) event.getWhoClicked();
        if (!InventoryManager.playerIsLoaded(player) || event.getInventory().getType() != InventoryType.WORKBENCH) {
            return;
        }

        List<CraftExtension> extensions = CraftManager.getExtensions(player);
        for (CraftExtension extension : extensions) {
            for (int slot : extension.getSlots()) {
                if (slot == event.getRawSlot()) {
                    event.setCancelled(true);

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            player.updateInventory();
                        }
                    }.runTaskLater(RPGInventory.getInstance(), 1);

                    return;
                }
            }
        }
    }
}
