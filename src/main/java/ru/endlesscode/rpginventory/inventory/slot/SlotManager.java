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
import ru.endlesscode.rpginventory.utils.Log;
import ru.endlesscode.rpginventory.utils.SafeEnums;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by OsipXD on 05.09.2015
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
public class SlotManager {

    private static final String CONFIG_NAME = "slots.yml";

    @Nullable
    private static SlotManager slotManager = null;

    private final List<Slot> slots = new ArrayList<>();

    @NotNull
    private final Path slotsFile;
    @NotNull
    private final FileConfiguration slotsConfig;

    private SlotManager() {
        this.slotsFile = RPGInventory.getInstance().getDataPath().resolve(CONFIG_NAME);
        if (Files.notExists(slotsFile)) {
            RPGInventory.getInstance().saveResource(CONFIG_NAME, false);
        }

        this.slotsConfig = YamlConfiguration.loadConfiguration(slotsFile.toFile());

        @Nullable final ConfigurationSection slots = this.slotsConfig.getConfigurationSection("slots");
        if (slots == null) {
            Log.s("Section ''slots'' not found in {0}", CONFIG_NAME);
            return;
        }

        for (String slotName : slots.getKeys(false)) {
            final ConfigurationSection slotConfiguration = slots.getConfigurationSection(slotName);
            assert slotConfiguration != null;

            Slot.SlotType slotType = SafeEnums.valueOfOrDefault(Slot.SlotType.class, slotConfiguration.getString("type"), Slot.SlotType.GENERIC, "slot type");
            Slot slot;
            if (slotType == Slot.SlotType.ACTION) {
                slot = new ActionSlot(slotName, slotConfiguration);
            } else {
                slot = new Slot(slotName, slotType, slotConfiguration);
            }

            if (this.validateSlot(slot)) {
                this.slots.add(slot);
            } else {
                Log.w("Slot \"{0}\" was not been added.", slot.getName());
            }
        }
    }

    public static boolean init() {
        try {
            SlotManager.slotManager = new SlotManager();
        } catch (Exception e) {
            Log.w(e, "Failed to initialize SlotManager");
            return false;
        }

        return true;
    }

    @NotNull
    public static SlotManager instance() {
        return Objects.requireNonNull(SlotManager.slotManager, "Plugin is not initialized yet");
    }

    private boolean validateSlot(Slot slot) {
        if (slot.getSlotType().isReadItemList() && slot.itemListIsEmpty()) {
            Log.w("Slot with type {0} must contains list of allowed items", slot.getSlotType());
        }

        if (!validateItems(slot.getAllowedItems()) || !validateItems(slot.getDeniedItems())) {
            return false;
        }

        if (!slot.getSlotType().isAllowMultiSlots() && slot.getSlotIds().size() > 1) {
            Log.w("Slot with type {0} can not contain more than one slotId", slot.getSlotType());
            return false;
        }

        for (int slotId : slot.getSlotIds()) {
            if (slotId < 0 || slotId > 53) {
                Log.w("Slot IDs should be in range 0..53, but it was {0}", slotId);
                return false;
            }
        }

        for (Slot existingSlot : this.slots) {
            for (int slotId : slot.getSlotIds()) {
                if (existingSlot.getSlotIds().contains(slotId)) {
                    Log.w("Slot {0} is occupied by {1}", slotId, existingSlot.getName());
                    return false;
                }
            }

            if (slot.isQuick() && existingSlot.isQuick()) {
                int slotId = slot.getQuickSlot();
                int existingSlotId = existingSlot.getQuickSlot();
                if (slotId == existingSlotId) {
                    Log.w("Quickbar slot {0} is occupied by {1}", slotId, existingSlot.getName());
                    return false;
                }
            }

            if (slot.getSlotType().isUnique() && slot.getSlotType() == existingSlot.getSlotType()) {
                Log.w("You can not create more then one slot with type {0}!", slot.getSlotType());
                return false;
            }
        }

        return true;
    }

    private boolean validateItems(List<String> itemsPatterns) {
        for (String itemPattern : itemsPatterns) {
            if (!itemPattern.matches("^[\\w_]+(:\\d+(-\\d)?)?$")) {
                Log.w("Allowed and denied items should fit to pattern ''[string]:[number]-[number]''");
                Log.w("But it was: {0}", itemPattern);
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
