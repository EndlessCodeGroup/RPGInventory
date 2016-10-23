package ru.endlesscode.rpginventory.inventory;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import ru.endlesscode.rpginventory.RPGInventory;
import ru.endlesscode.rpginventory.event.PetUnequipEvent;
import ru.endlesscode.rpginventory.inventory.slot.Slot;
import ru.endlesscode.rpginventory.inventory.slot.SlotManager;
import ru.endlesscode.rpginventory.item.CustomItem;
import ru.endlesscode.rpginventory.item.ItemManager;
import ru.endlesscode.rpginventory.pet.PetManager;
import ru.endlesscode.rpginventory.pet.PetType;
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
    private static final Map<UUID, ItemStack> EXTRA = new HashMap<>();

    public static void save(Player player, List<ItemStack> drops, boolean saveItems, boolean saveArmor, boolean saveRpgInv) {
        Inventory inventory = InventoryManager.get(player).getInventory();
        InventoryManager.syncArmor(InventoryManager.get(player));
        InventoryManager.syncQuickSlots(InventoryManager.get(player));
        InventoryManager.syncShieldSlot(InventoryManager.get(player));

        // Save armor
        List<ItemStack> armorList = new ArrayList<>(4);
        List<Slot> armorSlots = SlotManager.getSlotManager().getArmorSlots();

        for (ItemStack armor : player.getInventory().getArmorContents()) {
            if (saveArmor || CustomItem.isCustomItem(armor) && !ItemManager.getCustomItem(armor).isDrop()) {
                armorList.add(armor);
                drops.remove(armor);
            } else {
                boolean drop = true;
                for (Slot slot : new ArrayList<>(armorSlots)) {
                    if (armor.getType() == (inventory.getItem(slot.getSlotId())).getType()) {
                        drop = slot.isDrop();
                        armorSlots.remove(slot);
                        break;
                    }
                }

                if (drop) {
                    armorList.add(new ItemStack(Material.AIR));
                } else {
                    armorList.add(armor);
                    drops.remove(armor);
                }
            }
        }
        ARMORS.put(player.getUniqueId(), armorList.toArray(new ItemStack[armorList.size()]));

        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item != null && !drops.contains(item)) {
                contents[i] = null;
            }
        }

        // Save quick slots
        for (Slot slot : SlotManager.getSlotManager().getQuickSlots()) {
            ItemStack quickItem = player.getInventory().getItem(slot.getQuickSlot());
            if (!ItemUtils.isEmpty(quickItem) && (saveRpgInv || !slot.isDrop()) && !slot.isCup(quickItem)) {
                drops.remove(quickItem);
            } else {
                contents[slot.getQuickSlot()] = null;
            }
        }

        // Saving RPG inventory
        if (!saveRpgInv) {
            int petSlotId = PetManager.getPetSlotId();
            if (PetManager.isEnabled() && inventory.getItem(petSlotId) != null) {
                Slot petSlot = SlotManager.getSlotManager().getPetSlot();
                ItemStack petItem = inventory.getItem(petSlotId);

                if (petSlot != null && petSlot.isDrop() && !petSlot.isCup(petItem)) {
                    drops.add(PetType.clone(petItem));
                    RPGInventory.getInstance().getServer().getPluginManager().callEvent(new PetUnequipEvent(player));
                    inventory.setItem(petSlotId, petSlot.getCup());
                }
            }

            for (Slot slot : SlotManager.getSlotManager().getPassiveSlots()) {
                for (int slotId : slot.getSlotIds()) {
                    ItemStack item = inventory.getItem(slotId);

                    if (!slot.isQuick() && !slot.isCup(item) && slot.isDrop()
                            && (!CustomItem.isCustomItem(item) || ItemManager.getCustomItem(item).isDrop())) {
                        drops.add(inventory.getItem(slotId));
                        inventory.setItem(slotId, slot.getCup());
                    }
                }
            }
        }

        // Saving shield
        Slot shieldSlot = SlotManager.getSlotManager().getShieldSlot();
        if (shieldSlot != null && (saveItems || !shieldSlot.isDrop())) {
            ItemStack itemInOffHand = player.getEquipment().getItemInOffHand();
            if (!ItemUtils.isEmpty(itemInOffHand)) {
                EXTRA.put(player.getUniqueId(), itemInOffHand);
                drops.remove(itemInOffHand);
            }
        }

        // Saving inventory
        for (ItemStack drop : new ArrayList<>(drops)) {
            if (saveItems || !CustomItem.isCustomItem(drop) || !ItemManager.getCustomItem(drop).isDrop()) {
                drops.remove(drop);
            } else {
                for (int i = 9; i < contents.length; i++) {
                    if (drop == contents[i]) {
                        contents[i] = null;
                        break;
                    }
                }
            }
        }
        INVENTORIES.put(player.getUniqueId(), contents);
    }

    public static void restore(Player player) {
        // Restoring armor
        if (ARMORS.containsKey(player.getUniqueId())) {
            player.getInventory().setArmorContents(ARMORS.get(player.getUniqueId()));
            ARMORS.remove(player.getUniqueId());
        }

        // Restoring inventory
        if (INVENTORIES.containsKey(player.getUniqueId())) {
            player.getInventory().setStorageContents(INVENTORIES.get(player.getUniqueId()));
            INVENTORIES.remove(player.getUniqueId());
        }

        // Restoring extra slots
        if (EXTRA.containsKey(player.getUniqueId())) {
            player.getInventory().setItemInOffHand(EXTRA.get(player.getUniqueId()));
            EXTRA.remove(player.getUniqueId());
        }
    }
}
