package ru.endlesscode.rpginventory.misc.serialization;

import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.io.NbtBinarySerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.endlesscode.rpginventory.compat.MaterialCompat;
import ru.endlesscode.rpginventory.inventory.PlayerWrapper;
import ru.endlesscode.rpginventory.inventory.backpack.Backpack;
import ru.endlesscode.rpginventory.inventory.backpack.BackpackManager;
import ru.endlesscode.rpginventory.inventory.backpack.BackpackType;
import ru.endlesscode.rpginventory.inventory.slot.Slot;
import ru.endlesscode.rpginventory.inventory.slot.SlotManager;
import ru.endlesscode.rpginventory.utils.FileUtils;
import ru.endlesscode.rpginventory.utils.ItemUtils;
import ru.endlesscode.rpginventory.utils.NbtFactoryMirror;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.zip.GZIPInputStream;

/**
 * Legacy serialization for back compatibility.
 */
@Deprecated
class LegacySerialization {

    @NotNull
    static PlayerWrapper loadPlayer(Player player, @NotNull Path file) throws IOException {
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
                        itemList.add(nbtToItemStack(itemListNbt.getCompound(key)));
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
                contents[i] = compound == null ? new ItemStack(Material.AIR) : nbtToItemStack(compound);
            }

            backpack.setContents(contents);
        }

        return backpack;
    }


    @NotNull
    private static ItemStack nbtToItemStack(NbtCompound nbt) {
        ItemStack item = new ItemStack(MaterialCompat.getMaterialOrAir(nbt.getString("material")));

        if (ItemUtils.isNotEmpty(item)) {
            item.setAmount(nbt.getInteger("amount"));
            item.setDurability(nbt.getShort("data"));

            if (nbt.containsKey("tag")) {
                item = ItemUtils.toBukkitItemStack(item);
                if (ItemUtils.isNotEmpty(item)) {
                    NbtFactoryMirror.setItemTag(item, nbt.getCompound("tag"));
                }
            }
        }

        return item;
    }

}
