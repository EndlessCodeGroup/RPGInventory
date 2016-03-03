package ru.endlesscode.rpginventory.utils;

import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.endlesscode.rpginventory.RPGInventory;
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
 * All rights reserved 2014 - 2015 © «EndlessCode Group»
 */
public class ItemUtils {
    public static final String UNBREAKABLE_TAG = "Unbreakable";
    public static final String HIDE_FLAGS_TAG = "HideFlags";
    public static final String BACKPACK_UID_TAG = "backpack.uid";
    public static final String BACKPACK_TAG = "backpack.id";
    public static final String ITEM_TAG = "rpginv.id";
    public static final String FOOD_TAG = "food.id";
    public static final String PET_TAG = "pet.id";

    public static void setMaxStackSize(@NotNull ItemStack inputItem, int amount) {
        /* TODO: Add setting of max stack size
        try {
            inputItem = MinecraftReflection.getBukkitItemStack(inputItem);
            Field handle = MinecraftReflection.getCraftItemStackClass().getDeclaredField("handle");
            handle.setAccessible(true);
            Object itemStack = handle.get(inputItem);
            Field itemField = MinecraftReflection.getItemStackClass().getDeclaredField("item");
            itemField.setAccessible(true);
            Object item = itemField.get(itemStack);
            Field sizeField = MinecraftReflection.getMinecraftClass("Item").getDeclaredField("maxStackSize");
            sizeField.setAccessible(true);
            sizeField.set(item, amount);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }*/
    }

    public static ItemStack setTag(@NotNull ItemStack item, String tag, String value) {
        item = toBukkitItemStack(item);
        NbtCompound nbt = NbtFactory.asCompound(NbtFactory.fromItemTag(item));
        if (UNBREAKABLE_TAG.equals(tag) || HIDE_FLAGS_TAG.equals(tag)) {
            nbt.put(tag, Integer.valueOf(value));
        } else {
            nbt.put(tag, value);
        }
        NbtFactory.setItemTag(item, nbt);

        return item;
    }

    public static String getTag(@NotNull ItemStack item, String tag) {
        return getTag(item, tag, null);
    }

    @SuppressWarnings("WeakerAccess")
    public static String getTag(@NotNull ItemStack item, String tag, String defaultValue) {
        item = toBukkitItemStack(item);
        NbtCompound nbt = NbtFactory.asCompound(NbtFactory.fromItemTag(item));

        if (!nbt.containsKey(tag)) {
            return defaultValue;
        }

        return nbt.getString(tag);
    }

    public static boolean hasTag(@NotNull ItemStack item, String tag) {
        item = toBukkitItemStack(item);
        NbtCompound nbt = NbtFactory.asCompound(NbtFactory.fromItemTag(item));
        return nbt.containsKey(tag);
    }

    @NotNull
    public static ItemStack getTexturedItem(@NotNull String texture) {
        String[] textures = texture.split(":");

        if (Material.getMaterial(textures[0]) == null) {
            RPGInventory.getInstance().getLogger().warning("Material " + textures[0] + " not found");
            return new ItemStack(Material.AIR);
        }

        ItemStack item = new ItemStack(Material.getMaterial(textures[0]));

        if (textures.length == 2) {
            item.setDurability(Byte.parseByte(textures[1]));
        }

        return item;
    }

    public static NbtCompound itemStackToNBT(@NotNull ItemStack item, String name) {
        NbtCompound nbt = NbtFactory.ofCompound(name);

        nbt.put("material", item.getType().name());
        nbt.put("amount", item.getAmount());
        nbt.put("data", item.getDurability());

        item = toBukkitItemStack(item);
        NbtCompound tag = item.getType() == Material.AIR ? null : NbtFactory.asCompound(NbtFactory.fromItemTag(item));
        if (tag != null) {
            nbt.put("tag", tag);
        }

        return nbt;
    }

    public static ItemStack nbtToItemStack(@NotNull NbtCompound nbt) {
        ItemStack item = new ItemStack(Material.valueOf(nbt.getString("material")));

        if (item.getType() != Material.AIR) {
            item.setAmount(nbt.getInteger("amount"));
            item.setDurability(nbt.getShort("data"));

            if (nbt.containsKey("tag")) {
                item = toBukkitItemStack(item);
                NbtFactory.setItemTag(item, nbt.getCompound("tag"));
            }
        }

        return item;
    }

    public static ItemStack[] syncItems(ItemStack[] items) {
        for (int i = 0; i < items.length; i++) {
            items[i] = ItemUtils.syncItem(items[i]);
        }

        return items;
    }

    @Contract("null -> !null")
    private static ItemStack syncItem(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return new ItemStack(Material.AIR);
        }

        short durability = item.getDurability();
        if (CustomItem.isCustomItem(item)) {
            item = ItemManager.getItem(ItemUtils.getTag(item, ItemUtils.ITEM_TAG));
            item.setDurability(durability);
        } else if (BackpackManager.isBackpack(item)) {
            String bpUID = ItemUtils.getTag(item, ItemUtils.BACKPACK_UID_TAG);
            BackpackType type = BackpackManager.getBackpackType(ItemUtils.getTag(item, ItemUtils.BACKPACK_TAG));
            item = type == null ? new ItemStack(Material.AIR) : type.getItem();
            if (bpUID != null) {
                ItemUtils.setTag(item, ItemUtils.BACKPACK_UID_TAG, bpUID);
            }
            item.setDurability(durability);
        } else if (PetType.isPetItem(item)) {
            PetType petType = PetManager.getPetFromItem(item);
            if (petType == null) {
                return new ItemStack(Material.AIR);
            }

            int cooldown = PetManager.getCooldown(item);
            double health = PetManager.getHealth(item, petType.getHealth());

            item = petType.getSpawnItem();
            item.setDurability(durability);
            PetManager.setCooldown(item, cooldown);
            PetManager.saveHealth(item, health);
        } else if (PetFood.isFoodItem(item)) {
            PetFood food = PetManager.getFoodFromItem(item);
            int amount = item.getAmount();
            if (food == null) {
                return new ItemStack(Material.AIR);
            }

            item = food.getFoodItem();
            item.setAmount(amount);
            item.setDurability(durability);
        }

        return item;
    }

    private static ItemStack toBukkitItemStack(ItemStack item) {
        return !item.getClass().getName().endsWith("CraftItemStack") ? MinecraftReflection.getBukkitItemStack(item) : item;
    }
}
