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

package ru.endlesscode.rpginventory.utils;

import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.endlesscode.rpginventory.compat.MaterialCompat;
import ru.endlesscode.rpginventory.inventory.backpack.BackpackManager;
import ru.endlesscode.rpginventory.inventory.backpack.BackpackType;
import ru.endlesscode.rpginventory.item.CustomItem;
import ru.endlesscode.rpginventory.item.ItemManager;
import ru.endlesscode.rpginventory.pet.PetFood;
import ru.endlesscode.rpginventory.pet.PetManager;
import ru.endlesscode.rpginventory.pet.PetType;

/**
 * Created by OsipXD on 28.08.2015
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
public class ItemUtils {
    public static final String ENTITY_TAG = "EntityTag";

    public static final String BACKPACK_UID_TAG = "backpack.uid";
    public static final String BACKPACK_TAG = "backpack.id";
    public static final String ITEM_TAG = "rpginv.id";
    public static final String FOOD_TAG = "food.id";
    public static final String PET_TAG = "pet.id";

    @NotNull
    public static ItemStack setTag(ItemStack item, @NotNull String tag, @NotNull String value) {
        ItemStack bukkitItem = toBukkitItemStack(item);
        if (isEmpty(bukkitItem)) {
            return bukkitItem;
        }

        NbtCompound nbt = NbtFactoryMirror.fromItemCompound(bukkitItem);
        if (!nbt.containsKey(tag)) {
            nbt.put(tag, value);
        }
        NbtFactoryMirror.setItemTag(bukkitItem, nbt);

        return bukkitItem;
    }

    @NotNull
    public static String getTag(@NotNull ItemStack item, @NotNull String tag) {
        return getTag(item, tag, "");
    }

    @NotNull
    @SuppressWarnings("WeakerAccess")
    public static String getTag(@NotNull ItemStack item, @NotNull String tag, @NotNull String defaultValue) {
        final ItemStack bukkitItem = toBukkitItemStack(item);
        if (isEmpty(bukkitItem)) {
            return "";
        }

        NbtCompound nbt = NbtFactoryMirror.fromItemCompound(bukkitItem);
        return nbt.containsKey(tag) ? nbt.getString(tag) : defaultValue;
    }

    @Contract("null, _ -> false")
    public static boolean hasTag(@Nullable ItemStack originalItem, String tag) {
        if (isEmpty(originalItem) || !originalItem.hasItemMeta()) {
            return false;
        }

        ItemStack item = toBukkitItemStack(originalItem.clone());
        if (isEmpty(item)) {
            return false;
        }

        NbtCompound nbt = NbtFactoryMirror.fromItemCompound(item);
        return nbt.containsKey(tag);
    }

    public static boolean isItemHasDurability(ItemStack item) {
        return item.getType().getMaxDurability() > 0;
    }

    public static NbtCompound itemStackToNBT(ItemStack originalItem, String name) {
        NbtCompound nbt = NbtFactory.ofCompound(name);

        nbt.put("material", originalItem.getType().name());
        nbt.put("amount", originalItem.getAmount());
        nbt.put("data", originalItem.getDurability());

        ItemStack item = toBukkitItemStack(originalItem.clone());
        NbtCompound tag = isEmpty(item) ? null : NbtFactoryMirror.fromItemCompound(item);
        if (tag != null) {
            nbt.put("tag", tag);
        }

        return nbt;
    }

    @NotNull
    public static ItemStack nbtToItemStack(NbtCompound nbt) {
        ItemStack item = new ItemStack(MaterialCompat.getMaterialOrAir(nbt.getString("material")));

        if (!isEmpty(item)) {
            item.setAmount(nbt.getInteger("amount"));
            item.setDurability(nbt.getShort("data"));

            if (nbt.containsKey("tag")) {
                item = toBukkitItemStack(item);
                if (!isEmpty(item)) {
                    NbtFactoryMirror.setItemTag(item, nbt.getCompound("tag"));
                }
            }
        }

        return item;
    }

    @NotNull
    public static ItemStack[] syncItems(ItemStack[] items) {
        for (int i = 0; i < items.length; i++) {
            items[i] = ItemUtils.syncItem(items[i]);
        }

        return items;
    }

    @NotNull
    @Contract("null -> !null")
    private static ItemStack syncItem(@Nullable ItemStack item) {
        if (ItemUtils.isEmpty(item)) {
            return new ItemStack(Material.AIR);
        }

        short durability = item.getDurability();
        int amount = item.getAmount();
        short textureDurability;
        if (CustomItem.isCustomItem(item)) {
            CustomItem custom = ItemManager.getCustomItem(item);

            if (custom == null) {
                return new ItemStack(Material.AIR);
            }

            textureDurability = custom.getTextureDurability();
            item = ItemManager.getItem(ItemUtils.getTag(item, ItemUtils.ITEM_TAG));
        } else if (BackpackManager.isBackpack(item)) {
            BackpackType type = BackpackManager.getBackpackType(ItemUtils.getTag(item, ItemUtils.BACKPACK_TAG));

            if (type == null) {
                return new ItemStack(Material.AIR);
            }

            textureDurability = type.getTextureDurability();

            String bpUID = ItemUtils.getTag(item, ItemUtils.BACKPACK_UID_TAG);
            if (!bpUID.isEmpty()) {
                ItemUtils.setTag(item, ItemUtils.BACKPACK_UID_TAG, bpUID);
            }
        } else if (PetType.isPetItem(item)) {
            PetType petType = PetManager.getPetFromItem(item);
            if (petType == null) {
                return new ItemStack(Material.AIR);
            }
            textureDurability = petType.getTextureDurability();

            long deathTime = PetManager.getDeathTime(item);
            double health = PetManager.getHealth(item, petType.getHealth());

            item = petType.getSpawnItem();
            PetManager.saveDeathTime(item, deathTime);
            PetManager.saveHealth(item, health);
        } else if (PetFood.isFoodItem(item)) {
            PetFood food = PetManager.getFoodFromItem(item);
            if (food == null) {
                return new ItemStack(Material.AIR);
            }
            textureDurability = food.getTextureDurability();

            item = food.getFoodItem();
        } else {
            return item;
        }

        item.setDurability(textureDurability == -1 ? durability : textureDurability);
        item.setAmount(amount);
        return item;
    }

    @Contract("null -> true")
    public static boolean isEmpty(@Nullable ItemStack item) {
        return item == null || item.getType() == Material.AIR;
    }

    @Contract("null -> false")
    public static boolean isNotEmpty(@Nullable ItemStack item) {
        return item != null && item.getType() != Material.AIR;
    }

    @NotNull
    public static ItemStack toBukkitItemStack(ItemStack item) {
        return MinecraftReflection.getBukkitItemStack(item);
    }
}
