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
import org.bukkit.entity.Cat;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Wolf;
import org.bukkit.inventory.HorseInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.endlesscode.rpginventory.RPGInventory;
import ru.endlesscode.rpginventory.compat.MaterialCompat;
import ru.endlesscode.rpginventory.compat.VersionHandler;
import ru.endlesscode.rpginventory.event.listener.PetListener;
import ru.endlesscode.rpginventory.inventory.InventoryManager;
import ru.endlesscode.rpginventory.inventory.PlayerWrapper;
import ru.endlesscode.rpginventory.inventory.slot.SlotManager;
import ru.endlesscode.rpginventory.item.Texture;
import ru.endlesscode.rpginventory.utils.EffectUtils;
import ru.endlesscode.rpginventory.utils.ItemUtils;
import ru.endlesscode.rpginventory.utils.LocationUtils;
import ru.endlesscode.rpginventory.utils.Log;
import ru.endlesscode.rpginventory.utils.NbtFactoryMirror;
import ru.endlesscode.rpginventory.utils.SafeEnums;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by OsipXD on 26.08.2015
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
public class PetManager {

    private static final String CONFIG_NAME = "pets.yml";

    private static final String METADATA_KEY_PET_OWNER = "rpginventory:petowner";
    private static final Map<String, PetType> PETS = new HashMap<>();
    private static final Map<String, PetFood> PET_FOOD = new HashMap<>();
    private static final String DEATH_TIME_TAG = "pet.deathTime";
    private static CooldownsTimer COOLDOWNS_TIMER;
    private static int SLOT_PET;

    private PetManager() {
    }

    public static boolean init(@NotNull RPGInventory instance) {
        SLOT_PET = SlotManager.instance().getPetSlot() != null ? SlotManager.instance().getPetSlot().getSlotId() : -1;

        if (!PetManager.isEnabled()) {
            Log.i("Slot for pets not found");
            return false;
        }

        try {
            Path dataPath = RPGInventory.getInstance().getDataPath();
            Path petsFile = dataPath.resolve(CONFIG_NAME);
            if (Files.notExists(petsFile)) {
                String suffix = VersionHandler.isLegacy() ? ".legacy" : "";
                String defaultConfigName = CONFIG_NAME + suffix;
                RPGInventory.getInstance().saveResource(defaultConfigName, false);
                if (!suffix.isEmpty()) {
                    Files.move(dataPath.resolve(defaultConfigName), petsFile);
                }
            }

            FileConfiguration petsConfig = YamlConfiguration.loadConfiguration(petsFile.toFile());

            @Nullable final ConfigurationSection pets = petsConfig.getConfigurationSection("pets");
            if (pets == null) {
                Log.s("Section ''pets'' not found in {0}", CONFIG_NAME);
                return false;
            }

            PETS.clear();
            for (String key : pets.getKeys(false)) {
                ConfigurationSection section = pets.getConfigurationSection(key);
                if (section != null) {
                    tryToAddPet(key, section);
                }
            }

            @Nullable final ConfigurationSection food = petsConfig.getConfigurationSection("food");
            if (food == null) {
                Log.s("Section ''food'' not found in {0}", CONFIG_NAME);
            } else {
                PET_FOOD.clear();
                for (String key : food.getKeys(false)) {
                    ConfigurationSection section = food.getConfigurationSection(key);
                    if (section != null) {
                        tryToAddPetFood(key, section);
                    }
                }
            }
        } catch (Exception e) {
            instance.getReporter().report("Error on PetManager initialization", e);
            return false;
        }

        if (PETS.isEmpty()) {
            Log.i("No one configured pet found");
            return false;
        }

        Log.i("{0} pet(s) has been loaded", PetManager.PETS.size());
        Log.i("{0} food(s) has been loaded", PetManager.PET_FOOD.size());

        // Register events
        instance.getServer().getPluginManager().registerEvents(new PetListener(), instance);
        PetManager.COOLDOWNS_TIMER = new CooldownsTimer(instance);
        PetManager.COOLDOWNS_TIMER.runTaskTimer(instance, 20, CooldownsTimer.TICK_PERIOD);
        return true;
    }

