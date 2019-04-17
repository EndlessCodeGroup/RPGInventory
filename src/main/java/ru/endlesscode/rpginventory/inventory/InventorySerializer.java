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
import com.comphenix.protocol.wrappers.nbt.io.NbtBinarySerializer;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.endlesscode.rpginventory.inventory.slot.Slot;
import ru.endlesscode.rpginventory.inventory.slot.SlotManager;
import ru.endlesscode.rpginventory.misc.serialization.InventorySnapshot;
import ru.endlesscode.rpginventory.utils.FileUtils;
import ru.endlesscode.rpginventory.utils.ItemUtils;
import ru.endlesscode.rpginventory.utils.Log;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

class InventorySerializer {

    private static final String INV = "inventory";

    static void savePlayer(@NotNull PlayerWrapper playerWrapper, @NotNull Path file) throws IOException {
        final YamlConfiguration serializedInventory = new YamlConfiguration();
        serializedInventory.set(INV, InventorySnapshot.create(playerWrapper));

        Path tempFile = Files.createTempFile(file.getParent(), file.getFileName().toString(), null);
        try (OutputStream stream = Files.newOutputStream(tempFile)) {
            stream.write(serializedInventory.saveToString().getBytes());
        }
        Files.move(tempFile, file, StandardCopyOption.REPLACE_EXISTING);
    }

    @Nullable
    static PlayerWrapper loadPlayerOrNull(Player player, @NotNull Path file) {
        try {
            try {
                return loadPlayer(player, file);
            } catch (InvalidConfigurationException e) {
                Log.w("Can''t load {0}''s inventory. Trying to use legacy loader.", player.getName());
                return legacyLoadPlayer(player, file);
            }
        } catch (IOException e) {
            Log.d(e);
            FileUtils.resolveException(file);
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @NotNull
    private static PlayerWrapper loadPlayer(Player player, @NotNull Path file) throws IOException, InvalidConfigurationException {
        final YamlConfiguration serializedInventory = new YamlConfiguration();
        try (InputStreamReader reader = new InputStreamReader(Files.newInputStream(file))) {
            serializedInventory.load(reader);
        }

        InventorySnapshot inventorySnapshot = (InventorySnapshot) serializedInventory.get(INV);
        return inventorySnapshot.restore(player);
    }

    @NotNull
    private static PlayerWrapper legacyLoadPlayer(Player player, @NotNull Path file) throws IOException {
        PlayerWrapper playerWrapper = new PlayerWrapper(player);
        Inventory inventory = playerWrapper.getInventory();

        try (DataInputStream dataInput = new DataInputStream(new GZIPInputStream(Files.newInputStream(file)))) {
            NbtCompound playerNbt = NbtBinarySerializer.DEFAULT.deserializeCompound(dataInput);

            playerWrapper.setBuyedSlots(playerNbt.getInteger("buyed-slots"));
            playerNbt.remove("buyed-slots");

            NbtCompound itemsNbt = playerNbt.containsKey("slots") ? playerNbt.getCompound("slots") : playerNbt;

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
