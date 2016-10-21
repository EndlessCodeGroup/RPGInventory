package ru.endlesscode.rpginventory.inventory;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import ru.endlesscode.rpginventory.inventory.slot.Slot;
import ru.endlesscode.rpginventory.inventory.slot.SlotManager;
import ru.endlesscode.rpginventory.item.CustomItem;
import ru.endlesscode.rpginventory.item.ItemManager;
import ru.endlesscode.rpginventory.utils.ItemUtils;

import java.util.*;

/**
 * Created by OsipXD on 06.10.2016
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
public class InventorySaver {
    private static final Map<UUID, ItemStack[]> INVENTORIES = new HashMap<>();
    private static final Map<UUID, ItemStack[]> ARMORS = new HashMap<>();

    public static void save(Player player, List<ItemStack> drops, boolean saveItems, boolean saveArmor) {
        // Save armor
        List<ItemStack> armorList = new ArrayList<>();
        Inventory inventory = InventoryManager.get(player).getInventory();
        List<Slot> armorSlots = SlotManager.getSlotManager().getArmorSlots();
        InventoryManager.syncArmor(InventoryManager.get(player));

        for (ItemStack armor : player.getInventory().getArmorContents()) {
            if (saveArmor || (CustomItem.isCustomItem(armor) && !ItemManager.getCustomItem(armor).isDrop())) {
                armorList.add(armor);
                drops.remove(armor);
            } else {
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
                    drops.remove(armor);
                }
            }
        }
        ARMORS.put(player.getUniqueId(), armorList.toArray(new ItemStack[armorList.size()]));

        // Save quick slots
        List<ItemStack> contents = new ArrayList<>();
        for (Slot slot : SlotManager.getSlotManager().getQuickSlots()) {
            ItemStack quickItem = player.getInventory().getItem(slot.getQuickSlot());
            if (!ItemUtils.isEmpty(quickItem) && (saveItems || !slot.isDrop()) && !slot.isCup(quickItem)) {
                contents.add(quickItem);
                drops.remove(quickItem);
            }
        }

        // Saving shield
        Slot shieldSlot = SlotManager.getSlotManager().getShieldSlot();
        if (shieldSlot != null && (saveItems || !shieldSlot.isDrop())) {
            ItemStack itemInOffHand = player.getEquipment().getItemInOffHand();
            if (!ItemUtils.isEmpty(itemInOffHand)) {
                contents.add(itemInOffHand);
                drops.remove(itemInOffHand);
            }
        }

        // Saving inventory
        for (ItemStack drop : new ArrayList<>(drops)) {
            if (saveItems || !CustomItem.isCustomItem(drop) || !ItemManager.getCustomItem(drop).isDrop()) {
                contents.add(drop);
                drops.remove(drop);
            }
        }
        INVENTORIES.put(player.getUniqueId(), contents.toArray(new ItemStack[contents.size()]));
    }

    public static void restore(Player player) {
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
