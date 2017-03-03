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

package ru.endlesscode.rpginventory.pet;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import ru.endlesscode.rpginventory.RPGInventory;
import ru.endlesscode.rpginventory.inventory.InventoryManager;
import ru.endlesscode.rpginventory.utils.ItemUtils;

/**
 * Created by OsipXD on 27.08.2015
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
class CooldownTimer extends BukkitRunnable {
    private final Player player;
    private final ItemStack petItem;

    public CooldownTimer(Player player, ItemStack petItem) {
        this.player = player;
        this.petItem = petItem;
    }

    @Override
    public void run() {
        if (!InventoryManager.playerIsLoaded(this.player)) {
            this.cancel();
            return;
        }

        Inventory inventory = InventoryManager.get(this.player).getInventory();
        if (!this.player.isOnline() || this.player.isDead() || !PetManager.isEnabled() || inventory == null || inventory.getItem(PetManager.getPetSlotId()) == null) {
            this.cancel();
            return;
        }

        int cooldown = PetManager.getCooldown(this.petItem);

        if (cooldown > 1) {
            ItemStack item = this.petItem.clone();
            if (cooldown < 60) {
                item.setAmount(cooldown);
            }

            ItemMeta im = item.getItemMeta();
            im.setDisplayName(this.petItem.getItemMeta().getDisplayName()
                    + RPGInventory.getLanguage().getCaption("pet.cooldown", cooldown));
            item.setItemMeta(im);
            PetManager.addGlow(item);
            PetManager.setCooldown(item, cooldown);
            ItemUtils.setTag(item, ItemUtils.PET_TAG, ItemUtils.getTag(this.petItem, ItemUtils.PET_TAG));
            inventory.setItem(PetManager.getPetSlotId(), item);
        } else {
            PetManager.setCooldown(this.petItem, 0);
            PetManager.spawnPet(this.player, this.petItem);
            inventory.setItem(PetManager.getPetSlotId(), this.petItem);
            this.cancel();
        }
    }
}