package ru.endlesscode.rpginventory.inventory.slot;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.endlesscode.rpginventory.RPGInventory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by OsipXD on 05.09.2015
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
public class SlotManager {
    @Nullable
    private static SlotManager slotManager = null;

    private final List<Slot> slots = new ArrayList<>();

    private final File slotsFile;
    private final FileConfiguration slotsConfig;

    private SlotManager() {
        this.slotsFile = new File(RPGInventory.getInstance().getDataFolder(), "slots.yml");
        if (!slotsFile.exists()) {
            RPGInventory.getInstance().saveResource("slots.yml", false);
        }

        this.slotsConfig = YamlConfiguration.loadConfiguration(slotsFile);

        for (String key : this.slotsConfig.getConfigurationSection("slots").getKeys(false)) {
            ConfigurationSection config = this.slotsConfig.getConfigurationSection("slots." + key);
            Slot slot;
            Slot.SlotType slotType = Slot.SlotType.valueOf(config.getString("type"));
            if (slotType == Slot.SlotType.ACTION) {
                slot = new ActionSlot(key, config);
            } else {
                slot = new Slot(key, config);
            }

            if (this.validateSlot(slot)) {
                this.slots.add(slot);
            } else {
                RPGInventory.getPluginLogger().warning("Slot " + slot.getName() + " not been added.");
            }
        }
    }

    public static boolean init() {
        try {
            slotManager = new SlotManager();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public static SlotManager getSlotManager() {
        if (slotManager == null) {
            slotManager = new SlotManager();
        }

        return slotManager;
    }

    private boolean validateSlot(@NotNull Slot slot) {
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

    public List<Slot> getQuickSlots() {
        List<Slot> quickSlots = new ArrayList<>();
        for (Slot slot : this.slots) {
            if (slot.isQuick()) {
                quickSlots.add(slot);
            }
        }

        return quickSlots;
    }

    public List<Slot> getPassiveSlots() {
        List<Slot> passiveSlots = new ArrayList<>();
        for (Slot slot : this.slots) {
            if (slot.getSlotType() == Slot.SlotType.PASSIVE || slot.getSlotType() == Slot.SlotType.BACKPACK) {
                passiveSlots.add(slot);
            }
        }

        return passiveSlots;
    }

    public List<Slot> getActiveSlots() {
        List<Slot> activeSlots = new ArrayList<>();
        for (Slot slot : this.slots) {
            if (slot.getSlotType() == Slot.SlotType.ACTIVE) {
                activeSlots.add(slot);
            }
        }

        return activeSlots;
    }

    public List<Slot> getArmorSlots() {
        List<Slot> armorSlots = new ArrayList<>();
        for (Slot slot : this.slots) {
            if (slot.getSlotType() == Slot.SlotType.ARMOR) {
                armorSlots.add(slot);
            }
        }

        return armorSlots;
    }

    public List<Slot> getInfoSlots() {
        List<Slot> infoSlots = new ArrayList<>();
        for (Slot slot : this.slots) {
            if (slot.getSlotType() == Slot.SlotType.INFO) {
                infoSlots.add(slot);
            }
        }

        return infoSlots;
    }

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
            this.slotsConfig.save(this.slotsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
