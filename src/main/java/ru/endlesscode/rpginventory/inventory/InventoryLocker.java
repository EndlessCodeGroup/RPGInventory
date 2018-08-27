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

import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.endlesscode.rpginventory.RPGInventory;
import ru.endlesscode.rpginventory.event.listener.LockerListener;
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
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
public class InventoryLocker {

    private static ItemStack LOCKED_SLOT = null;
    private static ItemStack BUYABLE_SLOT = null;

    private InventoryLocker() {
    }

    public static boolean init(@NotNull RPGInventory instance) {
        if (!isEnabled()) {
            return false;
        }

        try {
            // Setup locked slot
            InventoryLocker.LOCKED_SLOT = ItemUtils.getTexturedItem(Config.getConfig().getString("slots.locked"));
            ItemMeta meta = InventoryLocker.LOCKED_SLOT.getItemMeta();
            meta.setDisplayName(RPGInventory.getLanguage().getMessage("locked.name"));
            meta.setLore(Collections.singletonList(RPGInventory.getLanguage().getMessage("locked.lore")));

            InventoryLocker.LOCKED_SLOT.setItemMeta(meta);
            InventoryLocker.LOCKED_SLOT = addId(InventoryLocker.LOCKED_SLOT);

            // Setup buyable slot
            InventoryLocker.BUYABLE_SLOT = ItemUtils.getTexturedItem(Config.getConfig().getString("slots.buyable"));
            meta = InventoryLocker.BUYABLE_SLOT.getItemMeta();
            meta.setDisplayName(RPGInventory.getLanguage().getMessage("buyable.name"));
            meta.setLore(Collections.singletonList(RPGInventory.getLanguage().getMessage("buyable.lore")));

            InventoryLocker.BUYABLE_SLOT.setItemMeta(meta);
            InventoryLocker.BUYABLE_SLOT = addId(InventoryLocker.BUYABLE_SLOT);
        } catch (Exception e) {
            instance.getReporter().report("Error on InventoryLocker initialization", e);
            return false;
        }

        instance.getServer().getPluginManager().registerEvents(new LockerListener(), instance);
        return true;
    }

    private static boolean isEnabled() {
        return Config.getConfig().getBoolean("slots.enabled");
    }

    public static boolean buySlot(@NotNull Player player, int line) {
        final FileConfiguration config = Config.getConfig();
        if (config.getBoolean("slots.money.enabled")) {
            if (!RPGInventory.economyConnected()) {
                return false;
            }
            final EconomyResponse economyResponse = RPGInventory.getEconomy().withdrawPlayer(
                    player, config.getDouble("slots.money.cost.line" + line)
            );
            if (!economyResponse.transactionSuccess()) {
                return false;
            }
        }
        if (config.getBoolean("slots.level.enabled") && config.getBoolean("slots.level.spend")) {
            if (RPGInventory.getLevelSystem() == PlayerUtils.LevelSystem.EXP) {
                final int level = player.getLevel() - config.getInt("slots.level.required.line" + line);
                if (0 > level) {
                    return false;
                }
                player.setLevel(level);
            }
        }
        return true;
    }

    @Contract(pure = true)
    public static int getLine(int slot) {
        return (slot - 9) / 9 + 1;
    }

    @NotNull
    public static ItemStack getBuyableSlotForLine(int line) {
        ItemStack slot = InventoryLocker.BUYABLE_SLOT.clone();
        ItemMeta im = slot.getItemMeta();
        List<String> lore = im.getLore();
        FileLanguage lang = RPGInventory.getLanguage();
        final FileConfiguration config = Config.getConfig();
        if (config.getBoolean("slots.money.enabled")) {
            lore.add(lang.getMessage("buyable.money", StringUtils.doubleToString(config.getDouble("slots.money.cost.line" + line))));
        }

        if (config.getBoolean("slots.level.enabled")) {
            lore.add(lang.getMessage("buyable.level", config.getInt("slots.level.required.line" + line)));
        }
        im.setLore(lore);
        slot.setItemMeta(im);

        return addId(slot);
    }

    @NotNull
    private static ItemStack addId(ItemStack item) {
        return ItemUtils.setTag(item, "locked", "0");
    }

    public static boolean isLockedSlot(@Nullable ItemStack item) {
        return InventoryLocker.isEnabled() && !ItemUtils.isEmpty(item) && ItemUtils.hasTag(item, "locked");
    }

    public static boolean isBuyableSlot(ItemStack currentItem, int line) {
        return InventoryLocker.getBuyableSlotForLine(line).equals(currentItem);
    }

    public static void lockSlots(@NotNull Player player) {
        InventoryLocker.lockSlots(player, false);
    }

    public static void lockSlots(@NotNull Player player, boolean force) {
        if (!force && player.getGameMode() == GameMode.CREATIVE) {
            return;
        }

        if (InventoryLocker.isEnabled()) {
            int maxSlot = getSlots(player) + 8;
            for (int i = 35; i > maxSlot; i--) {
                player.getInventory().setItem(i, InventoryLocker.LOCKED_SLOT);
            }

            if (maxSlot < 35) {
                player.getInventory().setItem(maxSlot + 1, getBuyableSlotForLine(getLine(maxSlot + 1)));
            }
        }

        InventoryManager.lockQuickSlots(player);
        InventoryManager.lockEmptySlots(player);
    }

    public static void unlockSlots(@NotNull Player player) {
        if (InventoryLocker.isEnabled()) {
            for (int i = 8 + getSlots(player); i < 36; i++) {
                ItemStack itemStack = player.getInventory().getItem(i);
                if (InventoryLocker.isLockedSlot(itemStack)) {
                    player.getInventory().setItem(i, null);
                }
            }
        }

        InventoryManager.unlockQuickSlots(player);
        InventoryManager.unlockEmptySlots(player);
    }

    public static boolean canBuySlot(@NotNull Player player, int line) {
        final FileConfiguration config = Config.getConfig();
        if (config.getBoolean("slots.money.enabled")) {
            double cost = config.getDouble("slots.money.cost.line" + line);

            if (!PlayerUtils.checkMoney(player, cost)) {
                return false;
            }
        }

        if (config.getBoolean("slots.level.enabled")) {
            int requirement = config.getInt("slots.level.required.line" + line);

            if (!PlayerUtils.checkLevel(player, requirement)) {
                PlayerUtils.sendMessage(player, RPGInventory.getLanguage().getMessage("error.level", requirement));
                return false;
            }
        }

        return true;
    }

    private static int getSlots(OfflinePlayer player) {
        int slots = Config.getConfig().getInt("slots.free") + InventoryManager.get(player).getBuyedGenericSlots();
        return slots > 27 ? 27 : slots;
    }
}
