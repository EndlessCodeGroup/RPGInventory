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
import ru.endlesscode.inspector.report.DiscordReporter;
import ru.endlesscode.inspector.report.Reporter;

@SuppressWarnings("unused")
public class RPGInventoryPlugin extends TrackedPlugin {

    public RPGInventoryPlugin() {
        super(RPGInventory.class);
    }

    @Override
    protected final @NotNull Reporter createReporter() {
        String id = "460142275171975168";
        String token = "yEl4EUYWB5yuraU0IgriK92NyUC5NSPtoBIfUAdRFbupSBE5wqODxc8vRJ7Fo6Sr_B6Y";

        return new DiscordReporter.Builder()
                .hook(id, token)
                .focusOn(this)
                .build();
    }
}
