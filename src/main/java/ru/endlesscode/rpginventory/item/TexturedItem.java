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

package ru.endlesscode.rpginventory.item;

/**
 * Created by OsipXD on 28.08.2016
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
public class TexturedItem {
    protected final String texture;

    protected TexturedItem(String texture) {
        this.texture = texture;
    }

    public short getTextureDurability() {
        String[] data = texture.split(":");

        if (data.length == 2) {
            try {
                return Short.parseShort(data[1]);
            } catch (NumberFormatException e) {
                return -1;
            }
        }

        return -1;
    }
}
