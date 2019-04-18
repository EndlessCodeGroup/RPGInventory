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

package ru.endlesscode.rpginventory.inventory;

import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import com.comphenix.protocol.wrappers.nbt.io.NbtBinarySerializer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.endlesscode.rpginventory.inventory.slot.Slot;
import ru.endlesscode.rpginventory.inventory.slot.SlotManager;
import ru.endlesscode.rpginventory.misc.config.Config;
import ru.endlesscode.rpginventory.utils.FileUtils;
import ru.endlesscode.rpginventory.utils.ItemUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

class InventorySerializer {
    static void savePlayer(@NotNull PlayerWrapper playerWrapper, @NotNull Path file) throws IOException {
        List<NbtCompound> slotList = new ArrayList<>();
        try (DataOutputStream dataOutput = new DataOutputStream(new GZIPOutputStream(Files.newOutputStream(file)))) {
            for (Slot slot : SlotManager.instance().getSlots()) {
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

            NbtBinarySerializer.DEFAULT.serialize(playerNbt, dataOutput);
        }
    }

    @Nullable
    static PlayerWrapper loadPlayerOrNull(Player player, @NotNull Path file) {
        try {
            return loadPlayer(player, file);
        } catch (IOException e) {
            FileUtils.resolveException(file);
            return null;
        }
    }

    @NotNull
    static PlayerWrapper loadPlayer(Player player, @NotNull Path file) throws IOException {
        PlayerWrapper playerWrapper = new PlayerWrapper(player);
        Inventory inventory = playerWrapper.getInventory();

        try (DataInputStream dataInput = new DataInputStream(new GZIPInputStream(Files.newInputStream(file)))) {
            NbtCompound playerNbt = NbtBinarySerializer.DEFAULT.deserializeCompound(dataInput);

            // =========== Added in v1.1.8 ============
            if (playerNbt.containsKey("free-slots")) {
                int savedFreeSlots = playerNbt.getInteger("free-slots");
                int freeSlotsFromConfig = Config.getConfig().getInt("slots.free");
                playerWrapper.setBuyedSlots(savedFreeSlots - freeSlotsFromConfig);
                playerNbt.remove("free-slots");
            } else {
                playerWrapper.setBuyedSlots(playerNbt.getInteger("buyed-slots"));
                playerNbt.remove("buyed-slots");
            }
            // ========================================

            // =========== Added in v1.2.1 ============
            NbtCompound itemsNbt = playerNbt.containsKey("slots") ? playerNbt.getCompound("slots") : playerNbt;
            // ========================================

            for (Slot slot : SlotManager.instance().getSlots()) {
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
