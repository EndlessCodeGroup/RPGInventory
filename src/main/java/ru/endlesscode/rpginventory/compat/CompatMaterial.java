package ru.endlesscode.rpginventory.compat;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class CompatMaterial {
    @NotNull
    public static Material getMaterial(String name) {
        return Objects.requireNonNull(getMaterialOrNull(name));
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
