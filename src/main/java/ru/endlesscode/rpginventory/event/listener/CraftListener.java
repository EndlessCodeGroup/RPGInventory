package ru.endlesscode.rpginventory.event.listener;

import com.comphenix.packetwrapper.included.WrapperPlayServerWindowItems;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import ru.endlesscode.rpginventory.inventory.InventoryManager;
import ru.endlesscode.rpginventory.inventory.craft.CraftExtension;
import ru.endlesscode.rpginventory.inventory.craft.CraftManager;
import ru.endlesscode.rpginventory.misc.Config;
import ru.endlesscode.rpginventory.utils.PlayerUtils;

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
        //noinspection ConstantConditions
        if (event.isCancelled() || !InventoryManager.playerIsLoaded(player)
                || !InventoryManager.get(player).isPocketCraft() && !Config.getConfig().getBoolean("craft.workbench", true)) {
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

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        final Player player = (Player) event.getWhoClicked();
        //noinspection ConstantConditions
        if (event.isCancelled() || !InventoryManager.playerIsLoaded(player)
                || event.getInventory().getType() != InventoryType.WORKBENCH
                || !InventoryManager.get(player).isPocketCraft() && !Config.getConfig().getBoolean("craft.workbench", true)) {
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

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorkbenchClosed(InventoryCloseEvent event) {
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
