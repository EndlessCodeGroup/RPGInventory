package ru.endlesscode.rpginventory.pet;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Tameable;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.endlesscode.rpginventory.RPGInventory;
import ru.endlesscode.rpginventory.item.TexturedItem;
import ru.endlesscode.rpginventory.misc.FileLanguage;
import ru.endlesscode.rpginventory.utils.ItemUtils;
import ru.endlesscode.rpginventory.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by OsipXD on 28.08.2015
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
public class PetFood extends TexturedItem {
    @NotNull
    private final String name;
    @NotNull
    private final List<String> lore;

    private final double value;
    private final List<String> eaters;

    private ItemStack foodItem;

    PetFood(@NotNull ConfigurationSection config) {
        super(config.getString("item"));

        this.name = StringUtils.coloredLine(config.getString("name"));
        this.lore = StringUtils.coloredLines(config.getStringList("lore"));
        this.value = config.getDouble("value");
        this.eaters = config.getStringList("eaters");

        this.createFoodItem(config.getName());
    }

    @Contract("null -> false")
    public static boolean isFoodItem(ItemStack itemStack) {
        return !ItemUtils.isEmpty(itemStack) && ItemUtils.hasTag(itemStack, ItemUtils.FOOD_TAG);
    }

    private void createFoodItem(String id) {
        // Set texture
        ItemStack spawnItem = ItemUtils.getTexturedItem(this.texture);

        // Set lore and display itemName
        ItemMeta meta = spawnItem.getItemMeta();
        meta.setDisplayName(this.name);

        FileLanguage lang = RPGInventory.getLanguage();
        List<String> lore = new ArrayList<>();
        lore.addAll(this.lore);
        lore.add(lang.getCaption("pet.food.value", (int) (this.value)));
        meta.setLore(lore);
        spawnItem.setItemMeta(meta);

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
