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

package ru.endlesscode.rpginventory.inventory.slot;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.endlesscode.rpginventory.item.Texture;
import ru.endlesscode.rpginventory.utils.InventoryUtils;
import ru.endlesscode.rpginventory.utils.ItemUtils;
import ru.endlesscode.rpginventory.utils.Log;
import ru.endlesscode.rpginventory.utils.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by OsipXD on 05.09.2015
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
public class Slot {

    public static final int SHIELD_RAW_SLOT_ID = 45;
    private static final int SHIELD_SLOT_ID = 40;

    private final String name;
    @NotNull
    private final SlotType slotType;

    private final List<String> allowed = new ArrayList<>();
    private final List<String> denied = new ArrayList<>();

    @NotNull
    private final List<Integer> slotIds;
    @NotNull
    private final ItemStack cup;
    private final int requiredLevel;
    private final int cost;
    private final int quickSlot;
    private final boolean drop;

    public Slot(String name, @NotNull SlotType slotType, @NotNull ConfigurationSection config) {
        this.name = name;

        this.slotType = slotType;
        final List<Integer> slotList = config.getIntegerList("slot");
        this.slotIds = slotList.isEmpty() ? Collections.singletonList(config.getInt("slot")) : slotList;
        this.requiredLevel = config.getInt("cost.required-level", 0);
        this.cost = config.getInt("cost.money", 0);
        this.drop = config.getBoolean("drop", true);

        int quickSlot = config.contains("quickbar") ? InventoryUtils.getQuickSlot(config.getInt("quickbar")) : -1;
        if (slotType == SlotType.SHIELD) {
            quickSlot = SHIELD_SLOT_ID;
        } else if (slotType != SlotType.ACTIVE && quickSlot == -1 || !slotType.isAllowQuick() || slotIds.size() > 1) {
            if (config.contains("quickbar")) {
                Log.w("Option \"quickbar\" is ignored for slot \"{0}\"!", name);
            }
            quickSlot = -1;
        }
        this.quickSlot = quickSlot;

        if (slotType.isReadItemList()) {
            for (String item : config.getStringList("items")) {
                if (item.startsWith("-")) {
                    this.denied.add(item.substring(1));
                } else {
                    this.allowed.add(item);
                }
            }
        } else {
            if (config.contains("items")) {
                Log.w("Option \"items\" is ignored for slot \"{0}\"!", name);
            }

            if (slotType == SlotType.ELYTRA) {
                this.allowed.add("ELYTRA");
            }
        }

        // Setup cup slot
        Texture texture = Texture.parseTexture(config.getString("holder.item"));
        ItemStack cup = texture.getItemStack();
        ItemMeta meta = cup.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(config.contains("holder.name") ? StringUtils.coloredLine(config.getString("holder.name")) : "[Holder name missing]");
            meta.setLore(config.contains("holder.lore") ? StringUtils.coloredLines(config.getStringList("holder.lore")) : Collections.singletonList("[Holder lore missing]"));
            cup.setItemMeta(meta);
        }
        this.cup = cup;
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
                String[] borders = data[1].split("-");
                int itemTextureData = ItemUtils.getTextureData(itemStack);

                if (borders.length == 1 && itemTextureData != Integer.parseInt(data[1])) {
                    continue;
                } else if (borders.length == 2) {
                    int min = Integer.parseInt(borders[0]);
                    int max = Integer.parseInt(borders[1]);

                    if (min > max) {
                        int temp = max;
                        max = min;
                        min = temp;
                    }

                    if (itemTextureData < min || itemTextureData > max) {
                        continue;
                    }
                }
            }

            return true;
        }

        return false;
    }

    @NotNull
    public ItemStack getCup() {
        return this.cup.clone();
    }

    public boolean isCup(@Nullable ItemStack itemStack) {
        return this.cup.equals(itemStack);
    }

    boolean containsSlot(int slot) {
        return this.slotIds.contains(slot);
    }

    public boolean isValidItem(@Nullable ItemStack itemStack) {
        return ItemUtils.isNotEmpty(itemStack) && !this.isDenied(itemStack) && this.isAllowed(itemStack);
    }

    public boolean isFree() {
        return this.cost == 0;
    }

    public List<String> getAllowedItems() {
        return allowed;
    }

    public List<String> getDeniedItems() {
        return denied;
    }

    boolean itemListIsEmpty() {
        return this.allowed.isEmpty() && this.denied.isEmpty();
    }

    private boolean isDenied(@NotNull ItemStack item) {
        return searchItem(this.denied, item);
    }

    private boolean isAllowed(@NotNull ItemStack item) {
        return searchItem(this.allowed, item);
    }

    @NotNull
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
        return this.quickSlot != -1 && this.slotType != SlotType.SHIELD;
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
        GENERIC(true, true, false, true),
        ACTION(false, true, false, false),
        PET(false, false, true, false),
        ARMOR(false, false, false, true),
        ACTIVE(true, false, false, true),
        BACKPACK(true, false, false, false),
        PASSIVE(true, true, false, true),
        SHIELD(false, false, true, true),
        ELYTRA(false, false, true, false),
        INFO(false, false, false, false);

        private final boolean allowQuick;
        private final boolean allowMultiSlots;
        private final boolean unique;
        private final boolean readItemList;

        SlotType(boolean allowQuick, boolean allowMultiSlots, boolean unique, boolean readItemList) {
            this.allowQuick = allowQuick;
            this.allowMultiSlots = allowMultiSlots;
            this.unique = unique;
            this.readItemList = readItemList;
        }

        public boolean isAllowQuick() {
            return allowQuick;
        }

        public boolean isAllowMultiSlots() {
            return allowMultiSlots;
        }

        public boolean isUnique() {
            return unique;
        }

        public boolean isReadItemList() {
            return readItemList;
        }
    }
}
