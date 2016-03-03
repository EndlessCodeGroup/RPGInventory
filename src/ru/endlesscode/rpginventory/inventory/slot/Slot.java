package ru.endlesscode.rpginventory.inventory.slot;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.endlesscode.rpginventory.RPGInventory;
import ru.endlesscode.rpginventory.inventory.InventoryManager;
import ru.endlesscode.rpginventory.inventory.ResourcePackManager;
import ru.endlesscode.rpginventory.misc.Config;
import ru.endlesscode.rpginventory.utils.ItemUtils;
import ru.endlesscode.rpginventory.utils.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by OsipXD on 05.09.2015
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2015 © «EndlessCode Group»
 */
public class Slot {
    private final String name;
    private final SlotType slotType;

    private final List<String> allowed = new ArrayList<>();
    private final List<String> denied = new ArrayList<>();

    private final List<Integer> slotIds;
    private final ItemStack cup;
    private final int requiredLevel;
    private final int cost;
    private final int quickSlot;
    private final boolean drop;

    public Slot(String name, @NotNull ConfigurationSection config) {
        this.name = name;
        this.slotType = SlotType.valueOf(config.getString("type"));
        this.slotIds = config.getIntegerList("slot").size() == 0 ? Collections.singletonList(config.getInt("slot")) : config.getIntegerList("slot");
        this.requiredLevel = config.getInt("cost.required-level", 0);
        this.cost = config.getInt("cost.money", 0);
        this.drop = config.getBoolean("drop", true);

        if (slotType != SlotType.ACTIVE && !config.contains("quickbar") || slotType == SlotType.ACTION
                || slotType == SlotType.PET || slotIds.size() > 1) {
            if (config.contains("quickbar")) {
                RPGInventory.getPluginLogger().warning("Option \"quickbar\" is ignored for slot \"" + name + "\"!");
            }
            this.quickSlot = -1;
        } else if (Config.getConfig().getBoolean("alternate-view.use-item")
                && ResourcePackManager.getMode() != ResourcePackManager.Mode.FORCE
                && config.getInt("quickbar") % 9 == InventoryManager.OPEN_ITEM_SLOT) {
            RPGInventory.getPluginLogger().warning("Option \"quickbar\" is ignored for slot \"" + name + "\"!");
            RPGInventory.getPluginLogger().warning("Slot " + config.getInt("quickbar") % 9 + " already reserved for inventory open item.");
            this.quickSlot = -1;
        } else {
            this.quickSlot = config.getInt("quickbar") % 9;
        }

        for (String item : config.getStringList("items")) {
            if (item.startsWith("-")) {
                this.denied.add(item.substring(1));
            } else {
                this.allowed.add(item);
            }
        }

        // Setup cup slot
        if (config.contains("holder.item")) {
            String texture = config.getString("holder.item");
            if (texture.startsWith("LEATHER_")) {
                this.cup = ItemUtils.getTexturedItem(StringUtils.coloredLine(texture.split(":")[0]));
                LeatherArmorMeta meta = (LeatherArmorMeta) cup.getItemMeta();
                meta.setColor(Color.fromRGB(Integer.parseInt(texture.split(":")[1], 16)));
                this.cup.setItemMeta(meta);
            } else {
                this.cup = ItemUtils.getTexturedItem(StringUtils.coloredLine(config.getString("holder.item")));
            }

            ItemMeta meta = this.cup.getItemMeta();
            meta.setDisplayName(config.contains("holder.name") ? StringUtils.coloredLine(config.getString("holder.name")) : "[Holder name missing]");
            meta.setLore(Collections.singletonList(config.contains("holder.lore") ? StringUtils.coloredLine(config.getString("holder.lore")) : "[Holder lore missing]"));
            this.cup.setItemMeta(meta);
        } else {
            this.cup = new ItemStack(Material.AIR);
        }
    }

    private static boolean searchItem(List<String> materialList, @NotNull ItemStack itemStack) {
        for (String material : materialList) {
            String[] data = material.split(":");

            if (material.equals("ALL")) {
                return true;
            }

            if (!itemStack.getType().name().equals(data[0])) {
                continue;
            }

            if (data.length > 1) {
                if (itemStack.getDurability() != Short.valueOf(data[1])) {
                    continue;
                }
            }

            return true;
        }

        return false;
    }

    @NotNull
    public ItemStack getCup() {
        return this.cup;
    }

    public boolean isCup(ItemStack itemStack) {
        return this.cup.equals(itemStack);
    }

    public boolean containsSlot(int slot) {
        return this.slotIds.contains(slot);
    }

    public boolean isValidItem(@Nullable ItemStack itemStack) {
        return itemStack != null && itemStack.getType() != Material.AIR && !this.isDenied(itemStack) && this.isAllowed(itemStack);
    }

    public boolean isFree() {
        return this.cost == 0;
    }

    private boolean isDenied(@NotNull ItemStack item) {
        return searchItem(this.denied, item);
    }

    private boolean isAllowed(@NotNull ItemStack item) {
        return searchItem(this.allowed, item);
    }

    public SlotType getSlotType() {
        return slotType;
    }

    public String getName() {
        return name;
    }

    public int getRequiredLevel() {
        return requiredLevel;
    }

    public int getSlotId() {
        return slotIds.get(0);
    }

    public int getQuickSlot() {
        return this.quickSlot;
    }

    public boolean isQuick() {
        return this.quickSlot != -1;
    }

    @NotNull
    public List<Integer> getSlotIds() {
        return slotIds;
    }

    public int getCost() {
        return cost;
    }

    public boolean isDrop() {
        return drop;
    }

    @SuppressWarnings("unused")
    public enum SlotType {
        GENERIC,
        ACTION,
        PET,
        ARMOR,
        ACTIVE,
        BACKPACK,
        PASSIVE
    }
}
