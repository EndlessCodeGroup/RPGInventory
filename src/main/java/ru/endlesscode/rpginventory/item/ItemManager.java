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

package ru.endlesscode.rpginventory.item;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.endlesscode.rpginventory.RPGInventory;
import ru.endlesscode.rpginventory.event.listener.ItemListener;
import ru.endlesscode.rpginventory.inventory.InventoryManager;
import ru.endlesscode.rpginventory.misc.FileLanguage;
import ru.endlesscode.rpginventory.misc.config.Config;
import ru.endlesscode.rpginventory.pet.PetManager;
import ru.endlesscode.rpginventory.pet.PetType;
import ru.endlesscode.rpginventory.utils.InventoryUtils;
import ru.endlesscode.rpginventory.utils.ItemUtils;
import ru.endlesscode.rpginventory.utils.Log;
import ru.endlesscode.rpginventory.utils.PlayerUtils;
import ru.endlesscode.rpginventory.utils.StringUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by OsipXD on 18.09.2015
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
public class ItemManager {

    private static final String CONFIG_NAME = "items.yml";

    private static final Map<String, CustomItem> CUSTOM_ITEMS = new HashMap<>();
    private static final List<String> LORE_PATTERN = Config.getConfig().getStringList("items.lore-pattern");
    private static final String LORE_SEPARATOR = Config.getConfig().getString("items.separator");

    private ItemManager() {
    }

    public static boolean init(@NotNull RPGInventory instance) {
        try {
            Path itemsFile = RPGInventory.getInstance().getDataPath().resolve(CONFIG_NAME);
            if (Files.notExists(itemsFile)) {
                RPGInventory.getInstance().saveResource(CONFIG_NAME, false);
            }

            FileConfiguration itemsConfig = YamlConfiguration.loadConfiguration(itemsFile.toFile());

            @Nullable final ConfigurationSection items = itemsConfig.getConfigurationSection("items");
            if (items == null) {
                Log.s("Section ''items'' not found in {0}", CONFIG_NAME);
                return false;
            }

            CUSTOM_ITEMS.clear();
            for (String key : items.getKeys(false)) {
                ConfigurationSection section = items.getConfigurationSection(key);
                if (section != null) {
                    tryToAddItem(key, section);
                }
            }

            if (CUSTOM_ITEMS.isEmpty()) {
                Log.i("No one configured item found");
                return false;
            }

            Log.i("{0} item(s) has been loaded", CUSTOM_ITEMS.size());

            instance.getServer().getPluginManager().registerEvents(new ItemListener(), instance);
            return true;
        } catch (Exception e) {
            instance.getReporter().report("Error on InventoryManager initialization", e);
            return false;
        }
    }

    private static void tryToAddItem(String name, @NotNull ConfigurationSection config) {
        try {
            Texture texture = Texture.parseTexture(config.getString("texture"));
            if (texture.isEmpty()) {
                Log.s("Item ''{0}'' has not been added because its texture is not valid.", name);
                return;
            }
            CustomItem customItem = new CustomItem(name, texture, config);
            CUSTOM_ITEMS.put(name, customItem);
        } catch (Exception e) {
            Log.s("Item ''{0}'' can''t be added: {1}", name, e.toString());
            Log.d(e);
        }
    }

    public static Modifier getModifier(Player player, ItemStat.StatType statType) {
        return getModifier(player, statType, false);
    }

    @SuppressWarnings("SameParameterValue")
    private static Modifier getModifier(Player player, ItemStat.StatType statType, boolean notifyPlayer) {
        List<ItemStack> effectiveItems = InventoryUtils.collectEffectiveItems(player, notifyPlayer);
        double minBonus = 0;
        double maxBonus = 0;
        float minMultiplier = 1;
        float maxMultiplier = 1;
        for (ItemStack item : effectiveItems) {
            CustomItem customItem;
            ItemStat stat;
            if (!CustomItem.isCustomItem(item) || (customItem = ItemManager.getCustomItem(item)) == null
                    || (stat = customItem.getStat(statType)) == null) {
                continue;
            }

            int sign = stat.getOperationType() == ItemStat.OperationType.MINUS ? -1 : 1;
            if (stat.isPercentage()) {
                minMultiplier += sign * stat.getMinValue() / 100;
                maxMultiplier += sign * stat.getMaxValue() / 100;
            } else {
                minBonus += sign * stat.getMinValue();
                maxBonus += sign * stat.getMaxValue();
            }
        }

        return new Modifier(minBonus, maxBonus, minMultiplier, maxMultiplier);
    }

