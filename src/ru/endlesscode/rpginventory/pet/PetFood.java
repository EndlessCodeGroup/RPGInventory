package ru.endlesscode.rpginventory.pet;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Tameable;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.endlesscode.rpginventory.RPGInventory;
import ru.endlesscode.rpginventory.misc.FileLanguage;
import ru.endlesscode.rpginventory.utils.ItemUtils;
import ru.endlesscode.rpginventory.utils.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by OsipXD on 28.08.2015
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2015 © «EndlessCode Group»
 */
public class PetFood {
    @NotNull
    private final String name;
    @NotNull
    private final String lore;
    private final String texture;

    private final double value;
    private final int stackSize;
    private final List<String> eaters;

    private ItemStack foodItem;

    public PetFood(@NotNull ConfigurationSection config) {
        this.name = StringUtils.coloredLine(config.getString("name"));
        this.lore = StringUtils.coloredLine(config.getString("lore"));
        this.texture = config.getString("item");
        this.value = config.getDouble("value");
        this.stackSize = config.getInt("stack-size", 1);
        this.eaters = config.getStringList("eaters");

        this.createFoodItem(config.getName());
    }

    @Contract("null -> false")
    public static boolean isFoodItem(ItemStack itemStack) {
        return itemStack != null && itemStack.getType() != Material.AIR && ItemUtils.hasTag(itemStack, ItemUtils.FOOD_TAG);
    }

    private void createFoodItem(String id) {
        // Set texture
        ItemStack spawnItem = ItemUtils.getTexturedItem(this.texture);

        // Set lore and display itemName
        ItemMeta meta = spawnItem.getItemMeta();
        meta.setDisplayName(this.name);

        FileLanguage lang = RPGInventory.getLanguage();
        List<String> lore = new ArrayList<>();
        lore.addAll(Arrays.asList(this.lore.split("\n")));
        lore.add(String.format(lang.getCaption("pet.food.value"), (int) (this.value)));
        meta.setLore(lore);
        spawnItem.setItemMeta(meta);
        ItemUtils.setMaxStackSize(spawnItem, stackSize);
        this.foodItem = ItemUtils.setTag(spawnItem, ItemUtils.FOOD_TAG, id);
    }

    public ItemStack getFoodItem() {
        return this.foodItem;
    }

    public double getValue() {
        return value;
    }

    public boolean canBeEaten(LivingEntity pet) {
        PetType petType = PetManager.getPetFromEntity((Tameable) pet);
        return petType != null && this.eaters.contains(petType.getSkin().toString());
    }
}
