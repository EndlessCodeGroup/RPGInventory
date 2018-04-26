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

package ru.endlesscode.rpginventory.api;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import ru.endlesscode.rpginventory.inventory.InventoryManager;
import ru.endlesscode.rpginventory.inventory.PlayerWrapper;
import ru.endlesscode.rpginventory.pet.PetManager;
import ru.endlesscode.rpginventory.pet.PetType;
import ru.endlesscode.rpginventory.utils.ItemUtils;

/**
 * Created by OsipXD on 03.09.2015
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class PetAPI {
    /**
     * Get pet spawn item from RPGInventory of specific player.
     *
     * @param player - not null player
     * @return ItemStack if player have pet spawn item, null - otherwise
     */
    @Nullable
    public static ItemStack getPetItem(Player player) {
        if (!InventoryManager.playerIsLoaded(player) || !PetManager.isEnabled()) {
            return null;
        }

        PlayerWrapper playerWrapper = InventoryManager.get(player);
        ItemStack petItem = playerWrapper.getInventory().getItem(PetManager.getPetSlotId());

        return ItemUtils.isEmpty(petItem) ? null : petItem;
    }

    /**
     * Get Pet of specific player.
     *
     * @param player - not null player
     * @return Pet if player have pet, null - otherwise
     */
    @Nullable
    public static PetType getPet(Player player) {
        return PetManager.getPetFromItem(PetAPI.getPetItem(player));
    }
}