    @NotNull
    public static List<String> getItemList() {
        return new ArrayList<>(CUSTOM_ITEMS.keySet());
    }

    public static boolean hasItem(String itemId) {
        return CUSTOM_ITEMS.containsKey(itemId);
    }

    @NotNull
    public static ItemStack getItem(String itemId) {
        CustomItem customItem = CUSTOM_ITEMS.get(itemId);
        return customItem == null ? new ItemStack(Material.AIR) : customItem.getItemStack();
    }

    @Nullable
    public static CustomItem getCustomItem(@Nullable ItemStack item) {
        if (ItemUtils.isEmpty(item)) {
            return null;
        }

        String tag = ItemUtils.getTag(item, ItemUtils.ITEM_TAG);
        if (tag.isEmpty()) {
            return null;
        }

        return CUSTOM_ITEMS.get(tag);
    }

    public static boolean allowedForPlayer(Player player, ItemStack item, boolean notifyPlayer) {
        ClassedItem classedItem;
        if (CustomItem.isCustomItem(item)) {
            classedItem = ItemManager.getCustomItem(item);
        } else if (PetType.isPetItem(item)) {
            classedItem = PetManager.getPetFromItem(item);
        } else {
            return true;
        }

        if (classedItem == null) {
            return true;
        }

        if (!PlayerUtils.checkLevel(player, classedItem.getLevel())) {
            if (notifyPlayer) {
                PlayerUtils.sendMessage(
                        player, RPGInventory.getLanguage().getMessage("error.item.level", classedItem.getLevel())
                );
            }

            return false;
        }

        if (classedItem.getClasses() == null || PlayerUtils.checkClass(player, classedItem.getClasses())) {
            return true;
        }

        if (notifyPlayer) {
            PlayerUtils.sendMessage(
                    player, RPGInventory.getLanguage().getMessage("error.item.class", classedItem.getClassesString())
            );
        }

        return false;
    }

    public static void updateStats(final Player player) {
        if (!InventoryManager.playerIsLoaded(player)) {
            return;
        }

        InventoryManager.get(player).updateStatsLater();
    }

    @NotNull
    static List<String> buildLore(@NotNull CustomItem item) {
        FileLanguage lang = RPGInventory.getLanguage();
        List<String> lore = new ArrayList<>();
        boolean lastIsSeparator = false;
        for (String loreElement : LORE_PATTERN) {
            switch (loreElement) {
                case "_UNBREAKABLE_":
                    if (item.isUnbreakable()) {
                        lore.add(lang.getMessage("item.unbreakable"));
                        lastIsSeparator = false;
                    }
                    break;
                case "_DROP_":
                    if (!item.isDrop()) {
                        lore.add(lang.getMessage("item.nodrop"));
                        lastIsSeparator = false;
                    }
                    break;
                case "_SEPARATOR_":
                    if (!lastIsSeparator) {
                        lore.add(LORE_SEPARATOR);
                        lastIsSeparator = true;
                    }
                    break;
                case "_LEVEL_":
                    if (item.getLevel() != -1) {
                        lore.add(lang.getMessage("item.level", item.getLevel()));
                        lastIsSeparator = false;
                    }
                    break;
                case "_CLASS_":
                    if (item.getClasses() != null) {
                        lore.add(lang.getMessage("item.class", item.getClassesString()));
                        lastIsSeparator = false;
                    }
                    break;
                case "_LORE_":
                    if (item.getLore() != null) {
                        lore.addAll(item.getLore());
                        lastIsSeparator = false;
                    }
                    break;
                case "_SKILLS_":
                    if (item.hasLeftClickCaption()) {
                        lore.add(lang.getMessage("item.left-click", item.getLeftClickCaption()));
                        lastIsSeparator = false;
                    }
                    if (item.hasRightClickCaption()) {
                        lore.add(lang.getMessage("item.right-click", item.getRightClickCaption()));
                        lastIsSeparator = false;
                    }
                    break;
                case "_STATS_":
                    if (item.isStatsHidden()) {
                        lore.add(lang.getMessage("item.hide"));
                        lastIsSeparator = false;
                    } else {
                        for (ItemStat stat : item.getStats()) {
                            lore.add(lang.getMessage("stat." + stat.getType().name().toLowerCase(), stat.getStringValue()));
                            lastIsSeparator = false;
                        }
                    }
                    break;
                default:
                    lore.add(StringUtils.coloredLine(loreElement));
            }
        }

        if (lastIsSeparator) {
            lore.remove(lore.size() - 1);
        }

        if (lore.get(0).equals(LORE_SEPARATOR)) {
            lore.remove(0);
        }

        return lore;
    }
}
