package ru.endlesscode.rpginventory.utils;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.endlesscode.rpginventory.inventory.InventoryManager;
import ru.endlesscode.rpginventory.inventory.slot.Slot;

/**
 * Created by OsipXD on 22.08.2015.
 * It is part of the RpgInventory.
 * Copyright © 2015 «EndlessCode Group»
 */
public class InventoryUtils {
    @NotNull
    @Contract(pure = true)
    public static ActionType getTypeOfAction(InventoryAction action) {
        if (action == InventoryAction.PLACE_ALL || action == InventoryAction.PLACE_ONE
                || action == InventoryAction.PLACE_SOME || action == InventoryAction.SWAP_WITH_CURSOR) {
            return ActionType.SET;
        }

        if (action == InventoryAction.MOVE_TO_OTHER_INVENTORY || action == InventoryAction.PICKUP_ALL
                || action == InventoryAction.PICKUP_ONE || action == InventoryAction.PICKUP_SOME
                || action == InventoryAction.PICKUP_HALF) {
            return ActionType.GET;
        }

        if (action == InventoryAction.DROP_ALL_CURSOR || action == InventoryAction.DROP_ALL_SLOT
                || action == InventoryAction.DROP_ONE_CURSOR || action == InventoryAction.DROP_ONE_SLOT) {
            return ActionType.DROP;
        }

        return ActionType.OTHER;
    }

    public static int countEmptySlots(@NotNull Inventory inventory) {
        int emptySlots = 0;
        for (ItemStack itemStack : inventory.getContents()) {
            if (itemStack == null || itemStack.getType() == Material.AIR) {
                emptySlots++;
            }
        }

        return emptySlots;
    }

    public static void heldFreeSlot(@NotNull Player player, int start, SearchType type) {
        if (type == SearchType.NEXT) {
            for (int i = start + 1; i < start + 9; i++) {
                int index = i % 9;
                if (!InventoryManager.isQuickEmptySlot(player.getInventory().getItem(index))) {
                    player.getInventory().setHeldItemSlot(index);
                    return;
                }
            }
        } else {
            for (int i = start - 1; i > start - 9; i--) {
                int index = (i + 9) % 9;
                if (!InventoryManager.isQuickEmptySlot(player.getInventory().getItem(index))) {
                    player.getInventory().setHeldItemSlot(index);
                    return;
                }
            }
        }
    }

    public static int getArmorSlot(Slot slot) {
        switch (slot.getName()) {
            case "helmet":
                return 5;
            case "chestplate":
                return 6;
            case "leggings":
                return 7;
            default:
                return 8;
        }
    }

    public enum SearchType {
        NEXT,
        PREV
    }

    public enum ActionType {
        SET,
        GET,
        DROP,
        OTHER
    }
}
