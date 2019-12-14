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
import ru.endlesscode.rpginventory.inventory.slot.Slot;
import ru.endlesscode.rpginventory.inventory.slot.SlotManager;
import ru.endlesscode.rpginventory.utils.ItemUtils;

import java.util.Objects;

/**
 * Created by OsipXD on 27.08.2015
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
@Deprecated
class CooldownTimer extends BukkitRunnable {
    private final Player player;
    private final ItemStack petItem;

    @Deprecated
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
        final boolean playerIsAlive = !this.player.isOnline() || this.player.isDead();
        final boolean playerHasNotPetItem = inventory.getItem(PetManager.getPetSlotId()) == null;
        if (playerIsAlive || !PetManager.isEnabled() || playerHasNotPetItem) {
            this.cancel();
            return;
        }

        int cooldown = PetManager.getCooldown(this.petItem);

        if (cooldown > 1) {
            ItemStack item = this.petItem.clone();
            if (cooldown < 60) {
                item.setAmount(cooldown);
            }

            ItemMeta itemMeta = item.getItemMeta();
            if (itemMeta != null) {
                itemMeta.setDisplayName(itemMeta.getDisplayName()
                        + RPGInventory.getLanguage().getMessage("pet.cooldown", cooldown));
                PetManager.addGlow(itemMeta);
                item.setItemMeta(itemMeta);
            }
            String itemTag = ItemUtils.getTag(this.petItem, ItemUtils.PET_TAG);

            if (itemTag.isEmpty()) {
                Slot petSlot = Objects.requireNonNull(SlotManager.instance().getPetSlot(), "Pet slot can't be null!");
                inventory.setItem(PetManager.getPetSlotId(), petSlot.getCup());
                this.cancel();
            } else {
                ItemUtils.setTag(item, ItemUtils.PET_TAG, itemTag);
                inventory.setItem(PetManager.getPetSlotId(), item);
            }
        } else {
            PetManager.respawnPet(this.player, this.petItem);
            inventory.setItem(PetManager.getPetSlotId(), this.petItem);
            this.cancel();
        }
    }
}
