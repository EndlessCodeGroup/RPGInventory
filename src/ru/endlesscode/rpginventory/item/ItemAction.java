package ru.endlesscode.rpginventory.item;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
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

    ItemAction(@NotNull ConfigurationSection config) {
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
