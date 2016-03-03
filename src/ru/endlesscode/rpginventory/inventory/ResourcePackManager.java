package ru.endlesscode.rpginventory.inventory;

import org.bukkit.entity.HumanEntity;
import org.jetbrains.annotations.NotNull;
import ru.endlesscode.rpginventory.misc.Config;
import ru.endlesscode.rpginventory.utils.FileUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by OsipXD on 01.10.2015
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2015 © «EndlessCode Group»
 */
public class ResourcePackManager {
    private static final Map<UUID, Boolean> RESOURCE_PACKS_WONT = new HashMap<>();
    private static final Map<UUID, Boolean> RESOURCE_PACKS_LOADED = new HashMap<>();

    private static Mode mode;

    private ResourcePackManager() {
    }

    public static void init() {
        mode = Mode.valueOf(Config.getConfig().getString("resource-pack.mode"));
        FileUtils.jsonToUuidBooleanMap("loaded-rp.tmp~", RESOURCE_PACKS_LOADED);
        FileUtils.jsonToUuidBooleanMap("wont-rp.tmp~", RESOURCE_PACKS_WONT);
    }

    public static void save() {
        FileUtils.uuidBooleanMapToJson(RESOURCE_PACKS_LOADED, "loaded-rp.tmp~");
        FileUtils.uuidBooleanMapToJson(RESOURCE_PACKS_WONT, "wont-rp.tmp~");
    }

    public static Mode getMode() {
        return mode;
    }

    public static void wontResourcePack(@NotNull HumanEntity player, boolean status) {
        RESOURCE_PACKS_WONT.put(player.getUniqueId(), status);
    }

    public static void loadedResourcePack(@NotNull HumanEntity player, boolean status) {
        RESOURCE_PACKS_LOADED.put(player.getUniqueId(), status);
    }

    public static void removePlayer(@NotNull HumanEntity player) {
        RESOURCE_PACKS_LOADED.remove(player.getUniqueId());
        RESOURCE_PACKS_WONT.remove(player.getUniqueId());
    }

    public static boolean isLoadedResourcePack(@NotNull HumanEntity player) {
        return RESOURCE_PACKS_LOADED.containsKey(player.getUniqueId()) && RESOURCE_PACKS_LOADED.get(player.getUniqueId());
    }

    public static boolean isWontResourcePack(@NotNull HumanEntity player) {
        return RESOURCE_PACKS_WONT.containsKey(player.getUniqueId()) && RESOURCE_PACKS_WONT.get(player.getUniqueId());
    }

    public static boolean isPlayerInitialised(@NotNull HumanEntity player) {
        return RESOURCE_PACKS_LOADED.containsKey(player.getUniqueId());
    }

    @SuppressWarnings("unused")
    public enum Mode {
        AUTO, FORCE, DISABLED
    }
}
