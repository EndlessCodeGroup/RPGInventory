package ru.endlesscode.rpginventory.utils;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.endlesscode.rpginventory.inventory.ArmorType;
import ru.endlesscode.rpginventory.inventory.InventoryManager;
import ru.endlesscode.rpginventory.inventory.slot.Slot;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by OsipXD on 22.08.2015.
 * It is part of the RpgInventory.
 * Copyright © 2015 «EndlessCode Group»
 */
public class InventoryUtils {
    public static int countEmptySlots(@NotNull Inventory inventory) {
        int emptySlots = 0;
        for (ItemStack itemStack : inventory.getContents()) {
            if (ItemUtils.isEmpty(itemStack)) {
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

    @Contract(pure = true)
    public static int getQuickSlot(int slotId) {
        return slotId % 9;
    }

    public static boolean playerNeedArmor(Player player, ArmorType armorType) {
        ItemStack armorItem = armorType.getItem(player);
        return ItemUtils.isEmpty(armorItem) && armorType != ArmorType.UNKNOWN;
    }

    public static int getArmorSlotId(Slot slot) {
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

    @Contract(pure = true)
    public static InventoryType.SlotType getSlotType(InventoryType.SlotType slotType, int rawSlot) {
        if (slotType == InventoryType.SlotType.QUICKBAR || rawSlot > 80) {
            return InventoryType.SlotType.QUICKBAR;
        }

        return slotType;
    }

    public static List<HumanEntity> getViewersOfInventory(Inventory inventory) {
        for (HumanEntity player : new ArrayList<>(inventory.getViewers())) {
            if (player.getOpenInventory().getTopInventory().getViewers() != inventory.getViewers()) {
                inventory.getViewers().remove(player);
            }
        }

        return inventory.getViewers();
    }

    public enum SearchType {
        NEXT,
        PREV
    }
}
