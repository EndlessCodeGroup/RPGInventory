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

package ru.endlesscode.rpginventory.compat;

import org.bukkit.Bukkit;

/**
 * Created by OsipXD on 29.08.2015
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
public class VersionHandler {

    // 1.9.x
    private static boolean is1_9() {
        return Bukkit.getBukkitVersion().contains("1.9");
    }

    // 1.10.x
    private static boolean is1_10() {
        return Bukkit.getBukkitVersion().contains("1.10");
    }

    // 1.11.x
    private static boolean is1_11() {
        return Bukkit.getBukkitVersion().contains("1.11");
    }

    // 1.12.x
    private static boolean is1_12() {
        return Bukkit.getBukkitVersion().contains("1.12");
    }

    // 1.13.x
    public static boolean is1_13() {
        return Bukkit.getBukkitVersion().contains("1.13");
    }

    public static boolean checkVersion() {
        return is1_9() || is1_10() || is1_11() || is1_12() || is1_13();
    }

    public static boolean isUpper1_12() {
        return !is1_9() && !is1_10() && !is1_11();
    }
}
