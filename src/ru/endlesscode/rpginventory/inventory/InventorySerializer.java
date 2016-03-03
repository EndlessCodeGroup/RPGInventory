package ru.endlesscode.rpginventory.inventory;

import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import com.comphenix.protocol.wrappers.nbt.io.NbtBinarySerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
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
    public static void savePlayer(@NotNull Player player, @NotNull InventoryWrapper inventoryWrapper, @NotNull File file) throws IOException {
        List<NbtCompound> nbtList = new ArrayList<>();

        try (DataOutputStream dataOutput = new DataOutputStream(new GZIPOutputStream(new FileOutputStream(file)))) {
            for (Slot slot : SlotManager.getSlotManager().getSlots()) {
                if (slot.getSlotType() == Slot.SlotType.ARMOR) {
                    continue;
                }

                List<NbtCompound> itemList = new ArrayList<>();
                List<Integer> slotIds = slot.getSlotIds();
                Inventory inventory = inventoryWrapper.getInventory();
                for (int i = 0; i < slotIds.size(); i++) {
                    int slotId = slotIds.get(i);
                    ItemStack itemStack = inventory.getItem(slotId);
                    if (itemStack != null && itemStack.getType() != Material.AIR && !slot.isCup(itemStack)) {
                        itemList.add(ItemUtils.itemStackToNBT(itemStack, i + ""));
                    }
                }

                if (itemList.size() > 0 || inventoryWrapper.isBuyedSlot(slot.getName())) {
                    NbtCompound slotNbt = NbtFactory.ofCompound(slot.getName());
                    slotNbt.put("type", slot.getSlotType().name());
                    if (inventoryWrapper.isBuyedSlot(slot.getName())) {
                        slotNbt.put("buyed", "true");
                    }
                    slotNbt.put(NbtFactory.ofCompound("items", itemList));
                    nbtList.add(slotNbt);
                }
            }

            NbtCompound itemList = NbtFactory.ofCompound("Inventory");
            itemList.put(NbtFactory.ofCompound("slots", nbtList));
            itemList.put("buyed-slots", inventoryWrapper.getBuyedGenericSlots());
            itemList.put("resource-pack", Boolean.toString(ResourcePackManager.isWontResourcePack(player)));

            NbtBinarySerializer.DEFAULT.serialize(itemList, dataOutput);
        }
    }

    public static InventoryWrapper loadPlayer(@NotNull Player player, @NotNull File file) throws IOException {
        InventoryWrapper inventoryWrapper = new InventoryWrapper(player);
        Inventory inventory = inventoryWrapper.getInventory();

        try (DataInputStream dataInput = new DataInputStream(new GZIPInputStream(new FileInputStream(file)))) {
            NbtCompound inventoryNbt = NbtBinarySerializer.DEFAULT.deserializeCompound(dataInput);

            // =========== Added in v1.1.8 ============
            if (inventoryNbt.containsKey("free-slots")) {
                inventoryWrapper.setBuyedSlots(inventoryNbt.getInteger("free-slots") - Config.getConfig().getInt("slots.free"));
                inventoryNbt.remove("free-slots");
            } else {
                inventoryWrapper.setBuyedSlots(inventoryNbt.getInteger("buyed-slots"));
                inventoryNbt.remove("buyed-slots");
            }
            // ========================================

            // =========== Added in v1.1.7 ============
            if (inventoryNbt.containsKey("resource-pack")) {
                ResourcePackManager.wontResourcePack(player, Boolean.parseBoolean(inventoryNbt.getString("resource-pack"))
                        && ResourcePackManager.getMode() != ResourcePackManager.Mode.DISABLED);
            } else {
                ResourcePackManager.wontResourcePack(player, ResourcePackManager.getMode() != ResourcePackManager.Mode.DISABLED);
            }
            // ========================================

            // =========== Added in v1.2.1 ============
            NbtCompound itemsNbt = inventoryNbt.containsKey("slots") ? inventoryNbt.getCompound("slots") : inventoryNbt;
            // ========================================

            for (Slot slot : SlotManager.getSlotManager().getSlots()) {
                if (itemsNbt.containsKey(slot.getName())) {
                    NbtCompound slotNbt = itemsNbt.getCompound(slot.getName());
                    if (slot.getSlotType() != Slot.SlotType.valueOf(slotNbt.getString("type"))) {
                        continue;
                    }

                    if (slotNbt.containsKey("buyed")) {
                        inventoryWrapper.setBuyedSlots(slot.getName());
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

        return inventoryWrapper;
    }
}