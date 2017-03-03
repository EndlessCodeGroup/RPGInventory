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

package ru.endlesscode.rpginventory.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Created by OsipXD on 16.11.2015
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
public class CommandUtils {
    /**
     * Execute command from player to server
     *
     * @param player    The player
     * @param command   The command
     * @param runFromOp If true, command will be run from OP
     */
    public static void sendCommand(Player player, String command, boolean runFromOp) {
        command = StringUtils.setPlaceholders(player, command);

        if (runFromOp) {
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
        } else {
            player.performCommand(command);
        }
    }
}