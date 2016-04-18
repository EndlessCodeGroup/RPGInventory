package ru.endlesscode.rpginventory.pet;

import me.libraryaddict.disguise.disguisetypes.*;
import me.libraryaddict.disguise.disguisetypes.watchers.*;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Ocelot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.endlesscode.rpginventory.RPGInventory;
import ru.endlesscode.rpginventory.misc.FileLanguage;
import ru.endlesscode.rpginventory.utils.ItemUtils;
import ru.endlesscode.rpginventory.utils.StringUtils;

import java.util.*;

/**
 * Created by OsipXD on 26.08.2015
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2015 © «EndlessCode Group»
 */
public class PetType {
    // Design
    @NotNull
    private final String name;
    @NotNull
    private final String itemName;
    @NotNull
    private final String lore;
    private final String texture;
    // Stats
    private final Role role;
    private final double health;
    private final double damage;
    private final double speed;
    // Options
    private final boolean attackMobs;
    private final boolean attackPlayers;
    private final boolean revival;
    private final int cooldown;
    private DisguiseType skin;
    private Disguise disguise;
    private ItemStack spawnItem;

    PetType(@NotNull ConfigurationSection config) {
        this.name = StringUtils.coloredLine(config.getString("name"));
        this.itemName = StringUtils.coloredLine(config.getString("item-name"));
        this.lore = StringUtils.coloredLine(config.getString("lore"));
        this.texture = config.getString("item");

        this.role = Role.valueOf(config.getString("type", "COMPANION"));

        this.health = config.getDouble("health");
        this.damage = config.getDouble("damage", 0);
        this.speed = config.getDouble("speed");

        this.attackMobs = config.getBoolean("attack-mobs", false);
        this.attackPlayers = config.getBoolean("attack-players", false);
        this.revival = config.getBoolean("revival", true);
        this.cooldown = this.revival ? config.getInt("cooldown") : 0;

        this.createSpawnItem(config.getName());
        this.createDisguise(config);
    }

    @Contract("null -> false")
    public static boolean isPetItem(ItemStack item) {
        return !ItemUtils.isEmpty(item) && ItemUtils.hasTag(item, ItemUtils.PET_TAG);
    }

    @Nullable
    @Contract("null -> null")
    public static ItemStack clone(ItemStack oldItem) {
        PetType petType = PetManager.getPetFromItem(oldItem);

        if (petType == null) {
            return null;
        }

        ItemStack newItem = petType.getSpawnItem();
        PetManager.setCooldown(newItem, PetManager.getCooldown(oldItem));
        PetManager.saveHealth(newItem, PetManager.getHealth(oldItem, petType.getHealth()));

        return newItem;
    }

