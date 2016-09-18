package ru.endlesscode.rpginventory.event.listener;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import ru.endlesscode.rpginventory.RPGInventory;
import ru.endlesscode.rpginventory.inventory.InventoryManager;
import ru.endlesscode.rpginventory.inventory.slot.Slot;
import ru.endlesscode.rpginventory.inventory.slot.SlotManager;
import ru.endlesscode.rpginventory.item.CustomItem;
import ru.endlesscode.rpginventory.item.ItemManager;
import ru.endlesscode.rpginventory.utils.ItemUtils;

import java.util.*;

/**
 * Created by OsipXD on 02.09.2016
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
public class PlayerListener implements Listener {
    private static final Map<UUID, ItemStack[]> INVENTORIES = new HashMap<>();
    private static final Map<UUID, ItemStack[]> ARMORS = new HashMap<>();

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event){
        Player player = event.getPlayer();
        if (InventoryManager.isAllowedWorld(player.getWorld()) && !InventoryManager.playerIsLoaded(player)) {
            player.sendMessage(RPGInventory.getLanguage().getCaption("error.rp.denied"));
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (InventoryManager.playerIsLoaded(player)) {
            return;
        }

        if (ResourcePackListener.isPreparedPlayer(player)) {
            ResourcePackListener.removePlayer(player);
            player.kickPlayer(RPGInventory.getLanguage().getCaption("error.rp.denied"));
            event.setCancelled(true);
        } else {
            Location newLocation = event.getFrom().clone();
            newLocation.setPitch(event.getTo().getPitch());
            newLocation.setYaw(event.getTo().getYaw());
            event.setTo(newLocation);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerDead(PlayerDeathEvent event) {
        Player player = event.getEntity();

        if (!InventoryManager.playerIsLoaded(player)) {
            return;
        }

        List<ItemStack> contents = new ArrayList<>();
        if (!event.getKeepInventory()) {
            boolean dropForPlayer = !RPGInventory.getPermissions().has(player, "rpginventory.keep.items");
            boolean dropArmorForPlayer = !RPGInventory.getPermissions().has(player, "rpginventory.keep.armor");

            // Saving armor
            List<ItemStack> armorList = new ArrayList<>();
            Inventory inventory = InventoryManager.get(player).getInventory();
            List<Slot> armorSlots = SlotManager.getSlotManager().getArmorSlots();
            InventoryManager.syncArmor(InventoryManager.get(player));
            for (ItemStack armor : player.getInventory().getArmorContents()) {
                if (dropArmorForPlayer && (!CustomItem.isCustomItem(armor) || ItemManager.getCustomItem(armor).isDrop())) {
                    boolean drop = true;
                    for (Slot slot : armorSlots) {
                        if (!slot.isDrop() && armor.getType() == (inventory.getItem(slot.getSlotId())).getType()) {
                            drop = false;
                            break;
                        }
                    }

                    if (drop) {
                        armorList.add(new ItemStack(Material.AIR, 0));
                    } else {
                        armorList.add(armor);
                        event.getDrops().remove(armor);
                    }
                } else {
                    armorList.add(armor);
                    event.getDrops().remove(armor);
                }
            }
            ARMORS.put(player.getUniqueId(), armorList.toArray(new ItemStack[armorList.size()]));

            // Saving quick slots
            for (Slot slot : SlotManager.getSlotManager().getQuickSlots()) {
                ItemStack quickItem = player.getInventory().getItem(slot.getQuickSlot());
                if (!ItemUtils.isEmpty(quickItem) && (!dropForPlayer || !slot.isDrop()) && !slot.isCup(quickItem)) {
                    contents.add(quickItem);
                    event.getDrops().remove(quickItem);
                }
            }
            // Saving shield
            Slot shieldSlot = SlotManager.getSlotManager().getShieldSlot();
            if (shieldSlot != null && (!dropForPlayer || !shieldSlot.isDrop())) {
                ItemStack itemInOffHand = player.getEquipment().getItemInOffHand();
                if (!ItemUtils.isEmpty(itemInOffHand)) {
                    contents.add(itemInOffHand);
                    event.getDrops().remove(itemInOffHand);
                }
            }

            // Saving inventory
            for (ItemStack drop : new ArrayList<>(event.getDrops())) {
                if (CustomItem.isCustomItem(drop) && !ItemManager.getCustomItem(drop).isDrop()) {
                    contents.add(drop);
                    event.getDrops().remove(drop);
                }
            }
            INVENTORIES.put(player.getUniqueId(), contents.toArray(new ItemStack[contents.size()]));
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        if (!InventoryManager.playerIsLoaded(player)) {
            return;
        }

        // Restoring armor
        if (ARMORS.containsKey(player.getUniqueId())) {
            player.getInventory().setArmorContents(ARMORS.get(player.getUniqueId()));
            ARMORS.remove(player.getUniqueId());
        }

        // Restoring inventory
        if (INVENTORIES.containsKey(player.getUniqueId())) {
            player.getInventory().addItem(INVENTORIES.get(player.getUniqueId()));
            INVENTORIES.remove(player.getUniqueId());
        }
    }
}
