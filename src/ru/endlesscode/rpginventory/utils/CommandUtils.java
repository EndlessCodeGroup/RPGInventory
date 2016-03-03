package ru.endlesscode.rpginventory.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Created by OsipXD on 16.11.2015
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2015 © «EndlessCode Group»
 */
public class CommandUtils {
    /**
     * Execute command from player to server
     *
     * @param player    The player
     * @param command   The command
     * @param runFromOp If true, command will be run from OP
     * @return True, if command was successfully executed, in other case - false
     */
    public static boolean sendCommand(Player player, String command, boolean runFromOp) {
        command = command.replaceAll("%WORLD%", player.getWorld().getName());
        command = command.replaceAll("%PLAYER%", player.getName());

        if (runFromOp) {
            return Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
        }

        return player.performCommand(command);
    }
}