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

package ru.endlesscode.rpginventory.inventory;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import ru.endlesscode.rpginventory.RPGInventory;
import ru.endlesscode.rpginventory.event.PetUnequipEvent;
import ru.endlesscode.rpginventory.inventory.slot.Slot;
import ru.endlesscode.rpginventory.inventory.slot.SlotManager;
import ru.endlesscode.rpginventory.item.CustomItem;
import ru.endlesscode.rpginventory.item.ItemManager;
import ru.endlesscode.rpginventory.pet.PetManager;
import ru.endlesscode.rpginventory.pet.PetType;
import ru.endlesscode.rpginventory.utils.ItemUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by OsipXD on 06.10.2016
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
public class InventorySaver {
    private static final Map<UUID, ItemStack[]> INVENTORIES = new HashMap<>();
    private static final Map<UUID, ItemStack[]> ARMORS = new HashMap<>();
    private static final Map<UUID, ItemStack> EXTRA = new HashMap<>();

    public static void save(@NotNull Player player, @NotNull List<ItemStack> drops, boolean saveItems, boolean saveArmor, boolean saveRpgInv) {
        PlayerWrapper playerWrapper = InventoryManager.get(player);
        Inventory inventory = playerWrapper.getInventory();
        InventoryManager.syncArmor(playerWrapper);
        InventoryManager.syncQuickSlots(playerWrapper);
        InventoryManager.syncShieldSlot(playerWrapper);

        // Save armor
        List<ItemStack> armorList = new ArrayList<>(4);
        List<Slot> armorSlots = SlotManager.instance().getArmorSlots();

        for (ItemStack armor : player.getInventory().getArmorContents()) {
            CustomItem armorItem = ItemManager.getCustomItem(armor);

            if (saveArmor || armorItem != null && !armorItem.isDrop()) {
                armorList.add(armor);
                drops.remove(armor);
            } else {
                boolean drop = true;
                if (armor != null) {
                    for (Slot slot : new ArrayList<>(armorSlots)) {
                        if (armor.getType() == (inventory.getItem(slot.getSlotId())).getType()) {
                            drop = slot.isDrop();
                            armorSlots.remove(slot);
                            break;
                        }
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
        ARMORS.put(player.getUniqueId(), armorList.toArray(new ItemStack[0]));

        List<ItemStack> contents = Arrays.asList(player.getInventory().getStorageContents());
        for (int i = 0; i < contents.size(); i++) {
            ItemStack item = contents.get(i);
            if (!ItemUtils.isEmpty(item) && !drops.contains(item)) {
                contents.set(i, null);
            }
        }

        // Saving RPG inventory
        List<ItemStack> additionalDrops = new ArrayList<>();
        if (!saveRpgInv) {
            // Save quick slots
            for (Slot slot : SlotManager.instance().getQuickSlots()) {
                ItemStack quickItem = player.getInventory().getItem(slot.getQuickSlot());

                if (!ItemUtils.isEmpty(quickItem) && !slot.isCup(quickItem)) {
                    if (slot.isDrop()) {
                        additionalDrops.add(quickItem);
                        contents.set(slot.getQuickSlot(), null);
                    }

                    drops.remove(quickItem);
                }
            }

            // Save pet
            int petSlotId = PetManager.getPetSlotId();
            if (PetManager.isEnabled() && inventory.getItem(petSlotId) != null) {
                Slot petSlot = SlotManager.instance().getPetSlot();
                ItemStack currentPetItem = inventory.getItem(petSlotId);

                if (petSlot != null && !petSlot.isCup(currentPetItem)) {
                    ItemStack petItem = PetType.clone(currentPetItem);
                    if (petSlot.isDrop()) {
                        additionalDrops.add(petItem);
                        RPGInventory.getInstance().getServer().getPluginManager().callEvent(new PetUnequipEvent(player));
                        inventory.setItem(petSlotId, petSlot.getCup());
                    } else {
                        inventory.setItem(petSlotId, petItem);
                    }
                }
            }

            for (Slot slot : SlotManager.instance().getPassiveSlots()) {
                for (int slotId : slot.getSlotIds()) {
                    ItemStack item = inventory.getItem(slotId);
                    CustomItem customItem = ItemManager.getCustomItem(item);

                    if (!slot.isQuick() && !slot.isCup(item) && slot.isDrop()
                            && (customItem == null || customItem.isDrop())) {
                        additionalDrops.add(inventory.getItem(slotId));
                        inventory.setItem(slotId, slot.getCup());
                    }
                }
            }
        }

        // Saving inventory
        for (ItemStack drop : new ArrayList<>(drops)) {
            CustomItem customItem = ItemManager.getCustomItem(drop);
            if (saveItems || customItem != null && customItem.isDrop()) {
                drops.remove(drop);
            } else {
                contents.replaceAll(itemStack -> drop.equals(itemStack) ? null : itemStack);
            }
        }
        INVENTORIES.put(player.getUniqueId(), contents.toArray(new ItemStack[0]));

        // Saving shield
        Slot shieldSlot = SlotManager.instance().getShieldSlot();
        if (shieldSlot != null && (saveItems || !shieldSlot.isDrop())) {
            ItemStack itemInOffHand = player.getEquipment().getItemInOffHand();
            if (!ItemUtils.isEmpty(itemInOffHand)) {
                EXTRA.put(player.getUniqueId(), itemInOffHand);
                drops.remove(itemInOffHand);
            }
        }

        // Add drop
        drops.addAll(additionalDrops);
    }

    public static void restore(Player player) {
        // Restoring armor
        if (ARMORS.containsKey(player.getUniqueId())) {
            player.getInventory().setArmorContents(ARMORS.get(player.getUniqueId()));
            ARMORS.remove(player.getUniqueId());
        }

        // Restoring inventory
        if (INVENTORIES.containsKey(player.getUniqueId())) {
            Inventory inventory = player.getInventory();
            ItemStack[] contents = INVENTORIES.get(player.getUniqueId());
            for (int i = 0; i < contents.length; i++) {
                ItemStack item = contents[i];

                if (!ItemUtils.isEmpty(item)) {
                    inventory.setItem(i, item);
                }
            }

            INVENTORIES.remove(player.getUniqueId());
        }

        // Restoring extra slots
        if (EXTRA.containsKey(player.getUniqueId())) {
            player.getInventory().setItemInOffHand(EXTRA.get(player.getUniqueId()));
            EXTRA.remove(player.getUniqueId());
        }
    }
}
