/*
 * This file is part of RPGInventory.
 * Copyright (C) 2020 EndlessCode Group and contributors
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

package ru.endlesscode.rpginventory.misc.config;

import ru.endlesscode.rpginventory.utils.SafeEnums;

@SuppressWarnings("unused")
public enum TexturesType {
    DAMAGE,
    CUSTOM_MODEL_DATA;

    static TexturesType parseString(String stringValue) {
        return SafeEnums.valueOfOrDefault(TexturesType.class, stringValue, DAMAGE, "textures type");
    }
}
