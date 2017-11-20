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

import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Wolf;
import org.bukkit.inventory.HorseInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.endlesscode.rpginventory.RPGInventory;
import ru.endlesscode.rpginventory.event.listener.PetListener;
import ru.endlesscode.rpginventory.inventory.InventoryManager;
import ru.endlesscode.rpginventory.inventory.PlayerWrapper;
import ru.endlesscode.rpginventory.inventory.slot.SlotManager;
import ru.endlesscode.rpginventory.utils.EffectUtils;
import ru.endlesscode.rpginventory.utils.ItemUtils;
import ru.endlesscode.rpginventory.utils.LocationUtils;

/**
 * Created by OsipXD on 26.08.2015
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
public class PetManager {
    private static final Map<String, PetType> PETS = new HashMap<>();
    private static final Map<String, PetFood> PET_FOOD = new HashMap<>();
    private static final String DEATH_TIME_TAG = "pet.deathTime";
    private static int SLOT_PET;

    private PetManager() {
    }

    public static boolean init(RPGInventory instance) {
        //noinspection ConstantConditions
        SLOT_PET = SlotManager.instance().getPetSlot() != null ? SlotManager.instance().getPetSlot().getSlotId() : -1;

        if (!isEnabled()) {
            return false;
        }

        try {
            Path petsFile = RPGInventory.getInstance().getDataPath().resolve("pets.yml");
            if (Files.notExists(petsFile)) {
                RPGInventory.getInstance().saveResource("pets.yml", false);
            }

            FileConfiguration petsConfig = YamlConfiguration.loadConfiguration(petsFile.toFile());

            PetManager.PETS.clear();
            for (String key : petsConfig.getConfigurationSection("pets").getKeys(false)) {
                tryToAddPet(key, petsConfig.getConfigurationSection("pets." + key));
            }
            RPGInventory.getPluginLogger().info(PetManager.PETS.size() + " pet(s) has been loaded");

            PetManager.PET_FOOD.clear();
            for (String key : petsConfig.getConfigurationSection("food").getKeys(false)) {
                tryToAddPetFood(key, petsConfig.getConfigurationSection("food." + key));
            }
            RPGInventory.getPluginLogger().info(PetManager.PET_FOOD.size() + " food(s) has been loaded");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        if (PETS.size() == 0 && PET_FOOD.size() == 0) {
            return false;
        }

        // Register events
        instance.getServer().getPluginManager().registerEvents(new PetListener(), instance);
        return true;
    }

    private static void tryToAddPet(String name, ConfigurationSection config) {
        try {
            PetType petType = new PetType(config);
            PetManager.PETS.put(name, petType);
        } catch (Exception e) {
            String message = String.format(
                    "Pet '%s' can't be added: %s", name, e.getLocalizedMessage());
            RPGInventory.getPluginLogger().warning(message);
        }
    }

    private static void tryToAddPetFood(String name, ConfigurationSection config) {
        try {
            PetFood pet = new PetFood(config);
            PetManager.PET_FOOD.put(name, pet);
        } catch (Exception e) {
            String message = String.format(
                    "Pet food '%s' can't be added: %s", name, e.getLocalizedMessage());
            RPGInventory.getPluginLogger().warning(message);
        }
    }

    public static void initPlayer(@NotNull Player player) {
        if (!isEnabled() || !InventoryManager.playerIsLoaded(player)) {
            return;
        }

        Inventory inventory = InventoryManager.get(player).getInventory();
        if (inventory.getItem(SLOT_PET) != null) {
            ItemStack petItem = inventory.getItem(SLOT_PET);
            PetManager.spawnPet(player, petItem);
        }
    }

    @Contract(pure = true)
    public static boolean isEnabled() {
        return SLOT_PET != -1;
    }

    public static int getPetSlotId() {
        return SLOT_PET;
    }

    @NotNull
    public static List<String> getPetList() {
        List<String> petList = new ArrayList<>();
        petList.addAll(PETS.keySet());
        return petList;
    }

    @NotNull
    public static List<String> getFoodList() {
        List<String> foodList = new ArrayList<>();
        foodList.addAll(PET_FOOD.keySet());
        return foodList;
    }

    public static void startCooldownTimer(Player player, ItemStack petItem) {
        new CooldownTimer(player, petItem).runTaskTimer(RPGInventory.getInstance(), 20, 20);
    }

    public static void spawnPet(@NotNull final Player player, @NotNull ItemStack petItem) {
        if (!InventoryManager.playerIsLoaded(player)) {
            return;
        }

        final PetType petType = PetManager.getPetFromItem(petItem);
        if (petType == null) {
            return;
        }

        if (PetManager.getDeathTime(petItem) > 0) {
            PetManager.startCooldownTimer(player, petItem);
            return;
        }

        PetManager.despawnPet(player);
        Location petLoc = LocationUtils.getLocationNearPlayer(player, 3);
        Animals pet = (Animals) player.getWorld().spawnEntity(petLoc, petType.getSkin());
        pet.teleport(petLoc);
        EffectUtils.playSpawnEffect(pet);
        Map<String, String> features = petType.getFeatures();

        switch (petType.getRole()) {
            case MOUNT:
                switch (pet.getType()) {
                    case HORSE:
                        Horse horsePet = (Horse) pet;
                        HorseInventory horseInv = horsePet.getInventory();
                        horseInv.setSaddle(new ItemStack(Material.SADDLE));

                        if (features.containsKey("CHEST") && features.get("CHEST").equals("TRUE")) {
                            horsePet.setCarryingChest(true);
                        }

                        if (features.containsKey("ARMOR")) {
                            horseInv.setArmor(new ItemStack(Material.valueOf(features.get("ARMOR"))));
                        }

                        String color = features.getOrDefault("COLOR", "BROWN");
                        String style = features.getOrDefault("STYLE", "NONE");

                        horsePet.setColor(Horse.Color.valueOf(color));
                        horsePet.setStyle(Horse.Style.valueOf(style));

                        break;
                    case PIG:
                        Pig pigPet = (Pig) pet;
                        pigPet.setSaddle(true);
                }

                break;
            case COMPANION:
                switch (pet.getType()) {
                    case WOLF:
                        Wolf wolfPet = (Wolf) pet;
                        if (features.containsKey("COLLAR")) {
                            wolfPet.setCollarColor(DyeColor.valueOf(features.get("COLLAR")));
                        }
                        break;
                    case OCELOT:
                        Ocelot ocelotPet = (Ocelot) pet;
                        String type = features.getOrDefault("TYPE", "WILD_OCELOT");
                        ocelotPet.setCatType(Ocelot.Type.valueOf(type));
                }
        }

        ((Tameable) pet).setTamed(true);
        ((Tameable) pet).setOwner(player);
        pet.setBreed(false);
        if (petType.isAdult()) {
            pet.setAdult();
        } else {
            pet.setBaby();
        }
        pet.setAgeLock(true);

        pet.setCustomName(RPGInventory.getLanguage().getMessage("pet.name", petType.getName(), player.getName()));
        pet.setMaxHealth(petType.getHealth());
        pet.setCanPickupItems(false);
        pet.setRemoveWhenFarAway(false);
        pet.setHealth(PetManager.getHealth(petItem, pet.getMaxHealth()));

        AttributeInstance speedAttribute = pet.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        speedAttribute.setBaseValue(petType.getSpeed());

        InventoryManager.get(player).setPet(pet);
    }

    public static void despawnPet(OfflinePlayer player) {
        if (!InventoryManager.playerIsLoaded(player) || !PetManager.isEnabled()) {
            return;
        }

        PlayerWrapper playerWrapper = InventoryManager.get(player);
        Inventory inventory = playerWrapper.getInventory();

        LivingEntity pet = playerWrapper.getPet();
        ItemStack petItem = inventory.getItem(SLOT_PET);

        if (pet == null) {
            return;
        }

        if (petItem != null) {
            PetManager.saveHealth(petItem, pet.getHealth());
            inventory.setItem(SLOT_PET, petItem);
        }

        EffectUtils.playDespawnEffect(pet);
        pet.remove();
        playerWrapper.setPet(null);
    }

    public static void respawnPet(OfflinePlayer player) {
        if (!InventoryManager.playerIsLoaded(player) || !PetManager.isEnabled()) {
            return;
        }

        Inventory inventory = InventoryManager.get(player).getInventory();
        despawnPet(player);
        spawnPet((Player) player, inventory.getItem(SLOT_PET));
    }

    @Nullable
    @Contract("null - > null")
    public static PetFood getFoodFromItem(@Nullable ItemStack item) {
        String id;

        if (ItemUtils.isEmpty(item) || (id = ItemUtils.getTag(item, ItemUtils.FOOD_TAG)) == null) {
            return null;
        }

        return PET_FOOD.get(id);
    }

    @Nullable
    @Contract("null - > null")
    public static PetType getPetFromItem(@Nullable ItemStack item) {
        String id;

        if (ItemUtils.isEmpty(item) || (id = ItemUtils.getTag(item, ItemUtils.PET_TAG)) == null) {
            return null;
        }

        return PETS.get(id);
    }

    @Nullable
    public static PetType getPetFromEntity(Tameable entity) {
        if (!entity.isTamed() || entity.getOwner() == null) {
            return null;
        }

        OfflinePlayer player = (OfflinePlayer) entity.getOwner();
        PlayerWrapper playerWrapper = InventoryManager.get(player);

        if (!InventoryManager.playerIsLoaded(player) || !PetManager.isEnabled() || entity != playerWrapper.getPet()) {
            return null;
        }

        return PetManager.getPetFromItem(playerWrapper.getInventory().getItem(SLOT_PET));
    }

    static void addGlow(ItemStack itemStack) {
        itemStack.addUnsafeEnchantment(Enchantment.DURABILITY, 88);
        NbtCompound compound = NbtFactory.asCompound(NbtFactory.fromItemTag(itemStack));
        compound.put(NbtFactory.ofList("ench"));
    }

    public static void saveDeathTime(ItemStack item) {
        saveDeathTime(item, System.currentTimeMillis());
    }

    public static void saveDeathTime(ItemStack item, long deathTime) {
        NbtCompound nbt = NbtFactory.asCompound(NbtFactory.fromItemTag(item));

        if (deathTime == 0) {
            nbt.remove(DEATH_TIME_TAG);
        } else {
            nbt.put(DEATH_TIME_TAG, deathTime);
        }

        NbtFactory.setItemTag(item, nbt);
    }

    public static long getDeathTime(ItemStack item) {
        NbtCompound nbt = NbtFactory.asCompound(NbtFactory.fromItemTag(item.clone()));

        if (!nbt.containsKey(DEATH_TIME_TAG)) {
            return 0;
        }

        return nbt.getLong(DEATH_TIME_TAG);
    }

    public static int getCooldown(ItemStack item) {
        long deathTime = getDeathTime(item);
        if (deathTime == 0) {
            return 0;
        }

        int secondsSinceDeath = (int) ((System.currentTimeMillis() - deathTime) / 1000);
        int petCooldown = getPetFromItem(item).getCooldown();
        int itemCooldown = petCooldown - secondsSinceDeath;
        if (itemCooldown < 0 || itemCooldown > petCooldown) {
            itemCooldown = 0;
        }

        return itemCooldown;
    }

    public static void saveHealth(ItemStack item, double health) {
        NbtCompound nbt = NbtFactory.asCompound(NbtFactory.fromItemTag(item));

        if (health == 0) {
            nbt.remove("pet.health");
        } else {
            nbt.put("pet.health", health);
        }

        NbtFactory.setItemTag(item, nbt);
    }

    public static double getHealth(ItemStack item, double maxHealth) {
        NbtCompound nbt = NbtFactory.asCompound(NbtFactory.fromItemTag(item.clone()));

        if (!nbt.containsKey("pet.health")) {
            return maxHealth;
        }

        return nbt.getDouble("pet.health");
    }

    @Nullable
    public static ItemStack getPetItem(String petId) {
        PetType petType = PETS.get(petId);
        return petType == null ? null : petType.getSpawnItem();
    }

    @Nullable
    public static ItemStack getFoodItem(String petId) {
        PetFood food = PET_FOOD.get(petId);
        return food == null ? null : food.getFoodItem();
    }

    public static ItemStack toPetItem(ItemStack item) {
        List<String> itemLore = item.getItemMeta().getLore();
        for (PetType petType : PETS.values()) {
            List<String> petItemLore = petType.getSpawnItem().getItemMeta().getLore();
            if (itemLore.equals(petItemLore)) {
                return petType.getSpawnItem();
            }
        }

        return item;
    }

    public static boolean isPetItem(@Nullable ItemStack item) {
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasLore()) {
            return false;
        }

        List<String> itemLore = item.getItemMeta().getLore();
        for (PetType petType : PETS.values()) {
            List<String> petItemLore = petType.getSpawnItem().getItemMeta().getLore();
            if (itemLore.equals(petItemLore)) {
                return true;
            }
        }

        return false;
    }
}
