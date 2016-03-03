package ru.endlesscode.rpginventory.inventory;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.endlesscode.rpginventory.RPGInventory;
import ru.endlesscode.rpginventory.misc.Config;
import ru.endlesscode.rpginventory.misc.FileLanguage;
import ru.endlesscode.rpginventory.utils.ItemUtils;
import ru.endlesscode.rpginventory.utils.PlayerUtils;
import ru.endlesscode.rpginventory.utils.StringUtils;

import java.util.Collections;
import java.util.List;

/**
 * Created by OsipXD on 28.08.2015
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2015 © «EndlessCode Group»
 */
public class InventoryLocker {
    private static ItemStack lockedSlot = null;
    private static ItemStack buyableSlot = null;

    private InventoryLocker() {
    }

    public static void init() {
        if (!isEnabled()) {
            return;
        }

        // Setup locked slot
        InventoryLocker.lockedSlot = ItemUtils.getTexturedItem(Config.getConfig().getString("slots.locked"));
        ItemMeta meta = InventoryLocker.lockedSlot.getItemMeta();
        meta.setDisplayName(RPGInventory.getLanguage().getCaption("locked.name"));
        meta.setLore(Collections.singletonList(RPGInventory.getLanguage().getCaption("locked.lore")));

        InventoryLocker.lockedSlot.setItemMeta(meta);
        lockedSlot = addId(lockedSlot);

        // Setup buyable slot
        InventoryLocker.buyableSlot = ItemUtils.getTexturedItem(Config.getConfig().getString("slots.buyable"));
        meta = InventoryLocker.buyableSlot.getItemMeta();
        meta.setDisplayName(RPGInventory.getLanguage().getCaption("buyable.name"));
        meta.setLore(Collections.singletonList(RPGInventory.getLanguage().getCaption("buyable.lore")));

        InventoryLocker.buyableSlot.setItemMeta(meta);
        buyableSlot = addId(buyableSlot);
    }

    private static boolean isEnabled() {
        return Config.getConfig().getBoolean("slots.enabled");
    }

    public static boolean buySlot(@NotNull Player player, int line) {
        if (RPGInventory.economyConnected() && RPGInventory.getEconomy().withdrawPlayer(player, Config.getConfig().getDouble("slots.money.cost.line" + line)).transactionSuccess()) {
            if (!RPGInventory.isSkillAPIEnabled() && Config.getConfig().getBoolean("slots.level.spend")) {
                player.setLevel(player.getLevel() - Config.getConfig().getInt("slots.level.required.line" + line));
            }

            return true;
        }

        return false;
    }

    @Contract(pure = true)
    public static int getLine(int slot) {
        return (slot - 9) / 9 + 1;
    }

    @NotNull
    public static ItemStack getBuyableSlotForLine(int line) {
        ItemStack slot = buyableSlot.clone();
        ItemMeta im = slot.getItemMeta();
        List<String> lore = im.getLore();
        FileLanguage lang = RPGInventory.getLanguage();

        if (Config.getConfig().getBoolean("slots.money.enabled")) {
            lore.add(String.format(lang.getCaption("buyable.money"), StringUtils.roundDouble(Config.getConfig().getDouble("slots.money.cost.line" + line))));
        }

        if (Config.getConfig().getBoolean("slots.level.enabled")) {
            lore.add(String.format(lang.getCaption("buyable.level"), Config.getConfig().getInt("slots.level.required.line" + line)));
        }
        im.setLore(lore);
        slot.setItemMeta(im);

        return addId(slot);
    }

    @NotNull
    private static ItemStack addId(@NotNull ItemStack item) {
        return ItemUtils.setTag(item, "locked", "0");
    }

    public static boolean isLockedSlot(@Nullable ItemStack item) {
        return isEnabled() && item != null && item.getType() != Material.AIR && ItemUtils.hasTag(item, "locked");
    }

    public static boolean isBuyableSlot(ItemStack currentItem, int line) {
        return getBuyableSlotForLine(line).equals(currentItem);
    }

    public static void lockSlots(@NotNull Player player) {
        InventoryLocker.lockSlots(player, false);
    }

    public static void lockSlots(@NotNull Player player, boolean force) {
        if (!force && player.getGameMode() == GameMode.CREATIVE) {
            return;
        }

        if (isEnabled()) {
            int maxSlot = getSlots(player) + 8;
            for (int i = 35; i > maxSlot; i--) {
                player.getInventory().setItem(i, lockedSlot);
            }

            if (maxSlot < 35) {
                player.getInventory().setItem(maxSlot + 1, getBuyableSlotForLine(getLine(maxSlot + 1)));
            }
        }

        InventoryManager.lockQuickSlots(player);
        InventoryManager.lockEmptySlots(player);
    }

    public static void unlockSlots(@NotNull Player player) {
        if (isEnabled()) {
            for (int i = 8 + getSlots(player); i < 36; i++) {
                ItemStack itemStack = player.getInventory().getItem(i);
                if (isLockedSlot(itemStack)) {
                    player.getInventory().setItem(i, null);
                }
            }
        }

        InventoryManager.unlockQuickSlots(player);
        InventoryManager.unlockEmptySlots(player);
    }

    public static boolean canBuySlot(@NotNull Player player, int line) {
        if (Config.getConfig().getBoolean("slots.money.enabled")) {
            double cost = Config.getConfig().getDouble("slots.money.cost.line" + line);

            if (!PlayerUtils.checkMoney(player, cost)) {
                return false;
            }
        }

        if (Config.getConfig().getBoolean("slots.level.enabled")) {
            int requirement = Config.getConfig().getInt("slots.level.required.line" + line);

            if (!PlayerUtils.checkLevel(player, requirement)) {
                player.sendMessage(String.format(RPGInventory.getLanguage().getCaption("error.level"), requirement));
                return false;
            }
        }

        return true;
    }

    private static int getSlots(@NotNull OfflinePlayer player) {
        int slots = Config.getConfig().getInt("slots.free") + InventoryManager.get(player).getBuyedGenericSlots();
        return slots > 27 ? 27 : slots;
    }
}
