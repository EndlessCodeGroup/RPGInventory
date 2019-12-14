package ru.endlesscode.rpginventory.utils;

import org.bukkit.DyeColor;
import org.bukkit.entity.Cat;
import org.bukkit.entity.Horse;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class SafeEnums {

    private SafeEnums() {
        // Shouldn't be instantiated
    }

    @Nullable
    public static DyeColor getDyeColor(String name) {
        return valueOf(DyeColor.class, name, "color");
    }

    @Nullable
    public static Horse.Color getHorseColor(String name) {
        return valueOf(Horse.Color.class, name, "horse color");
    }

    @Nullable
    public static Horse.Style getHorseStyle(String name) {
        return valueOf(Horse.Style.class, name, "horse style");
    }

    @Nullable
    public static Cat.Type getCatType(String name) {
        return valueOf(Cat.Type.class, name, "cat type");
    }

    @NotNull
    public static <T extends Enum<T>> T valueOfOrDefault(Class<T> enumClass, String name, T defaultValue) {
        return valueOfOrDefault(enumClass, name, defaultValue, enumClass.getSimpleName());
    }

    @NotNull
    public static <T extends Enum<T>> T valueOfOrDefault(Class<T> enumClass, String name, T defaultValue, String alias) {
        T value = valueOf(enumClass, name, alias);
        if (value != null) {
            return value;
        } else {
            Log.w("Used {0} {1} by default.", defaultValue.name(), alias);
            return defaultValue;
        }
    }

    @Nullable
    public static <T extends Enum<T>> T valueOf(Class<T> enumClass, String name, String alias) {
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
