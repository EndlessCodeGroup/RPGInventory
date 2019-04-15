package ru.endlesscode.rpginventory.compat;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class MaterialCompat {
    @NotNull
    public static Material getMaterial(String name) {
        return Objects.requireNonNull(getMaterialOrNull(name));
    }

    @NotNull
    public static Material getMaterialOrAir(String name) {
        Material material = getMaterialOrNull(name);
        if (material == null) {
            return Material.AIR;
        } else {
            return material;
        }
    }

    @Nullable
    public static Material getMaterialOrNull(String name) {
        Material material = Material.getMaterial(name);
        if (material == null && VersionHandler.getVersionCode() >= VersionHandler.VERSION_1_13) {
            material = Material.getMaterial(name, true);
        }

        return material;
    }
}
