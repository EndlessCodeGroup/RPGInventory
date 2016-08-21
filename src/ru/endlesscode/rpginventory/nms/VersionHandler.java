package ru.endlesscode.rpginventory.nms;

import com.comphenix.protocol.utility.MinecraftReflection;
import org.bukkit.Bukkit;

/**
 * Created by OsipXD on 29.08.2015
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
public class VersionHandler {
    // 1.8
    public static boolean is1_8_R1() {
        return Bukkit.getVersion().contains("1.8") && MinecraftReflection.getMinecraftPackage().contains("R1");
    }

    // 1.7.10
    public static boolean is1_7_R4() {
        return Bukkit.getVersion().contains("1.7") && MinecraftReflection.getMinecraftPackage().contains("R4");
    }

    // 1.8.x
    private static boolean is1_8() {
        return Bukkit.getVersion().contains("1.8");
    }

    // 1.9.x
    public static boolean is1_9() {
        return Bukkit.getVersion().contains("1.9");
    }

    // 1.10.x
    private static boolean is1_10() {
        return Bukkit.getVersion().contains("1.10");
    }

    public static boolean checkVersion() {
        return is1_7_R4() || is1_8() || is1_9() || is1_10();
    }

    public static boolean isHigher1_9() {
        return is1_9() || is1_10();
    }
}
