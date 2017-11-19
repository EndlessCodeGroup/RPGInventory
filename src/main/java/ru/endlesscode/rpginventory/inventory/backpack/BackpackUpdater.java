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

package ru.endlesscode.rpginventory.inventory.backpack;

import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;

import ru.endlesscode.rpginventory.RPGInventory;

/**
 * Created by OsipXD on 26.08.2016
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
public class BackpackUpdater extends BukkitRunnable {
    private final Inventory inventory;
    private final Backpack backpack;

    private static final int DELAY = 2;

    private BackpackUpdater(Inventory inventory, Backpack backpack) {
        this.inventory = inventory;
        this.backpack = backpack;
    }

    public static void update(Inventory inventory, Backpack backpack) {
        new BackpackUpdater(inventory, backpack).runTaskLater(RPGInventory.getInstance(), DELAY);
    }

    @Override
    public void run() {
        backpack.onUse();

        int backpackSize = backpack.getType().getSize();
        backpack.setContents(Arrays.copyOfRange(inventory.getContents(), 0, backpackSize));
    }
}
