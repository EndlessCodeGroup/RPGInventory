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

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Created by OsipXD on 27.08.2016
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
public class ClassedItem extends TexturedItem {
    private final int level;
    private final List<String> classes;

    protected ClassedItem(@NotNull ConfigurationSection config, String texture) {
        super(texture);

        this.level = config.getInt("level", -1);
        this.classes = config.contains("classes") ? config.getStringList("classes") : null;
    }

    public int getLevel() {
        return this.level;
    }

    @Nullable
    protected List<String> getClasses() {
        return this.classes;
    }

    protected String getClassesString() {
        StringBuilder classesString = new StringBuilder();
        for (String theClass : this.classes) {
            if (classesString.length() > 0) {
                classesString.append(", ");
            }

            classesString.append(theClass);
        }

        return classesString.toString();
    }
}
