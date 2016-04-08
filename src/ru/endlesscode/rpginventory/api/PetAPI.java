package ru.endlesscode.rpginventory.api;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.endlesscode.rpginventory.inventory.InventoryManager;
import ru.endlesscode.rpginventory.inventory.PlayerWrapper;
import ru.endlesscode.rpginventory.pet.PetManager;
import ru.endlesscode.rpginventory.pet.PetType;

/**
 * Created by OsipXD on 03.09.2015
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2015 © «EndlessCode Group»
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class PetAPI {
    /**
     * Get pet spawn item from RPGInventory of specific player
     *
     * @param player - the player
     * @return ItemStack if player have pet spawn item, null - otherwise
     */
    @Nullable
    public static ItemStack getPetItem(@NotNull Player player) {
        PlayerWrapper playerWrapper = InventoryManager.get(player);
        ItemStack petItem = PetManager.isEnabled() && playerWrapper != null ? playerWrapper.getInventory().getItem(PetManager.getPetSlotId()) : null;

        return petItem != null && petItem.getType() == Material.AIR ? null : petItem;
    }

    /**
     * Get Pet of specific player
     *
     * @param player - the player
     * @return Pet if player have pet, null - otherwise
     */
    @Nullable
    public static PetType getPet(@NotNull Player player) {
        return PetManager.getPetFromItem(PetAPI.getPetItem(player));
    }
}
