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

package ru.endlesscode.rpginventory.inventory;

import org.bukkit.event.inventory.InventoryAction;
import org.jetbrains.annotations.*;

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

    @NotNull
    @Contract(pure = true)
    public static ActionType getTypeOfAction(InventoryAction action) {
        if (action == InventoryAction.PLACE_ALL || action == InventoryAction.PLACE_ONE
                || action == InventoryAction.PLACE_SOME || action == InventoryAction.SWAP_WITH_CURSOR) {
            return SET;
        }

        if (action == InventoryAction.MOVE_TO_OTHER_INVENTORY || action == InventoryAction.PICKUP_ALL
                || action == InventoryAction.PICKUP_ONE || action == InventoryAction.PICKUP_SOME
                || action == InventoryAction.PICKUP_HALF || action == InventoryAction.COLLECT_TO_CURSOR) {
            return GET;
        }

        if (action == InventoryAction.DROP_ALL_CURSOR || action == InventoryAction.DROP_ALL_SLOT
                || action == InventoryAction.DROP_ONE_CURSOR || action == InventoryAction.DROP_ONE_SLOT) {
            return DROP;
        }

        return OTHER;
    }
}