    private static void tryToAddPet(String name, @NotNull ConfigurationSection config) {
        try {
            Texture texture = Texture.parseTexture(config.getString("item"));
            if (texture.isEmpty()) {
                Log.s("Pet ''{0}'' has not been added because its item is not valid.", name);
                return;
            }
            PetType petType = new PetType(texture, config);
            PetManager.PETS.put(name, petType);
        } catch (Exception e) {
            Log.s("Pet ''{0}'' can''t be added: {1}", name, e.toString());
            Log.d(e);
        }
    }

    private static void tryToAddPetFood(String name, @NotNull ConfigurationSection config) {
        try {
            Texture texture = Texture.parseTexture(config.getString("item"));
            if (texture.isEmpty()) {
                Log.s("Pet food ''{0}'' has not been added because its item is not valid.", name);
                return;
            }
            PetFood pet = new PetFood(texture, config);
            PetManager.PET_FOOD.put(name, pet);
        } catch (Exception e) {
            Log.s("Pet food ''{0}'' can''t be added: {1}", name, e.toString());
            Log.d(e);
        }
    }

    public static void initPlayer(@NotNull Player player) {
        if (!isEnabled() || !InventoryManager.playerIsLoaded(player)) {
            return;
        }

        respawnPet(player);
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
        return new ArrayList<>(PETS.keySet());
    }

    @NotNull
    public static List<String> getFoodList() {
        return new ArrayList<>(PET_FOOD.keySet());
    }

    public static void startCooldownTimer(Player player, ItemStack petItem) {
        PetManager.COOLDOWNS_TIMER.addPetCooldown(player, petItem);
    }

    public static void teleportPet(@NotNull final Player player, @Nullable final Location to) {
        if (!InventoryManager.playerIsLoaded(player) || !PetManager.isEnabled()) {
            return;
        }

        final LivingEntity petEntity = InventoryManager.get(player).getPet();
        if (petEntity == null) {
            return;
        }

        Location baseLocation = to != null ? to : player.getLocation();
        Location newPetLoc = LocationUtils.getLocationNearPoint(baseLocation, 3);
        petEntity.teleport(newPetLoc);
    }

    /** @see #respawnPet(Player, ItemStack) */
    @Deprecated
    public static void spawnPet(@NotNull final Player player, @NotNull ItemStack petItem) {
        respawnPet(player, petItem);
    }

    public static void respawnPet(@Nullable OfflinePlayer player) {
        if (!InventoryManager.playerIsLoaded(player) || !PetManager.isEnabled()) {
            return;
        }

        Inventory inventory = InventoryManager.get(player).getInventory();
        ItemStack petItem = inventory.getItem(SLOT_PET);
        if (petItem != null) {
            respawnPet((Player) player, petItem);
        }
    }

