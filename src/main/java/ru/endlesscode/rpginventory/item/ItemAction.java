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
import org.bukkit.entity.Player;
import ru.endlesscode.rpginventory.utils.CommandUtils;
import ru.endlesscode.rpginventory.utils.PlayerUtils;
import ru.endlesscode.rpginventory.utils.StringUtils;

/**
 * Created by OsipXD on 12.04.2016
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
class ItemAction {
    private final String command;
    private final String caption;
    private final String message;
    private final boolean asOp;

    ItemAction(ConfigurationSection config) {
        this.command = config.getString("command");
        this.caption = config.getString("lore");
        this.message = config.getString("message");
        this.asOp = config.getBoolean("op", false);
    }

    void doAction(Player player) {
        CommandUtils.sendCommand(player, command, asOp);
        if (message != null) {
            PlayerUtils.sendMessage(player, StringUtils.coloredLine(message));
        }
    }

    public String getCaption() {
        return caption;
    }
}
