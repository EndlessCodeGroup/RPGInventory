/*
 * This file is part of RPGInventory.
 * Copyright (C) 2015-2017 osipf
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
import ru.endlesscode.rpginventory.item.ClassedItem;
import ru.endlesscode.rpginventory.misc.FileLanguage;
import ru.endlesscode.rpginventory.utils.ItemUtils;
import ru.endlesscode.rpginventory.utils.StringUtils;
import ru.endlesscode.rpginventory.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by OsipXD on 26.08.2015
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
public class PetType extends ClassedItem {
    // Design
    @NotNull
    private final String name;
    @NotNull
    private final String itemName;
    @NotNull
    private final List<String> lore;

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
        super(config, config.getString("item"));

        this.name = StringUtils.coloredLine(config.getString("name"));
        this.itemName = StringUtils.coloredLine(config.getString("item-name"));
        this.lore = StringUtils.coloredLines(config.getStringList("lore"));

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
                        disguise.getWatcher().setItemInMainHand(new ItemStack(Material.valueOf(features.get("ITEM"))));
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
                        disguise.getWatcher().setItemInMainHand(new ItemStack(Material.valueOf(features.get("ITEM"))));
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
        ItemStack spawnItem = ItemUtils.getTexturedItem(this.texture);

        // Set lore and display itemName
        ItemMeta meta = spawnItem.getItemMeta();
        meta.setDisplayName(this.itemName);

        FileLanguage lang = RPGInventory.getLanguage();
        List<String> lore = new ArrayList<>();
        lore.add(StringUtils.coloredLine("&8" + lang.getCaption("pet.role." + this.role.name().toLowerCase())));

        // Add class and level requirements
        if (this.getLevel() != -1) {
            lore.add(lang.getCaption("item.level", this.getLevel()));
        }

        if (this.getClasses() != null) {
            lore.add(lang.getCaption("item.class", this.getClassesString()));
        }

        lore.addAll(this.lore);

        lore.add(lang.getCaption("pet.health", (int) (this.health)));
        if (this.role == Role.COMPANION && (this.attackMobs || this.attackPlayers)) {
            lore.add(lang.getCaption("pet.damage", (int) (this.damage)));
        }
        lore.add(lang.getCaption("pet.speed", Utils.round(this.speed, 2)));
        lore.add(lang.getCaption("pet.revival." + (this.revival ? "yes" : "no"), this.cooldown));

        meta.setLore(lore);
        spawnItem.setItemMeta(meta);

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
        double speed = this.speed*Attributes.ONE_BPS;

        if (this.role == Role.MOUNT) {
            speed /= Attributes.GALLOP_MULTIPLIER;
        }

        if (!isAdult()) {
            speed /= 1.5;
        }

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
