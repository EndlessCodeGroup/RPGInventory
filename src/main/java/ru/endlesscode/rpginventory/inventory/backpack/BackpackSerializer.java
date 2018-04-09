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

package ru.endlesscode.rpginventory.inventory.backpack;

import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import com.comphenix.protocol.wrappers.nbt.io.NbtBinarySerializer;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import ru.endlesscode.rpginventory.utils.FileUtils;
import ru.endlesscode.rpginventory.utils.ItemUtils;

/**
 * Created by OsipXD on 20.11.2015
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
class BackpackSerializer {
    static void saveBackpack(Backpack backpack, @NotNull Path file) throws IOException {
        List<NbtCompound> nbtList = new ArrayList<>();

        try (DataOutputStream dataOutput = new DataOutputStream(new GZIPOutputStream(Files.newOutputStream(file)))) {
            ItemStack[] contents = backpack.getContents();
            for (int i = 0; i < contents.length; i++) {
                ItemStack item = contents[i];
                nbtList.add(ItemUtils.itemStackToNBT(ItemUtils.isEmpty(item)
                        ? new ItemStack(Material.AIR)
                        : item, i + ""));
            }

            NbtCompound backpackNbt = NbtFactory.ofCompound("Backpack");
            backpackNbt.put(NbtFactory.ofCompound("contents", nbtList));
            backpackNbt.put("type", backpack.getType().getId());
            backpackNbt.put("last-use", backpack.getLastUse());
            NbtBinarySerializer.DEFAULT.serialize(backpackNbt, dataOutput);
        }
    }

    @Nullable
    static Backpack loadBackpack(@NotNull Path file) throws IOException {
        Backpack backpack;
        try (DataInputStream dataInput = new DataInputStream(new GZIPInputStream(Files.newInputStream(file)))) {
            NbtCompound nbtList = NbtBinarySerializer.DEFAULT.deserializeCompound(dataInput);

            BackpackType type = BackpackManager.getBackpackType(nbtList.getString("type"));
            if (type == null) {
                return null;
            }

            long lastUse = (nbtList.containsKey("last-use")) ? nbtList.getLong("last-use") : System.currentTimeMillis();
            backpack = new Backpack(type, UUID.fromString(FileUtils.stripExtension(file.getFileName().toString())));
            backpack.setLastUse(lastUse);
            NbtCompound itemList = nbtList.getCompound("contents");
            ItemStack[] contents = new ItemStack[type.getSize()];
            for (int i = 0; i < type.getSize() && itemList.containsKey(i + ""); i++) {
                NbtCompound compound = itemList.getCompound(i + "");
                contents[i] = compound == null ? new ItemStack(Material.AIR) : ItemUtils.nbtToItemStack(compound);
            }

            backpack.setContents(contents);
        }

        return backpack;
    }
}
