package ru.endlesscode.rpginventory.pet;

import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.inventory.HorseInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.endlesscode.rpginventory.RPGInventory;
import ru.endlesscode.rpginventory.inventory.InventoryManager;
import ru.endlesscode.rpginventory.inventory.PlayerWrapper;
import ru.endlesscode.rpginventory.inventory.slot.SlotManager;
import ru.endlesscode.rpginventory.utils.EffectUtils;
import ru.endlesscode.rpginventory.utils.EntityUtils;
import ru.endlesscode.rpginventory.utils.ItemUtils;
import ru.endlesscode.rpginventory.utils.LocationUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by OsipXD on 26.08.2015
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
public class PetManager {
    @SuppressWarnings("ConstantConditions")
    private static final int SLOT_PET = SlotManager.getSlotManager().getPetSlot() != null ? SlotManager.getSlotManager().getPetSlot().getSlotId() : -1;
    private static final Map<String, PetType> PETS = new HashMap<>();
    private static final Map<String, PetFood> PET_FOOD = new HashMap<>();

    private PetManager() {
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

    public static void init() {
        File petsFile = new File(RPGInventory.getInstance().getDataFolder(), "pets.yml");
        if (!petsFile.exists()) {
            RPGInventory.getInstance().saveResource("pets.yml", false);
        }

        FileConfiguration petsConfig = YamlConfiguration.loadConfiguration(petsFile);

        PetManager.PETS.clear();
        for (String key : petsConfig.getConfigurationSection("pets").getKeys(false)) {
            PetType petType = new PetType(petsConfig.getConfigurationSection("pets." + key));
            PetManager.PETS.put(key, petType);
        }
        RPGInventory.getPluginLogger().info(PetManager.PETS.size() + " pet(s) has been loaded");

        PetManager.PET_FOOD.clear();
        for (String key : petsConfig.getConfigurationSection("food").getKeys(false)) {
            PetFood pet = new PetFood(petsConfig.getConfigurationSection("food." + key));
            PetManager.PET_FOOD.put(key, pet);
        }
        RPGInventory.getPluginLogger().info(PetManager.PET_FOOD.size() + " food(s) has been loaded");
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

        if (PetManager.getCooldown(petItem) > 0) {
            PetManager.startCooldownTimer(player, petItem);
            return;
        }

        PetManager.despawnPet(player);
        Animals pet = (Animals) player.getWorld().spawnEntity(LocationUtils.getLocationNearPlayer(player, 3),
                EntityType.valueOf(petType.getRole().getDefaultSkin()));
        EffectUtils.playSpawnEffect(pet);

        switch (petType.getRole()) {
            case MOUNT:
                ((Horse) pet).setCarryingChest(false);
                HorseInventory horseInv = ((Horse) pet).getInventory();
                horseInv.setSaddle(new ItemStack(Material.SADDLE));
                EntityUtils.goToPlayer(player, pet);
            case COMPANION:
                ((Tameable) pet).setTamed(true);
                ((Tameable) pet).setOwner(player);
        }

        pet.setBreed(false);
        pet.setAdult();
        pet.setAgeLock(true);

        pet.setCustomName(String.format(RPGInventory.getLanguage().getCaption("pet.name"), petType.getName(), player.getName()));
        pet.setMaxHealth(petType.getHealth());
        pet.setCanPickupItems(false);
        pet.setRemoveWhenFarAway(false);
        pet.setHealth(PetManager.getHealth(petItem, pet.getMaxHealth()));

        Attributes attributes = new Attributes(pet);
        attributes.setSpeed(Attributes.BASE_SPEED * petType.getSpeed() / (petType.isAdult() ? 1 : 2));

        InventoryManager.get(player).setPet(pet);

        // Pet skin
        Disguise disguise = petType.getDisguise();
        disguise.getWatcher().setCustomName(pet.getCustomName());
        disguise.getWatcher().setCustomNameVisible(true);
        DisguiseAPI.disguiseEntity(pet, disguise);
    }

    public static void despawnPet(@NotNull OfflinePlayer player) {
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

        // Pet skin
        if (DisguiseAPI.isDisguised(pet)) {
            DisguiseAPI.undisguiseToAll(pet);
        }

        EffectUtils.playDespawnEffect(pet);
        pet.remove();
        playerWrapper.setPet(null);
    }

    public static void respawnPet(@NotNull OfflinePlayer player) {
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
    public static PetType getPetFromEntity(@NotNull Tameable entity) {
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

    static void addGlow(@NotNull ItemStack itemStack) {
        itemStack.addUnsafeEnchantment(Enchantment.DURABILITY, 88);
        NbtCompound compound = NbtFactory.asCompound(NbtFactory.fromItemTag(itemStack));
        compound.put(NbtFactory.ofList("ench"));
    }

    public static void setCooldown(@NotNull ItemStack item, int cooldown) {
        NbtCompound nbt = NbtFactory.asCompound(NbtFactory.fromItemTag(item));

        if (cooldown == 0) {
            nbt.remove("pet.cooldown");
        } else {
            nbt.put("pet.cooldown", System.currentTimeMillis() + (cooldown * 1000));
        }

        NbtFactory.setItemTag(item, nbt);
    }

    public static int getCooldown(@NotNull ItemStack item) {
        NbtCompound nbt = NbtFactory.asCompound(NbtFactory.fromItemTag(item.clone()));

        if (!nbt.containsKey("pet.cooldown")) {
            return 0;
        }

        int cooldown = (int) ((nbt.getLong("pet.cooldown") - System.currentTimeMillis()) / 1000);
        if (cooldown < 0) {
            cooldown = 0;
        }

        return cooldown;
    }

    public static void saveHealth(@NotNull ItemStack item, double health) {
        NbtCompound nbt = NbtFactory.asCompound(NbtFactory.fromItemTag(item));

        if (health == 0) {
            nbt.remove("pet.health");
        } else {
            nbt.put("pet.health", health);
        }

        NbtFactory.setItemTag(item, nbt);
    }

    public static double getHealth(@NotNull ItemStack item, double maxHealth) {
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

    public static ItemStack toPetItem(@NotNull ItemStack item) {
        for (PetType petType : PETS.values()) {
            if (item.getItemMeta().getLore().equals(petType.getSpawnItem().getItemMeta().getLore()) && item.getMaxStackSize() == 1) {
                return petType.getSpawnItem();
            }
        }

        return item;
    }

    public static boolean isPetItem(@NotNull ItemStack item) {
        if (!item.getItemMeta().hasLore()) {
            return false;
        }

        for (PetType petType : PETS.values()) {
            if (item.getItemMeta().getLore().equals(petType.getSpawnItem().getItemMeta().getLore()) && item.getMaxStackSize() == 1) {
                return true;
            }
        }

        return false;
    }
}