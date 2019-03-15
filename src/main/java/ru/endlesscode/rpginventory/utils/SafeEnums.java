package ru.endlesscode.rpginventory.utils;

import org.bukkit.DyeColor;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Ocelot;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class SafeEnums {

    private SafeEnums() {
        // Shouldn't be instantiated
    }

    @Nullable
    public static DyeColor getDyeColor(String name) {
        return safeValueOf(DyeColor.class, name, "color");
    }

    @Nullable
    public static Horse.Color getHorseColor(String name) {
        return safeValueOf(Horse.Color.class, name, "horse color");
    }

    @Nullable
    public static Horse.Style getHorseStyle(String name) {
        return safeValueOf(Horse.Style.class, name, "horse style");
    }

    @Nullable
    public static Ocelot.Type getOcelotType(String name) {
        return safeValueOf(Ocelot.Type.class, name, "ocelot type");
    }

    @Nullable
    private static <T extends Enum<T>> T safeValueOf(Class<T> enumClass, String name, String alias) {
        if (name == null) {
            return null;
        }

        try {
            return Enum.valueOf(enumClass, name.toUpperCase());
        } catch (IllegalArgumentException e) {
            Log.w("Unknown {0}: {1}. Available values: {2}", alias, name, Arrays.toString(enumClass.getEnumConstants()));
            return null;
        }
    }
}
