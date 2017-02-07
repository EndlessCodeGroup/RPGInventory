package ru.endlesscode.rpginventory.inventory;

import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import com.comphenix.protocol.wrappers.nbt.io.NbtBinarySerializer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import ru.endlesscode.rpginventory.event.updater.HealthUpdater;
import ru.endlesscode.rpginventory.inventory.slot.Slot;
import ru.endlesscode.rpginventory.inventory.slot.SlotManager;
import ru.endlesscode.rpginventory.misc.Config;
import ru.endlesscode.rpginventory.utils.ItemUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

class InventorySerializer {
    static void savePlayer(@NotNull Player player, @NotNull PlayerWrapper playerWrapper, @NotNull File file) throws IOException {
        List<NbtCompound> slotList = new ArrayList<>();
        try (DataOutputStream dataOutput = new DataOutputStream(new GZIPOutputStream(new FileOutputStream(file)))) {
            for (Slot slot : SlotManager.getSlotManager().getSlots()) {
                if (slot.getSlotType() == Slot.SlotType.ARMOR) {
                    continue;
                }

                List<NbtCompound> itemList = new ArrayList<>();
                List<Integer> slotIds = slot.getSlotIds();
                Inventory inventory = playerWrapper.getInventory();
                for (int i = 0; i < slotIds.size(); i++) {
                    int slotId = slotIds.get(i);
                    ItemStack itemStack = inventory.getItem(slotId);
                    if (!ItemUtils.isEmpty(itemStack) && !slot.isCup(itemStack)) {
                        itemList.add(ItemUtils.itemStackToNBT(itemStack, i + ""));
                    }
                }

                if (itemList.size() > 0 || playerWrapper.isBuyedSlot(slot.getName())) {
                    NbtCompound slotNbt = NbtFactory.ofCompound(slot.getName());
                    slotNbt.put("type", slot.getSlotType().name());
                    if (playerWrapper.isBuyedSlot(slot.getName())) {
                        slotNbt.put("buyed", "true");
                    }
                    slotNbt.put(NbtFactory.ofCompound("items", itemList));
                    slotList.add(slotNbt);
                }
            }

            NbtCompound playerNbt = NbtFactory.ofCompound("Inventory");
            playerNbt.put(NbtFactory.ofCompound("slots", slotList));
            playerNbt.put("buyed-slots", playerWrapper.getBuyedGenericSlots());

            HealthUpdater healthUpdater = playerWrapper.getHealthUpdater();
            double attributesBonus = healthUpdater.getAttributesBonus();
            double otherPluginsBonus = healthUpdater.getOtherPluginsBonus();
            double initHealth = (attributesBonus == 0 && otherPluginsBonus == 0) ? -1 : healthUpdater.getHealth();

            playerNbt.put("health.current", initHealth == 0 ? player.getHealth() : initHealth);
            playerNbt.put("health.attributes", healthUpdater.getAttributesBonus());
            playerNbt.put("health.other-plugins", healthUpdater.getOtherPluginsBonus());

            NbtBinarySerializer.DEFAULT.serialize(playerNbt, dataOutput);
        }
    }

    static PlayerWrapper loadPlayer(@NotNull Player player, @NotNull File file) throws IOException {
        PlayerWrapper playerWrapper = new PlayerWrapper(player);
        Inventory inventory = playerWrapper.getInventory();

        try (DataInputStream dataInput = new DataInputStream(new GZIPInputStream(new FileInputStream(file)))) {
            NbtCompound playerNbt = NbtBinarySerializer.DEFAULT.deserializeCompound(dataInput);

            // =========== Added in v1.1.8 ============
            if (playerNbt.containsKey("free-slots")) {
                playerWrapper.setBuyedSlots(playerNbt.getInteger("free-slots") - Config.getConfig().getInt("slots.free"));
                playerNbt.remove("free-slots");
            } else {
                playerWrapper.setBuyedSlots(playerNbt.getInteger("buyed-slots"));
                playerNbt.remove("buyed-slots");
            }
            // ========================================

            // =========== Added in v1.3.3 ============
            HealthUpdater healthUpdater = playerWrapper.getHealthUpdater();
            if (playerNbt.containsKey("health.current")) {
                double health = playerNbt.getDouble("health.current");
                if (health != -1) {
                    healthUpdater.setHealth(health);
                }

                healthUpdater.setAttributesBonus(playerNbt.getDouble("health.attributes"));
                healthUpdater.setOtherPluginsBonus(playerNbt.getDouble("health.other-plugins"));
            }
            // ========================================

            // =========== Added in v1.2.1 ============
            NbtCompound itemsNbt = playerNbt.containsKey("slots") ? playerNbt.getCompound("slots") : playerNbt;
            // ========================================

            for (Slot slot : SlotManager.getSlotManager().getSlots()) {
                if (itemsNbt.containsKey(slot.getName())) {
                    NbtCompound slotNbt = itemsNbt.getCompound(slot.getName());
                    if (slot.getSlotType() != Slot.SlotType.valueOf(slotNbt.getString("type"))) {
                        continue;
                    }

                    if (slotNbt.containsKey("buyed")) {
                        playerWrapper.setBuyedSlots(slot.getName());
                    }

                    NbtCompound itemListNbt = slotNbt.getCompound("items");
                    List<ItemStack> itemList = new ArrayList<>();
                    for (String key : itemListNbt.getKeys()) {
                        itemList.add(ItemUtils.nbtToItemStack(itemListNbt.getCompound(key)));
                    }

                    List<Integer> slotIds = slot.getSlotIds();
                    for (int i = 0; i < slotIds.size(); i++) {
                        if (itemList.size() > i) {
                            inventory.setItem(slotIds.get(i), itemList.get(i));
                        }
                    }
                }
            }
        }

        return playerWrapper;
    }
}