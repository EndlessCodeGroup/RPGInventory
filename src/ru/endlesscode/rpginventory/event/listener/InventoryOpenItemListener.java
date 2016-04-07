package ru.endlesscode.rpginventory.event.listener;

import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import ru.endlesscode.rpginventory.event.PlayerInventoryLoadEvent;
import ru.endlesscode.rpginventory.event.PlayerInventoryUnloadEvent;
import ru.endlesscode.rpginventory.inventory.InventoryManager;
import ru.endlesscode.rpginventory.inventory.ResourcePackManager;
import ru.endlesscode.rpginventory.nms.VersionHandler;

import java.util.ArrayList;

/**
 * Created by OsipXD on 26.09.2015
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2015 © «EndlessCode Group»
 */
public class InventoryOpenItemListener implements Listener {
    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteractItem(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!InventoryManager.playerIsLoaded(player) || ResourcePackManager.isLoadedResourcePack(player)) {
            if (InventoryManager.isInventoryOpenItem(event.getItem())) {
                if (VersionHandler.is1_9()) {
                    player.getEquipment().setItemInMainHand(new ItemStack(Material.AIR));
                } else {
                    //noinspection deprecation
                    player.setItemInHand(new ItemStack(Material.AIR));
                }
                event.setCancelled(true);
            }

            return;
        }

        if ((event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) && InventoryManager.isInventoryOpenItem(event.getItem())) {
            InventoryManager.get(player).openInventory();
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        for (ItemStack drop : new ArrayList<>(event.getDrops())) {
            if (InventoryManager.isInventoryOpenItem(drop)) {
                event.getDrops().remove(drop);
                return;
            }
        }
    }

    @EventHandler
    public void onPlayerDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (!InventoryManager.playerIsLoaded(player) || InventoryManager.isInventoryOpenItem(event.getItemDrop().getItemStack())) {
            if (ResourcePackManager.isLoadedResourcePack(player)) {
                event.getItemDrop().remove();
                return;
            }

            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        HumanEntity player = event.getWhoClicked();

        if (!InventoryManager.playerIsLoaded(player) || ResourcePackManager.isLoadedResourcePack(player)) {
            return;
        }

        if (event.getSlotType() == InventoryType.SlotType.QUICKBAR && event.getSlot() == InventoryManager.OPEN_ITEM_SLOT) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if (!InventoryManager.playerIsLoaded(player) || ResourcePackManager.isLoadedResourcePack(player)) {
            return;
        }

        player.getInventory().setItem(InventoryManager.OPEN_ITEM_SLOT, InventoryManager.getInventoryOpenItem());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryLoad(PlayerInventoryLoadEvent.Post event) {
        Player player = event.getPlayer();
        if (!InventoryManager.playerIsLoaded(player) || ResourcePackManager.isLoadedResourcePack(player)) {
            return;
        }

        int slot = InventoryManager.OPEN_ITEM_SLOT;
        Inventory inventory = player.getInventory();

        ItemStack itemBackup = inventory.getItem(slot);
        inventory.setItem(slot, InventoryManager.getInventoryOpenItem());
        if (itemBackup != null && !InventoryManager.isInventoryOpenItem(itemBackup)) {
            inventory.addItem(itemBackup);
        }
    }

    @EventHandler
    public void onInventoryUnload(PlayerInventoryUnloadEvent.Post event) {
        Player player = event.getPlayer();

        if (ResourcePackManager.isLoadedResourcePack(player)) {
            return;
        }

        Inventory inventory = player.getInventory();
        int slot = InventoryManager.OPEN_ITEM_SLOT;

        if (InventoryManager.isInventoryOpenItem(inventory.getItem(slot))) {
            inventory.setItem(slot, new ItemStack(Material.AIR));
        }
    }
}
