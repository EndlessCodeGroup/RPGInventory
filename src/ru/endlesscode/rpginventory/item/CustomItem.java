package ru.endlesscode.rpginventory.item;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.endlesscode.rpginventory.inventory.InventoryManager;
import ru.endlesscode.rpginventory.nms.VersionHandler;
import ru.endlesscode.rpginventory.utils.ItemUtils;
import ru.endlesscode.rpginventory.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by OsipXD on 18.09.2015
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2015 © «EndlessCode Group»
 */
public class CustomItem {
    // Required options
    private final String name;
    private final String texture;
    private final List<ItemStat> stats = new ArrayList<>();

    // Not required options
    private final String lore;
    private final List<String> classes;
    private final List<String> permissions;
    private final int level;
    private final boolean drop;
    private final boolean unbreakable;
    private final boolean statsHidden;

    // Commands
    @Nullable
    private final ItemAction rightClickAction;
    @Nullable
    private final ItemAction leftClickAction;

    private ItemStack customItem;

    CustomItem(String id, @NotNull ConfigurationSection config) {
        Rarity rarity = Rarity.valueOf(config.getString("rarity"));
        this.name = StringUtils.coloredLine(rarity.getColor() + config.getString("name"));
        this.texture = config.getString("texture");

        if (config.contains("stats")) {
            for (String stat : config.getStringList("stats")) {
                this.stats.add(new ItemStat(ItemStat.StatType.valueOf(stat.split(" ")[0]), stat.split(" ")[1]));
            }
        }

        this.lore = config.contains("lore") ? StringUtils.coloredLine(config.getString("lore")) : null;

        this.leftClickAction = config.contains("abilities.left-click.command")
                ? new ItemAction(config.getConfigurationSection("abilities.left-click"))
                : null;
        this.rightClickAction = config.contains("abilities.right-click.command")
                ? new ItemAction(config.getConfigurationSection("abilities.right-click"))
                : null;

        this.classes = config.contains("classes") ? config.getStringList("classes") : null;
        this.permissions = config.contains("abilities.permissions") ? config.getStringList("abilities.permissions") : null;
        this.level = config.getInt("level", -1);
        this.drop = config.getBoolean("drop", true);
        this.unbreakable = config.getBoolean("unbreakable", false);
        this.statsHidden = config.getBoolean("hide-stats", false);

        this.createItem(id);
    }

    @Contract("null -> false")
    public static boolean isCustomItem(ItemStack itemStack) {
        return !ItemUtils.isEmpty(itemStack) && ItemUtils.hasTag(itemStack, ItemUtils.ITEM_TAG);
    }

    void onEquip(Player player) {
        if (this.permissions == null) {
            return;
        }

        InventoryManager.get(player).addPermissions(this.permissions);
    }

    public void onRightClick(Player player) {
        if (rightClickAction != null) {
            rightClickAction.doAction(player);
        }
    }

    public void onLeftClick(Player player) {
        if (leftClickAction != null) {
            leftClickAction.doAction(player);
        }
    }

    private void createItem(String id) {
        // Set texture
        ItemStack customItem = ItemUtils.getTexturedItem(this.texture);

        // Set lore and display name
        ItemMeta meta = customItem.getItemMeta();
        meta.setDisplayName(this.name);
        meta.setLore(ItemManager.buildLore(this));
        customItem.setItemMeta(meta);
        ItemUtils.setMaxStackSize(customItem, 1);
        if (this.unbreakable) {
            customItem = ItemUtils.setTag(customItem, ItemUtils.UNBREAKABLE_TAG, "1");
        }
        if (!VersionHandler.is1_7_10()) {
            customItem = ItemUtils.setTag(customItem, ItemUtils.HIDE_FLAGS_TAG, "63");
        }
        this.customItem = ItemUtils.setTag(customItem, ItemUtils.ITEM_TAG, id);
    }

    public ItemStack getItemStack() {
        return this.customItem;
    }

    @Nullable
    ItemStat getStat(ItemStat.StatType type) {
        for (ItemStat stat : this.stats) {
            if (stat.getType() == type) {
                return stat;
            }
        }

        return null;
    }

    public int getLevel() {
        return this.level;
    }

    public boolean isDrop() {
        return drop;
    }

    @Nullable
    List<String> getClasses() {
        return this.classes;
    }

    String getClassesString() {
        String classesString = "";
        for (String theClass : this.classes) {
            if (!classesString.isEmpty()) {
                classesString += ", ";
            }

            classesString += theClass;
        }

        return classesString;
    }

    public String getLore() {
        return lore;
    }

    boolean hasLeftClickCaption() {
        return this.leftClickAction != null && this.leftClickAction.getCaption() != null;
    }

    boolean hasRightClickCaption() {
        return this.rightClickAction != null && this.rightClickAction.getCaption() != null;
    }

    String getLeftClickCaption() {
        return this.leftClickAction == null ? "" : this.leftClickAction.getCaption();
    }

    String getRightClickCaption() {
        return this.rightClickAction == null ? "" : this.rightClickAction.getCaption();
    }

    List<ItemStat> getStats() {
        return stats;
    }

    boolean isStatsHidden() {
        return statsHidden;
    }

    boolean isUnbreakable() {
        return unbreakable;
    }

    @SuppressWarnings("unused")
    private enum Rarity {
        COMMON('7'),
        UNCOMMON('6'),
        RARE('9'),
        MYTHICAL('5'),
        LEGENDARY('d');

        private final char color;

        Rarity(char color) {
            this.color = color;
        }

        @Contract(pure = true)
        public String getColor() {
            return "&" + this.color;
        }
    }
}