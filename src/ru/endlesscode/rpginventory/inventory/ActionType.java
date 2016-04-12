package ru.endlesscode.rpginventory.inventory;

import org.bukkit.event.inventory.InventoryAction;
import org.jetbrains.annotations.Contract;

/**
 * Created by OsipXD on 09.04.2016
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
public enum ActionType {
    SET,
    GET,
    DROP,
    OTHER;

    @Contract(pure = true)
    public static ActionType getTypeOfAction(InventoryAction action) {
        if (action == InventoryAction.PLACE_ALL || action == InventoryAction.PLACE_ONE
                || action == InventoryAction.PLACE_SOME || action == InventoryAction.SWAP_WITH_CURSOR) {
            return SET;
        }

        if (action == InventoryAction.MOVE_TO_OTHER_INVENTORY || action == InventoryAction.PICKUP_ALL
                || action == InventoryAction.PICKUP_ONE || action == InventoryAction.PICKUP_SOME
                || action == InventoryAction.PICKUP_HALF) {
            return GET;
        }

        if (action == InventoryAction.DROP_ALL_CURSOR || action == InventoryAction.DROP_ALL_SLOT
                || action == InventoryAction.DROP_ONE_CURSOR || action == InventoryAction.DROP_ONE_SLOT) {
            return DROP;
        }

        return OTHER;
    }
}
