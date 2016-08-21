package ru.endlesscode.rpginventory.inventory.slot;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

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
            player.openWorkbench(null, true);
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
