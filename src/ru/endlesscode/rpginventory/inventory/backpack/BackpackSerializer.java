package ru.endlesscode.rpginventory.inventory.backpack;

import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import com.comphenix.protocol.wrappers.nbt.io.NbtBinarySerializer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.endlesscode.rpginventory.utils.FileUtils;
import ru.endlesscode.rpginventory.utils.ItemUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Created by OsipXD on 20.11.2015
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2015 © «EndlessCode Group»
 */
class BackpackSerializer {
    public static void saveBackpack(@NotNull Backpack backpack, @NotNull File file) throws IOException {
        List<NbtCompound> nbtList = new ArrayList<>();

        try (DataOutputStream dataOutput = new DataOutputStream(new GZIPOutputStream(new FileOutputStream(file)))) {
            ItemStack[] contents = backpack.getContents();
            for (int i = 0; i < contents.length; i++) {
                ItemStack item = contents[i];
                nbtList.add(ItemUtils.itemStackToNBT(item == null ? new ItemStack(Material.AIR) : item, i + ""));
            }

            NbtCompound backpackNbt = NbtFactory.ofCompound("Backpack");
            backpackNbt.put(NbtFactory.ofCompound("contents", nbtList));
            backpackNbt.put("type", backpack.getType().getId());
            backpackNbt.put("last-use", backpack.getLastUse());
            NbtBinarySerializer.DEFAULT.serialize(backpackNbt, dataOutput);
        }
    }

    @Nullable
    public static Backpack loadBackpack(@NotNull File file) throws IOException {
        Backpack backpack;
        try (DataInputStream dataInput = new DataInputStream(new GZIPInputStream(new FileInputStream(file)))) {
            NbtCompound nbtList = NbtBinarySerializer.DEFAULT.deserializeCompound(dataInput);

            BackpackType type = BackpackManager.getBackpackType(nbtList.getString("type"));
            if (type == null) {
                return null;
            }

            long lastUse = (nbtList.containsKey("last-use")) ? nbtList.getLong("last-use") : System.currentTimeMillis();
            backpack = new Backpack(type, UUID.fromString(FileUtils.stripExtension(file.getName())));
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
