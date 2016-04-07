package ru.endlesscode.rpginventory.nms;

import com.comphenix.protocol.utility.MinecraftReflection;
import org.bukkit.Bukkit;

/**
 * Created by OsipXD on 29.08.2015
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2015 © «EndlessCode Group»
 */
public class VersionHandler {
    public static boolean is1_8_X() {
        return Bukkit.getVersion().contains("1.8") && MinecraftReflection.getMinecraftPackage().contains("R3");
    }

    public static boolean is1_8() {
        return Bukkit.getVersion().contains("1.8") && MinecraftReflection.getMinecraftPackage().contains("R1");
    }

    public static boolean is1_7_10() {
        return Bukkit.getVersion().contains("1.7") && MinecraftReflection.getMinecraftPackage().contains("R4");
    }

    public static boolean is1_9() {
        return Bukkit.getVersion().contains("1.9");
    }
}
