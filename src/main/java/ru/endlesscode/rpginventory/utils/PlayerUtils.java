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

package ru.endlesscode.rpginventory.utils;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.endlesscode.inspector.bukkit.scheduler.TrackedBukkitRunnable;
import ru.endlesscode.mimic.classes.BukkitClassSystem;
import ru.endlesscode.mimic.level.BukkitLevelSystem;
import ru.endlesscode.rpginventory.RPGInventory;
import ru.endlesscode.rpginventory.inventory.InventoryManager;
import ru.endlesscode.rpginventory.inventory.PlayerWrapper;

import java.util.List;

/**
 * Created by OsipXD on 09.11.2015
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
public class PlayerUtils {
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean checkLevel(@NotNull Player player, int required) {
        BukkitLevelSystem levelSystem = RPGInventory.getLevelSystem(player);
        return levelSystem.didReachLevel(required);
    }

    public static boolean checkClass(@NotNull Player player, @NotNull List<String> classes) {
        BukkitClassSystem classSystem = RPGInventory.getClassSystem(player);
        return classSystem.hasAnyOfClasses(classes);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean checkMoney(@NotNull Player player, double cost) {
        double balance = (RPGInventory.economyConnected() ? RPGInventory.getEconomy().getBalance(player) : 0);
        if (balance < cost) {
            PlayerUtils.sendMessage(player, RPGInventory.getLanguage().getMessage("error.money", StringUtils.doubleToString(cost - balance)));
            return false;
        }

        return true;
    }

    public static void updateInventory(@NotNull final Player player) {
        new TrackedBukkitRunnable() {
            @Override
            public void run() {
                player.updateInventory();
            }
        }.runTaskLater(RPGInventory.getInstance(), 1);
    }

    public static void sendMessage(@NotNull Player player, String message) {
        if (InventoryManager.playerIsLoaded(player)) {
            PlayerWrapper wrapper = InventoryManager.get(player);

            if (wrapper.getLastMessage().equals(message)) {
                return;
            }

            wrapper.setLastMessage(message);
        }

        player.sendMessage(message);
    }
}
