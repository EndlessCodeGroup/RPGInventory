package ru.endlesscode.rpginventory.inventory;

import org.bukkit.entity.HumanEntity;
import org.jetbrains.annotations.NotNull;
import ru.endlesscode.rpginventory.utils.FileUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by OsipXD on 01.10.2015
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
public class ResourcePackManager {
    private static final Map<UUID, Boolean> RESOURCE_PACKS_LOADED = new HashMap<>();

    private ResourcePackManager() {
    }

    public static void init() {
        FileUtils.jsonToUuidBooleanMap("loaded-rp.tmp~", RESOURCE_PACKS_LOADED);
    }

    public static void save() {
        FileUtils.uuidBooleanMapToJson(RESOURCE_PACKS_LOADED, "loaded-rp.tmp~");
    }

    public static void loadedResourcePack(@NotNull HumanEntity player, boolean status) {
        RESOURCE_PACKS_LOADED.put(player.getUniqueId(), status);
    }

    public static void removePlayer(@NotNull HumanEntity player) {
        RESOURCE_PACKS_LOADED.remove(player.getUniqueId());
    }

    public static boolean isLoadedResourcePack(@NotNull HumanEntity player) {
        return RESOURCE_PACKS_LOADED.containsKey(player.getUniqueId()) && RESOURCE_PACKS_LOADED.get(player.getUniqueId());
    }
}