package ru.endlesscode.rpginventory.inventory.chest;

import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.endlesscode.rpginventory.RPGInventory;
import ru.endlesscode.rpginventory.misc.Config;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by OsipXD on 01.09.2015
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
public class ChestManager {
    public static final int PREV = 6;
    public static final int NONE = 7;
    public static final int NEXT = 8;

    private static final Map<UUID, ChestWrapper> CHEST_LIST = new HashMap<>();
    private static ItemStack capSlot;

    private ChestManager() {
    }

    public static void init() {
        // Setup cap slot
        capSlot = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 8);
        ItemMeta meta = capSlot.getItemMeta();
        meta.setDisplayName(RPGInventory.getLanguage().getCaption("chest.cap.name"));
        meta.setLore(Collections.singletonList(RPGInventory.getLanguage().getCaption("chest.cap.lore")));
        capSlot.setItemMeta(meta);
    }

    public static boolean validateContainer(InventoryType type) {
        return !Config.getConfig().getStringList("containers.list").contains(type.name());
    }

    public static void add(HumanEntity player, ChestWrapper chest) {
        CHEST_LIST.put(player.getUniqueId(), chest);
    }

    public static void remove(HumanEntity player) {
        CHEST_LIST.remove(player.getUniqueId());
    }

    public static boolean chestOpened(HumanEntity player) {
        return CHEST_LIST.containsKey(player.getUniqueId());
    }

    public static ChestWrapper getChest(HumanEntity player) {
        return ChestManager.CHEST_LIST.get(player.getUniqueId());
    }

    public static boolean isCapSlot(ItemStack itemStack) {
        return capSlot.equals(itemStack);
    }

    public static ItemStack getCapSlot() {
        return capSlot;
    }
}
