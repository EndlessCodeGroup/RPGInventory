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

package ru.endlesscode.rpginventory.inventory.slot;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.endlesscode.rpginventory.inventory.InventoryManager;

/**
 * Created by OsipXD on 06.09.2015
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
public class ActionSlot extends Slot {
    private final ActionType actionType;
    private final String command;
    private final boolean isGui;

    ActionSlot(String name, @NotNull ConfigurationSection config) {
        super(name, config);
        this.actionType = ActionType.valueOf(config.getString("action"));
        this.command = config.getString("command");
        this.isGui = config.getBoolean("gui", false);
    }

    public void preformAction(@NotNull Player player) {
        if (this.isGui) {
            player.closeInventory();
        }

        if (this.actionType == ActionType.WORKBENCH) {
            InventoryManager.get(player).openWorkbench();
        } else if (this.actionType == ActionType.ENDERCHEST) {
            player.openInventory(player.getEnderChest());
        } else if (this.actionType == ActionType.COMMAND && command != null) {
            player.performCommand(command);
        }
    }

    private enum ActionType {
        WORKBENCH,
        ENDERCHEST,
        COMMAND
    }
}
