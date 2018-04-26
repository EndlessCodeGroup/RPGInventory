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
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.endlesscode.rpginventory.RPGInventory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

/**
 * Created by OsipXD on 05.09.2015
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
public class SlotManager {
    @Nullable
    private static SlotManager slotManager = null;

    private final List<Slot> slots = new ArrayList<>();

    @NotNull
    private final Path slotsFile;
    @NotNull
    private final FileConfiguration slotsConfig;

    private SlotManager() {
        this.slotsFile = RPGInventory.getInstance().getDataPath().resolve("slots.yml");
        if (Files.notExists(slotsFile)) {
            RPGInventory.getInstance().saveResource("slots.yml", false);
        }

        this.slotsConfig = YamlConfiguration.loadConfiguration(slotsFile.toFile());
        final ConfigurationSection slots = this.slotsConfig.getConfigurationSection("slots");
        for (String slotName : slots.getKeys(false)) {
            ConfigurationSection slotConfiguration = slots.getConfigurationSection(slotName);
            Slot.SlotType slotType = Slot.SlotType.valueOf(slotConfiguration.getString("type"));
            Slot slot;
            if (slotType == Slot.SlotType.ACTION) {
                slot = new ActionSlot(slotName, slotConfiguration);
            } else {
                slot = new Slot(slotName, slotConfiguration);
            }

            if (this.validateSlot(slot)) {
                this.slots.add(slot);
            } else {
                RPGInventory.getPluginLogger().warning("Slot \"" + slot.getName() + "\" was not been added.");
            }
        }
    }

    public static boolean init() {
        try {
            SlotManager.slotManager = new SlotManager();
        } catch (Exception e) {
            RPGInventory.getPluginLogger().log(Level.WARNING, "Failed to initialize SlotManager", e);
            return false;
        }

        return true;
    }

    @NotNull
    public static SlotManager instance() {
        return Objects.requireNonNull(SlotManager.slotManager, "Plugin is not initialized yet.");
    }

    private boolean validateSlot(Slot slot) {
        if (slot.getSlotType().isReadItemList() && slot.itemListIsEmpty()) {
            RPGInventory.getPluginLogger().warning("Slot with type " + slot.getSlotType()
                    + " must contains list of allowed items");
        }

        if (!slot.getSlotType().isAllowMultiSlots() && slot.getSlotIds().size() > 1) {
            RPGInventory.getPluginLogger().warning("Slot with type " + slot.getSlotType()
                    + " can not contain more than one slotId");
            return false;
        }

        for (Slot existingSlot : this.slots) {
            for (int slotId : slot.getSlotIds()) {
                if (existingSlot.getSlotIds().contains(slotId)) {
                    RPGInventory.getPluginLogger().warning("Slot " + slotId + " is occupied by "
                            + existingSlot.getName());
                    return false;
                }
            }

            if (slot.isQuick() && existingSlot.isQuick()) {
                int slotId = slot.getQuickSlot();
                int existingSlotId = existingSlot.getQuickSlot();
                if (slotId == existingSlotId) {
                    RPGInventory.getPluginLogger().warning("Quickbar slot " + slotId + " is occupied by "
                            + existingSlot.getName());
                    return false;
                }
            }

            if (slot.getSlotType().isUnique() && slot.getSlotType() == existingSlot.getSlotType()) {
                RPGInventory.getPluginLogger().warning("You can not create more then one slot with type " +
                        slot.getSlotType() + "!");
                return false;
            }
        }

        return true;
    }

    @Nullable
    public Slot getSlot(String name) {
        for (Slot slot : this.slots) {
            if (slot.getName().equalsIgnoreCase(name)) {
                return slot;
            }
        }

        return null;
    }

    @Nullable
    public Slot getSlot(int slotId, InventoryType.SlotType slotType) {
        for (Slot slot : this.slots) {
            if (slotType == InventoryType.SlotType.QUICKBAR) {
                if ((slot.isQuick() || slot.getSlotType() == Slot.SlotType.SHIELD) && slot.getQuickSlot() == slotId) {
                    return slot;
                }
            } else if (slot.containsSlot(slotId)) {
                return slot;
            }
        }

        return null;
    }

    @NotNull
    public List<Slot> getQuickSlots() {
        List<Slot> quickSlots = new ArrayList<>();
        for (Slot slot : this.slots) {
            if (slot.isQuick()) {
                quickSlots.add(slot);
            }
        }

        return quickSlots;
    }

    @NotNull
    public List<Slot> getPassiveSlots() {
        List<Slot> passiveSlots = new ArrayList<>();
        for (Slot slot : this.slots) {
            final Slot.SlotType type = slot.getSlotType();
            if (type == Slot.SlotType.PASSIVE || type == Slot.SlotType.BACKPACK || type == Slot.SlotType.ELYTRA) {
                passiveSlots.add(slot);
            }
        }

        return passiveSlots;
    }

    @NotNull
    public List<Slot> getActiveSlots() {
        List<Slot> activeSlots = new ArrayList<>();
        for (Slot slot : this.slots) {
            if (slot.getSlotType() == Slot.SlotType.ACTIVE) {
                activeSlots.add(slot);
            }
        }

        return activeSlots;
    }

    @NotNull
    public List<Slot> getArmorSlots() {
        List<Slot> armorSlots = new ArrayList<>(4);
        for (Slot slot : this.slots) {
            if (slot.getSlotType() == Slot.SlotType.ARMOR) {
                armorSlots.add(slot);
            }
        }

        return armorSlots;
    }

    @NotNull
    public List<Slot> getInfoSlots() {
        List<Slot> infoSlots = new ArrayList<>();
        for (Slot slot : this.slots) {
            if (slot.getSlotType() == Slot.SlotType.INFO) {
                infoSlots.add(slot);
            }
        }

        return infoSlots;
    }

    @NotNull
    public List<Slot> getSlots() {
        return this.slots;
    }

    @Nullable
    public Slot getPetSlot() {
        for (Slot slot : this.slots) {
            if (slot.getSlotType() == Slot.SlotType.PET) {
                return slot;
            }
        }

        return null;
    }

    @Nullable
    public Slot getShieldSlot() {
        for (Slot slot : this.slots) {
            if (slot.getSlotType() == Slot.SlotType.SHIELD) {
                return slot;
            }
        }

        return null;
    }

    @Nullable
    public Slot getBackpackSlot() {
        for (Slot slot : this.slots) {
            if (slot.getSlotType() == Slot.SlotType.BACKPACK) {
                return slot;
            }
        }

        return null;
    }

    @Nullable
    public Slot getElytraSlot() {
        for (Slot slot : this.slots) {
            if (slot.getSlotType() == Slot.SlotType.ELYTRA) {
                return slot;
            }
        }

        return null;
    }

    public void saveDefaults() {
        try {
            this.slotsConfig.save(this.slotsFile.toFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
