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
        command = StringUtils.applyPlaceHolders(command, player);

        if (runFromOp) {
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
        } else {
            player.performCommand(command);
        }
    }
}