    private void createDisguise(ConfigurationSection config) {
        // Load features
        Map<String, String> features;
        List<String> featureList = config.getStringList("features");
        if (featureList != null && featureList.size() > 0) {
            features = new HashMap<>(featureList.size());
            for (String feature : featureList) {
                String[] data = feature.replaceAll(" ", "").split(":");
                features.put(data[0], data[1]);
            }
        } else {
            features = null;
        }

        // Build disguise
        this.skin = DisguiseType.valueOf(config.getString("skin", this.role.getDefaultSkin()));

        Disguise disguise;
        if (features != null) {
            if (this.skin == DisguiseType.PLAYER) {
                disguise = new PlayerDisguise(this.name);
            } else {
                disguise = new MobDisguise(this.skin, !(features.containsKey("BABY") && features.get("BABY").equals("TRUE")));
            }

            switch (this.skin) {
                case PIG:
                    if (features.containsKey("SADDLE") && features.get("SADDLE").equals("TRUE")) {
                        ((PigWatcher) disguise.getWatcher()).setSaddled(true);
                    }
                    break;
                case HORSE:
                    HorseWatcher horseWatcher = ((HorseWatcher) disguise.getWatcher());
                    if (features.containsKey("CHEST") && features.get("CHEST").equals("TRUE")) {
                        horseWatcher.setCarryingChest(true);
                    }

                    if (features.containsKey("ARMOR")) {
                        horseWatcher.setHorseArmor(new ItemStack(Material.valueOf(features.get("ARMOR"))));
                    }

                    if (features.containsKey("COLOR")) {
                        horseWatcher.setColor(Horse.Color.valueOf(features.get("COLOR")));
                    }

                    if (features.containsKey("STYLE")) {
                        horseWatcher.setStyle(Horse.Style.valueOf(features.get("STYLE")));
                    }
                case DONKEY:
                case SKELETON_HORSE:
                case UNDEAD_HORSE:
                case MULE:
                    horseWatcher = ((HorseWatcher) disguise.getWatcher());
                    horseWatcher.setTamed(true);
                    if (features.containsKey("SADDLE") && features.get("SADDLE").equals("TRUE")) {
                        horseWatcher.setSaddled(true);
                    }
                    break;
                case WOLF:
                    if (features.containsKey("COLLAR")) {
                        ((WolfWatcher) disguise.getWatcher()).setCollarColor(AnimalColor.valueOf(features.get("COLLAR")));
                    }
                    break;
                case ENDERMAN:
                    if (features.containsKey("ITEM")) {
                        disguise.getWatcher().setItemInHand(new ItemStack(Material.valueOf(features.get("ITEM"))));
                    }
                    break;
                case OCELOT:
                    if (features.containsKey("TYPE")) {
                        ((OcelotWatcher) disguise.getWatcher()).setType(Ocelot.Type.valueOf(features.get("TYPE")));
                    }
                    break;
                case PLAYER:
                    if (features.containsKey("SKIN")) {
                        ((PlayerWatcher) disguise.getWatcher()).setSkin(features.get("SKIN"));
                    }

                    if (features.containsKey("ITEM")) {
                        disguise.getWatcher().setItemInHand(new ItemStack(Material.valueOf(features.get("ITEM"))));
                    }
                    break;
                case RABBIT:
                    if (features.containsKey("TYPE")) {
                        ((RabbitWatcher) disguise.getWatcher()).setType(RabbitType.valueOf(features.get("TYPE")));
                    }
                    break;
                case SHEEP:
                    SheepWatcher sheepWatcher = ((SheepWatcher) disguise.getWatcher());
                    if (features.containsKey("SHEARED")) {
                        sheepWatcher.setSheared(true);
                    }

                    if (features.containsKey("COLOR")) {
                        sheepWatcher.setColor(AnimalColor.valueOf(features.get("COLOR")));
                    }
                    break;
            }
        } else {
            disguise = new MobDisguise(this.skin, true);
        }

        disguise.setReplaceSounds(true);
        this.disguise = disguise;
    }

    private void createSpawnItem(String id) {
        // Set texture
        ItemStack spawnItem = ItemUtils.getTexturedItem(this.texture);

        // Set lore and display itemName
        ItemMeta meta = spawnItem.getItemMeta();
        meta.setDisplayName(this.itemName);

        FileLanguage lang = RPGInventory.getLanguage();
        List<String> lore = new ArrayList<>();
        lore.add(StringUtils.coloredLine("&8" + lang.getCaption("pet.role." + this.role.name().toLowerCase())));
        lore.addAll(Arrays.asList(this.lore.split("\n")));

        lore.add(String.format(lang.getCaption("pet.health"), (int) (this.health)));
        if (this.role == Role.COMPANION && !this.attackMobs && !this.attackPlayers) {
            lore.add(String.format(lang.getCaption("pet.damage"), (int) (this.damage)));
        }
        lore.add(String.format(lang.getCaption("pet.speed"), (int) (this.speed * 100)));
        lore.add(String.format(lang.getCaption("pet.revival." + (this.revival ? "yes" : "no")), this.cooldown));

        meta.setLore(lore);
        spawnItem.setItemMeta(meta);
        ItemUtils.setMaxStackSize(spawnItem, 1);
        this.spawnItem = ItemUtils.setTag(spawnItem, ItemUtils.PET_TAG, id);
    }

    public ItemStack getSpawnItem() {
        return this.spawnItem;
    }

    @NotNull
    public String getName() {
        return name;
    }

    public double getHealth() {
        return health;
    }

    public boolean isRevival() {
        return revival;
    }

    public int getCooldown() {
        return cooldown;
    }

    public double getSpeed() {
        return speed;
    }

    public double getDamage() {
        return damage;
    }

    public boolean isAttackPlayers() {
        return attackPlayers;
    }

    public boolean isAttackMobs() {
        return attackMobs;
    }

    boolean isAdult() {
        return disguise.getType() == DisguiseType.PLAYER || ((MobDisguise) disguise).isAdult();
    }

    public Role getRole() {
        return role;
    }

    DisguiseType getSkin() {
        return this.skin;
    }

    Disguise getDisguise() {
        return this.disguise.clone();
    }

    public enum Role {
        COMPANION("WOLF"),
        MOUNT("HORSE");

        private final String defaultSkin;

        Role(String defaultSkin) {
            this.defaultSkin = defaultSkin;
        }

        public String getDefaultSkin() {
            return defaultSkin;
        }
    }
}
