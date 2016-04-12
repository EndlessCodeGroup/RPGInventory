package ru.endlesscode.rpginventory.item;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import ru.endlesscode.rpginventory.RPGInventory;
import ru.endlesscode.rpginventory.api.InventoryAPI;
import ru.endlesscode.rpginventory.misc.Config;
import ru.endlesscode.rpginventory.misc.FileLanguage;
import ru.endlesscode.rpginventory.nms.VersionHandler;
import ru.endlesscode.rpginventory.utils.ItemUtils;
import ru.endlesscode.rpginventory.utils.PlayerUtils;
import ru.endlesscode.rpginventory.utils.StringUtils;

import java.io.File;
import java.util.*;

/**
 * Created by OsipXD on 18.09.2015
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2015 © «EndlessCode Group»
 */
public class ItemManager {
    private static final Map<String, CustomItem> CUSTOM_ITEMS = new HashMap<>();
    private static final List<String> LORE_PATTERN = Config.getConfig().getStringList("items.lore-pattern");
    private static final String LORE_SEPARATOR = Config.getConfig().getString("items.separator");

    private ItemManager() {
    }

    public static void init() {
        File itemsFile = new File(RPGInventory.getInstance().getDataFolder(), "items.yml");
        if (!itemsFile.exists()) {
            RPGInventory.getInstance().saveResource("items.yml", false);
        }

        FileConfiguration itemsConfig = YamlConfiguration.loadConfiguration(itemsFile);

        CUSTOM_ITEMS.clear();
        for (String key : itemsConfig.getConfigurationSection("items").getKeys(false)) {
            CustomItem customItem = new CustomItem(key, itemsConfig.getConfigurationSection("items." + key));
            CUSTOM_ITEMS.put(key, customItem);
        }
        RPGInventory.getPluginLogger().info(CUSTOM_ITEMS.size() + " item(s) has been loaded");
    }

    public static Modifier getModifier(@NotNull Player player, ItemStat.StatType statType, boolean notifyPlayer) {
        double bonus = 0;
        float multiplier = 1;

        List<ItemStack> items = new ArrayList<>(InventoryAPI.getPassiveItems(player));
        Collections.addAll(items, player.getInventory().getArmorContents());

        //noinspection deprecation
        ItemStack itemInHand = (VersionHandler.is1_9()) ? player.getEquipment().getItemInMainHand() : player.getItemInHand();
        if (CustomItem.isCustomItem(itemInHand) && ItemManager.allowedForPlayer(player, itemInHand, notifyPlayer)) {
            items.add(itemInHand);
        }

        if (VersionHandler.is1_9()) {
            itemInHand = player.getEquipment().getItemInOffHand();
            if (CustomItem.isCustomItem(itemInHand) && ItemManager.allowedForPlayer(player, itemInHand, notifyPlayer)) {
                items.add(itemInHand);
            }
        }

        for (ItemStack item : items) {
            CustomItem customItem;
            ItemStat stat;
            if (!CustomItem.isCustomItem(item) || (customItem = ItemManager.getCustomItem(item)) == null
                    || (stat = customItem.getStat(statType)) == null) {
                continue;
            }

            if (stat.isPercentage()) {
                multiplier += stat.getOperationType() == ItemStat.OperationType.MINUS ? -stat.getValue() / 100 : stat.getValue() / 100;
            } else {
                bonus += stat.getOperationType() == ItemStat.OperationType.MINUS ? -stat.getValue() : stat.getValue();
            }
        }

        return new Modifier(bonus, multiplier);
    }

    public static List<String> getItemList() {
        List<String> itemList = new ArrayList<>();
        itemList.addAll(CUSTOM_ITEMS.keySet());
        return itemList;
    }

    public static ItemStack getItem(String itemId) {
        CustomItem customItem = CUSTOM_ITEMS.get(itemId);
        return customItem == null ? new ItemStack(Material.AIR) : customItem.getItemStack();
    }

    public static CustomItem getCustomItem(@NotNull ItemStack item) {
        return CUSTOM_ITEMS.get(ItemUtils.getTag(item, ItemUtils.ITEM_TAG));
    }

    public static boolean allowedForPlayer(@NotNull Player player, @NotNull ItemStack item, boolean notifyPlayer) {
        if (!CustomItem.isCustomItem(item)) {
            return true;
        }

        CustomItem customItem = ItemManager.getCustomItem(item);
        if (!PlayerUtils.checkLevel(player, customItem.getLevel())) {
            if (notifyPlayer) {
                player.sendMessage(String.format(RPGInventory.getLanguage().getCaption("error.item.level"), customItem.getLevel()));
            }

            return false;
        }

        if (customItem.getClasses() == null || PlayerUtils.checkClass(player, customItem.getClasses())) {
            return true;
        }

        if (notifyPlayer) {
            player.sendMessage(String.format(RPGInventory.getLanguage().getCaption("error.item.class"), customItem.getClassesString()));
        }

        return false;
    }

    public static void updateStatsLater(@NotNull final Player player) {
        new StatsUpdater(player).runTaskLater(RPGInventory.getInstance(), 1);
    }

    static List<String> buildLore(CustomItem item) {
        FileLanguage lang = RPGInventory.getLanguage();
        List<String> lore = new ArrayList<>();
        boolean lastIsSeparator = false;
        for (String loreElement : LORE_PATTERN) {
            switch (loreElement) {
                case "_UNBREAKABLE_":
                    if (item.isUnbreakable()) {
                        lore.add(lang.getCaption("item.unbreakable"));
                        lastIsSeparator = false;
                    }
                    break;
                case "_DROP_":
                    if (!item.isDrop()) {
                        lore.add(lang.getCaption("item.nodrop"));
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
                        lore.add(String.format(lang.getCaption("item.level"), item.getLevel()));
                        lastIsSeparator = false;
                    }
                    break;
                case "_CLASS_":
                    if (item.getClasses() != null) {
                        lore.add(String.format(lang.getCaption("item.class"), item.getClassesString()));
                        lastIsSeparator = false;
                    }
                    break;
                case "_LORE_":
                    if (item.getLore() != null) {
                        lore.addAll(Arrays.asList(item.getLore().split("\n")));
                        lastIsSeparator = false;
                    }
                    break;
                case "_SKILLS_":
                    if (item.hasLeftClickCaption()) {
                        lore.add(String.format(lang.getCaption("item.left-click"), item.getLeftClickCaption()));
                        lastIsSeparator = false;
                    }
                    if (item.hasRightClickCaption()) {
                        lore.add(String.format(lang.getCaption("item.right-click"), item.getRightClickCaption()));
                        lastIsSeparator = false;
                    }
                    break;
                case "_STATS_":
                    if (item.isStatsHidden()) {
                        lore.add(lang.getCaption("item.hide"));
                    } else {
                        for (ItemStat stat : item.getStats()) {
                            lore.add(String.format(lang.getCaption("stat." + stat.getType().name().toLowerCase()), stat.getStringValue()));
                        }
                        lastIsSeparator = false;
                    }
                    break;
                default:
                    lore.add(StringUtils.coloredLine(loreElement));
            }
        }

        return lore;
    }
}
