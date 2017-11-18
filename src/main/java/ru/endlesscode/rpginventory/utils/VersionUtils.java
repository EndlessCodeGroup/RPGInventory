/*
 * This file is part of RPGInventory.
 * Copyright (C) 2017 EndlessCode Group and contributors
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

public class VersionUtils {

    public static String trimQualifiers(String fullVersion) {
        String version = fullVersion;
        if (fullVersion.contains("-")) {
            int versionEndIndex = fullVersion.indexOf('-');
            version = fullVersion.substring(0, versionEndIndex);
        }

        return version;
    }

    public static int versionToCode(String version) {
        return Integer.parseInt(version.replaceAll("\\.", ""));
    }
}
