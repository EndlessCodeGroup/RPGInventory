/*
 * This file is part of RPGInventory.
 * Copyright (C) 2018 EndlessCode Group and contributors
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

package ru.endlesscode.rpginventory;

import org.jetbrains.annotations.NotNull;
import ru.endlesscode.inspector.bukkit.plugin.TrackedPlugin;
import ru.endlesscode.inspector.report.Reporter;
import ru.endlesscode.inspector.report.SentryReporter;

@SuppressWarnings("unused")
public class RPGInventoryPlugin extends TrackedPlugin {

    public RPGInventoryPlugin() {
        super(RPGInventory.class);
    }

    @Override
    protected final @NotNull Reporter createReporter() {
        String id = "1331962";
        String key = "3364f9220f04483a9903023e5b3dbaae";

        return new SentryReporter.Builder()
                .setDataSourceName(key, id)
                .focusOn(this)
                .build();
    }
}
