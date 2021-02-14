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
import ru.endlesscode.rpginventory.utils.Version;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by OsipXD on 29.08.2015
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
public class VersionHandler {

    public static final int VERSION_1_11 = 1_11_00;
    public static final int VERSION_1_12 = 1_12_00;
    public static final int VERSION_1_13 = 1_13_00;
    public static final int VERSION_1_14 = 1_14_00;
    public static final int VERSION_1_15 = 1_15_00;
    public static final int VERSION_1_16 = 1_16_00;
    public static final int VERSION_1_17 = 1_17_00;

    private static final Pattern pattern = Pattern.compile("(?<version>\\d\\.\\d{1,2}(\\.\\d)?)-.*");

    private static int versionCode = -1;

    public static boolean isNotSupportedVersion() {
        return getVersionCode() < VERSION_1_14 || getVersionCode() >= VERSION_1_17;
    }

    public static boolean isExperimentalSupport() {
        return false;
    }

    public static boolean isLegacy() {
        return getVersionCode() < VERSION_1_13;
    }

    public static int getVersionCode() {
        if (versionCode == -1) {
            initVersionCode();
        }

        return versionCode;
    }

    private static void initVersionCode() {
        Matcher matcher = pattern.matcher(Bukkit.getBukkitVersion());
        if (matcher.find()) {
            String versionString = matcher.group("version");
            versionCode = Version.parseVersion(versionString).getVersionCode();
        } else {
            versionCode = 0;
        }
    }
}
