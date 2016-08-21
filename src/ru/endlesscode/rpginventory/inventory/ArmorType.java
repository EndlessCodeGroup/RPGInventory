package ru.endlesscode.rpginventory.inventory;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import ru.endlesscode.rpginventory.nms.VersionHandler;
import ru.endlesscode.rpginventory.utils.ItemUtils;

/**
 * Created by OsipXD on 08.04.2016
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
public enum ArmorType {
    HELMET(5),
    CHESTPLATE(6),
    LEGGINGS(7),
    BOOTS(8),
    UNKNOWN(-1);

    private final int slot;

    ArmorType(int slot) {
        this.slot = slot;
    }

    public static ArmorType matchType(ItemStack item) {
        if (ItemUtils.isEmpty(item)) {
            return UNKNOWN;
        }

        if (VersionHandler.isHigher1_9() && item.getType() == Material.ELYTRA) {
            return CHESTPLATE;
        }

        switch (item.getType()) {
            case LEATHER_HELMET:
            case CHAINMAIL_HELMET:
            case IRON_HELMET:
            case GOLD_HELMET:
            case DIAMOND_HELMET:
                return HELMET;
            case LEATHER_CHESTPLATE:
            case CHAINMAIL_CHESTPLATE:
            case IRON_CHESTPLATE:
            case GOLD_CHESTPLATE:
            case DIAMOND_CHESTPLATE:
                return CHESTPLATE;
            case LEATHER_LEGGINGS:
            case CHAINMAIL_LEGGINGS:
            case IRON_LEGGINGS:
            case GOLD_LEGGINGS:
            case DIAMOND_LEGGINGS:
                return LEGGINGS;
            case LEATHER_BOOTS:
            case CHAINMAIL_BOOTS:
            case IRON_BOOTS:
            case GOLD_BOOTS:
            case DIAMOND_BOOTS:
                return BOOTS;
        }

        return UNKNOWN;
    }

    @Nullable
    public ItemStack getItem(Player player) {
        switch (this) {
            case HELMET:
                return player.getEquipment().getHelmet();
            case CHESTPLATE:
                return player.getEquipment().getChestplate();
            case LEGGINGS:
                return player.getEquipment().getLeggings();
            case BOOTS:
                return player.getEquipment().getBoots();
            default:
                return null;
        }
    }

    public int getSlot() {
        return slot;
    }
}