    public static void respawnPet(@NotNull final Player player, @NotNull ItemStack petItem) {
        if (!InventoryManager.playerIsLoaded(player) || !PetManager.isEnabled()) {
            return;
        }

        PetManager.despawnPet(player);

        final PetType petType = PetManager.getPetFromItem(petItem);
        if (petType == null) {
            return;
        }

        if (PetManager.getCooldown(petItem) > 0) {
            PetManager.startCooldownTimer(player, petItem);
            return;
        }

        Location petLoc = LocationUtils.getLocationNearPoint(player.getLocation(), 3);
        Animals pet = (Animals) player.getWorld().spawnEntity(petLoc, petType.getSkin());
        pet.teleport(petLoc);
        EffectUtils.playSpawnEffect(pet);
        Map<String, String> features = petType.getFeatures();

        boolean hasInitializationErrors = false;
        switch (petType.getRole()) {
            case MOUNT:
                switch (pet.getType()) {
                    case HORSE:
                        Horse horsePet = (Horse) pet;
                        HorseInventory horseInv = horsePet.getInventory();
                        horseInv.setSaddle(new ItemStack(Material.SADDLE));

                        if (features.containsKey("CHEST") && features.get("CHEST").equals("TRUE")) {
                            try {
                                //noinspection deprecation
                                horsePet.setCarryingChest(true);
                            } catch (UnsupportedOperationException ignored) {
                                //org.bukkit.craftbukkit.entity.CraftHorse.setCarryingChest (CraftHorse.class:56)
                                Log.w("Failed to add a chest to the horse");
                            }
                        }

                        if (features.containsKey("ARMOR")) {
                            String materialName = features.get("ARMOR");
                            Material armorMaterial = MaterialCompat.getMaterialOrNull(materialName);
                            if (armorMaterial != null) {
                                horseInv.setArmor(new ItemStack(armorMaterial));
                            } else {
                                Log.w("Failed to add an armor to the horse. Unknown material: {0}", materialName);
                            }
                        }

                        Horse.Color color = SafeEnums.getHorseColor(features.getOrDefault("COLOR", "BROWN"));
                        if (color != null) {
                            horsePet.setColor(color);
                        } else {
                            hasInitializationErrors = true;
                        }

                        Horse.Style style = SafeEnums.getHorseStyle(features.getOrDefault("STYLE", "NONE"));
                        if (style != null) {
                            horsePet.setStyle(style);
                        } else {
                            hasInitializationErrors = true;
                        }

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
                        DyeColor wolfCollarColor = SafeEnums.getDyeColor(features.getOrDefault("COLLAR", "RED"));
                        if (wolfCollarColor != null) {
                            wolfPet.setCollarColor(wolfCollarColor);
                        } else {
                            hasInitializationErrors = true;
                        }
                        break;
                    case CAT:
                        Cat catPet = (Cat) pet;
                        Cat.Type catType = SafeEnums.getCatType(features.getOrDefault("TYPE", "TABBY"));
                        if (catType != null) {
                            catPet.setCatType(catType);
                        } else {
                            hasInitializationErrors = true;
                        }

                        DyeColor catCollarColor = SafeEnums.getDyeColor(features.getOrDefault("COLLAR", "RED"));
                        if (catCollarColor != null) {
                            catPet.setCollarColor(catCollarColor);
                        } else {
                            hasInitializationErrors = true;
                        }
                }
        }

        if (pet instanceof Tameable) {
            ((Tameable) pet).setTamed(true);
            ((Tameable) pet).setOwner(player);
        }

        pet.setBreed(false);
        if (petType.isAdult()) {
            pet.setAdult();
        } else {
            pet.setBaby();
        }
        pet.setAgeLock(true);

        pet.setCustomName(RPGInventory.getLanguage().getMessage("pet.name", petType.getName(), player.getName()));
        pet.setCustomNameVisible(true);
        pet.setCanPickupItems(false);
        pet.setRemoveWhenFarAway(false);

        AttributeInstance maxHealth = pet.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        assert maxHealth != null;
        maxHealth.setBaseValue(petType.getHealth());
        pet.setHealth(PetManager.getHealth(petItem, maxHealth.getBaseValue()));

        AttributeInstance speedAttribute = pet.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        assert speedAttribute != null;
        speedAttribute.setBaseValue(petType.getSpeed());

        pet.setMetadata(PetManager.METADATA_KEY_PET_OWNER, new FixedMetadataValue(RPGInventory.getInstance(), player.getUniqueId()));

        if (hasInitializationErrors) {
            Log.w("The pet ''{0}'' has errors on initialization and can be differ from expected result.", pet.getCustomName());
        }

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

    public static void despawnPet(Tameable petEntity) {
        if (!PetManager.isEnabled()) {
            return;
        }

        EffectUtils.playDespawnEffect(petEntity);
        petEntity.remove();
    }

    /**
     * Gets the owner of present LivingEntity, if he is exists.
     *
     * @param entity LivingEntity
     * @return if exists, the UUID of the Player that owning that entity, otherwise null
     * @since 2.1.7
     */
    @Nullable
    public static UUID getPetOwner(@NotNull LivingEntity entity) {
        if (!entity.hasMetadata(PetManager.METADATA_KEY_PET_OWNER)) {
            return null;
        }

        final List<MetadataValue> metadata = entity.getMetadata(PetManager.METADATA_KEY_PET_OWNER);
        return metadata.stream()
                .filter(value -> RPGInventory.getInstance().equals(value.getOwningPlugin()))
                .findFirst()
                .map(it -> (UUID) it.value())
                .orElse(null);
    }

    @Nullable
    @Contract("null -> null")
    public static PetFood getFoodFromItem(@Nullable ItemStack item) {
        String id;

        if (ItemUtils.isEmpty(item) || (id = ItemUtils.getTag(item, ItemUtils.FOOD_TAG)).isEmpty()) {
            return null;
        }

        return PET_FOOD.get(id);
    }

    @Nullable
    @Contract("null -> null")
    public static PetType getPetFromItem(@Nullable ItemStack item) {
        String id;

        if (ItemUtils.isEmpty(item) || (id = ItemUtils.getTag(item, ItemUtils.PET_TAG)).isEmpty()) {
            return null;
        }

        return PETS.get(id);
    }

    @Nullable
    public static PetType getPetFromEntity(@Nullable LivingEntity entity, OfflinePlayer player) {
        if (entity == null || !InventoryManager.playerIsLoaded(player) || !PetManager.isEnabled()) {
            return null;
        }

        PlayerWrapper playerWrapper = InventoryManager.get(player);
        return (entity == playerWrapper.getPet())
                ? PetManager.getPetFromItem(playerWrapper.getInventory().getItem(SLOT_PET))
                : null;
    }

    static void addGlow(@NotNull ItemMeta meta) {
        meta.addEnchant(Enchantment.DURABILITY, 88, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
    }

    public static void saveDeathTime(@NotNull ItemStack item) {
        saveDeathTime(item, System.currentTimeMillis());
    }

    public static void saveDeathTime(@NotNull ItemStack item, long deathTime) {
        NbtCompound nbt = NbtFactoryMirror.fromItemCompound(item);
        if (deathTime == 0) {
            nbt.remove(DEATH_TIME_TAG);
        } else {
            nbt.put(DEATH_TIME_TAG, deathTime);
        }

        NbtFactoryMirror.setItemTag(item, nbt);
    }

    public static long getDeathTime(@NotNull ItemStack item) {
        if (ItemUtils.isEmpty(item)) {
            return 0L;
        }

        NbtCompound nbt = NbtFactoryMirror.fromItemCompound(item.clone());
        return nbt.containsKey(DEATH_TIME_TAG) ? nbt.getLong(DEATH_TIME_TAG) : 0L;
    }

    public static int getCooldown(@NotNull ItemStack item) {
        long deathTime = getDeathTime(item);
        if (deathTime == 0) {
            return 0;
        }

        PetType petFromItem = getPetFromItem(item);
        if (petFromItem == null) {
            return 0;
        }

        int secondsSinceDeath = (int) ((System.currentTimeMillis() - deathTime) / 1000);
        int petCooldown = petFromItem.getCooldown();
        int itemCooldown = petCooldown - secondsSinceDeath;
        if (itemCooldown < 0 || itemCooldown > petCooldown) {
            itemCooldown = 0;
        }

        if (itemCooldown == 0) {
            PetManager.saveDeathTime(item, 0);
        }

        return itemCooldown;
    }

    public static void saveHealth(@NotNull ItemStack item, double health) {
        NbtCompound nbt = NbtFactoryMirror.fromItemCompound(item);

        if (health == 0) {
            nbt.remove("pet.health");
        } else {
            nbt.put("pet.health", health);
        }

        NbtFactoryMirror.setItemTag(item, nbt);
    }

    public static double getHealth(ItemStack item, double maxHealth) {
        NbtCompound nbt = NbtFactoryMirror.fromItemCompound(item.clone());

        if (!nbt.containsKey("pet.health")) {
            return maxHealth;
        }

        double health = nbt.getDouble("pet.health");
        return Math.min(health, maxHealth);
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
        if (ItemUtils.isEmpty(item)) {
            return item;
        }

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
        if (ItemUtils.isEmpty(item) || !item.hasItemMeta() || !item.getItemMeta().hasLore()) {
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
