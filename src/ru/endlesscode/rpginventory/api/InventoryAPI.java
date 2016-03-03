package ru.endlesscode.rpginventory.api;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import ru.endlesscode.rpginventory.inventory.InventoryManager;
import ru.endlesscode.rpginventory.inventory.InventoryWrapper;
import ru.endlesscode.rpginventory.inventory.slot.Slot;
import ru.endlesscode.rpginventory.inventory.slot.SlotManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by OsipXD on 03.09.2015
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2015 © «EndlessCode Group»
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class InventoryAPI {
    /**
     * Checks if opened inventory is RPGInventory
     *
     * @param inventory - opened inventory
     * @return true - if opened RPGInventory, false - otherwise
     */
    public static boolean isRPGInventory(@NotNull Inventory inventory) {
        return inventory.getHolder() instanceof InventoryWrapper;
    }

    /**
     * Get all passive item from RPGInventory of specific player
     *
     * @param player - the player
     * @return List of not null passive item
     */
    @NotNull
    public static List<ItemStack> getPassiveItems(@NotNull Player player) {
        List<ItemStack> passiveItems = new ArrayList<>();

        if (!InventoryManager.playerIsLoaded(player)) {
            return passiveItems;
        }

        Inventory inventory = InventoryManager.get(player).getInventory();
        for (Slot slot : SlotManager.getSlotManager().getPassiveSlots()) {
            for (int slotId : slot.getSlotIds()) {
                ItemStack item = inventory.getItem(slotId);
                if (item != null && item.getType() != Material.AIR && !InventoryManager.isEmptySlot(item)) {
                    passiveItems.add(item);
                }
            }
        }

        return passiveItems;
    }

    /**
     * Get all active item from RPGInventory of specific player
     *
     * @param player - the player
     * @return List of not null active item
     */
    @NotNull
    public static List<ItemStack> getActiveItems(@NotNull Player player) {
        List<ItemStack> activeItems = new ArrayList<>();

        if (!InventoryManager.playerIsLoaded(player)) {
            return activeItems;
        }

        Inventory inventory = InventoryManager.get(player).getInventory();
        for (Slot slot : SlotManager.getSlotManager().getActiveSlots()) {
            ItemStack item = inventory.getItem(slot.getSlotId());

            if (item != null && item.getType() != Material.AIR && !InventoryManager.isQuickEmptySlot(item)) {
                activeItems.add(item);
            }
        }

        return activeItems;
    }
}
