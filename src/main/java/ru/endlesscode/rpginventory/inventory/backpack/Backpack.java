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

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import ru.endlesscode.rpginventory.inventory.InventoryManager;
import ru.endlesscode.rpginventory.misc.Config;

import java.util.UUID;

/**
 * Created by OsipXD on 19.10.2015
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
public class Backpack {
    private final UUID id;
    private final BackpackType backpackType;

    private long lastUse;
    private ItemStack[] contents;

    public Backpack(BackpackType backpackType) {
        this(backpackType, UUID.randomUUID());
    }

    public Backpack(BackpackType backpackType, UUID uuid) {
        this.id = uuid;
        this.backpackType = backpackType;
        this.contents = new ItemStack[backpackType.getSize()];
    }

    UUID getUniqueId() {
        return this.id;
    }

    public BackpackType getType() {
        return backpackType;
    }

    void open(Player player) {
        int realSize = (int) Math.ceil(this.backpackType.getSize() / 9.0) * 9;
        BackpackHolder holder = new BackpackHolder();
        Inventory inventory = Bukkit.createInventory(holder, realSize, backpackType.getTitle());
        holder.setInventory(inventory);

        for (int i = 0; i < realSize; i++) {
            if (i < this.backpackType.getSize()) {
                if (this.contents[i] != null) {
                    inventory.setItem(i, this.contents[i]);
                }
            } else {
                inventory.setItem(i, InventoryManager.getFillSlot());
            }
        }

        player.openInventory(inventory);
        InventoryManager.get(player).setBackpack(this);
    }

    ItemStack[] getContents() {
        return contents;
    }

    void setContents(ItemStack[] contents) {
        this.contents = contents;
    }

    public void onUse() {
        this.lastUse = System.currentTimeMillis();
    }

    long getLastUse() {
        return this.lastUse;
    }

    void setLastUse(long lastUse) {
        this.lastUse = lastUse;
    }

    boolean isOverdue() {
        int lifeTime = Config.getConfig().getInt("backpacks.expiration-time", 0);
        return lifeTime != 0 && (System.currentTimeMillis() - this.lastUse) / (1_000 * 60 * 60 * 24) > lifeTime;
    }
}
