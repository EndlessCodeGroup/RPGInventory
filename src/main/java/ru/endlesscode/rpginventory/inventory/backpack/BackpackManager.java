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

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import ru.endlesscode.rpginventory.RPGInventory;
import ru.endlesscode.rpginventory.event.listener.BackpackListener;
import ru.endlesscode.rpginventory.inventory.InventoryManager;
import ru.endlesscode.rpginventory.inventory.slot.Slot;
import ru.endlesscode.rpginventory.inventory.slot.SlotManager;
import ru.endlesscode.rpginventory.misc.Config;
import ru.endlesscode.rpginventory.utils.ItemUtils;

/**
 * Created by OsipXD on 05.10.2015
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
public class BackpackManager {
    private static final HashMap<String, BackpackType> BACKPACK_TYPES = new HashMap<>();
    private static final HashMap<UUID, Backpack> BACKPACKS = new HashMap<>();
    private static int BACKPACK_LIMIT;

    public static boolean init(RPGInventory instance) {
        if (!isEnabled()) {
            return false;
        }

        try {
            Path petsFile = RPGInventory.getInstance().getDataPath().resolve("backpacks.yml");
            if (Files.notExists(petsFile)) {
                RPGInventory.getInstance().saveResource("backpacks.yml", false);
            }

            FileConfiguration petsConfig = YamlConfiguration.loadConfiguration(petsFile.toFile());

            BACKPACK_TYPES.clear();
            for (String key : petsConfig.getConfigurationSection("backpacks").getKeys(false)) {
                tryToAddBackpack(key, petsConfig.getConfigurationSection("backpacks." + key));
            }

            BackpackManager.loadBackpacks();
            RPGInventory.getPluginLogger().info(BACKPACK_TYPES.size() + " backpack type(s) has been loaded");
            RPGInventory.getPluginLogger().info(BACKPACKS.size() + " backpack(s) has been loaded");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        if (BACKPACK_TYPES.size() == 0) {
            return false;
        }

        BACKPACK_LIMIT = Config.getConfig().getInt("backpacks.limit", 0);

        // Register events
        instance.getServer().getPluginManager().registerEvents(new BackpackListener(), instance);
        return true;
    }

    private static void tryToAddBackpack(String name, ConfigurationSection config) {
        try {
            BackpackType backpackType = new BackpackType(config);
            BACKPACK_TYPES.put(name, backpackType);
        } catch (Exception e) {
            String message = String.format(
                    "Backpack '%s' can't be added: %s", name, e.getLocalizedMessage());
            RPGInventory.getPluginLogger().warning(message);
        }
    }

    private static boolean isEnabled() {
        return SlotManager.instance().getBackpackSlot() != null;
    }

    public static List<String> getBackpackList() {
        List<String> backpackList = new ArrayList<>();
        backpackList.addAll(BACKPACK_TYPES.keySet());
        return backpackList;
    }

    public static ItemStack getItem(String id) {
        BackpackType backpackType = BACKPACK_TYPES.get(id);
        return backpackType == null ? new ItemStack(Material.AIR) : backpackType.getItem();
    }

    @Contract("_, null -> false")
    public static boolean open(Player player, @Nullable ItemStack bpItem) {
        if (ItemUtils.isEmpty(bpItem)) {
            return false;
        }

        BackpackType type;
        String bpId = ItemUtils.getTag(bpItem, ItemUtils.BACKPACK_TAG);
        if (bpId == null || (type = BackpackManager.getBackpackType(bpId)) == null) {
            return false;
        }

        Backpack backpack;
        String bpUid = ItemUtils.getTag(bpItem, ItemUtils.BACKPACK_UID_TAG);
        UUID uuid = bpUid == null ? null : UUID.fromString(bpUid);
        if (!BACKPACKS.containsKey(uuid)) {
            if (uuid == null) {
                backpack = type.createBackpack();
                ItemUtils.setTag(
                        bpItem, ItemUtils.BACKPACK_UID_TAG, backpack.getUniqueId().toString());
            } else {
                backpack = type.createBackpack(uuid);
            }

            BACKPACKS.put(backpack.getUniqueId(), backpack);
        } else {
            backpack = BACKPACKS.get(uuid);
        }

        backpack.open(player);
        return true;
    }

    @Nullable
    public static BackpackType getBackpackType(String bpId) {
        return BACKPACK_TYPES.get(bpId);
    }

    public static void saveBackpacks() {
        Path folder = RPGInventory.getInstance().getDataPath().resolve("backpacks");

        try {
            Files.createDirectories(folder);
            for (Map.Entry<UUID, Backpack> entry : BACKPACKS.entrySet()) {
                Path bpFile = folder.resolve(entry.getKey().toString() + ".bp");
                BackpackSerializer.saveBackpack(entry.getValue(), bpFile);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadBackpacks() {
        try {
            Path folder = RPGInventory.getInstance().getDataPath().resolve("backpacks");
            Files.createDirectories(folder);

            Files.list(folder)
                    .filter((file) -> Files.isRegularFile(file) && file.toString().endsWith(".bp"))
                    .forEach(BackpackManager::tryToLoadBackpack);
        } catch (IOException | RuntimeException e) {
            e.printStackTrace();
        }
    }

    private static void tryToLoadBackpack(Path path) {
        try {
            loadBackpack(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void loadBackpack(Path path) throws IOException {
        Backpack backpack = BackpackSerializer.loadBackpack(path);
        if (backpack == null || backpack.isOverdue()) {
            Files.delete(path);
        } else {
            BACKPACKS.put(backpack.getUniqueId(), backpack);
        }
    }

    @Contract("null -> false")
    public static boolean isBackpack(ItemStack item) {
        return !ItemUtils.isEmpty(item) && ItemUtils.hasTag(item, ItemUtils.BACKPACK_TAG);
    }

    public static boolean playerCanTakeBackpack(Player player) {
        if (BACKPACK_LIMIT == 0) {
            return true;
        }

        // Check vanilla inventory
        Inventory inventory = player.getInventory();

        int count = 0;
        for (ItemStack item : inventory.getContents()) {
            if (isBackpack(item)) {
                count++;
            }
        }

        // Check RPGInventory slots
        inventory = InventoryManager.get(player).getInventory();
        Slot backpackSlot = SlotManager.instance().getBackpackSlot();
        if (BackpackManager.isBackpack(inventory.getItem(backpackSlot.getSlotId())) && !backpackSlot.isQuick()) {
            count++;
        }

        return count < BACKPACK_LIMIT;
    }

    public static int getLimit() {
        return BACKPACK_LIMIT;
    }
}
