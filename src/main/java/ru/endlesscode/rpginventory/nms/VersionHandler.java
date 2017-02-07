package ru.endlesscode.rpginventory.nms;

import org.bukkit.Bukkit;

/**
 * Created by OsipXD on 29.08.2015
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
public class VersionHandler {
    // 1.9.x
    public static boolean is1_9() {
        return Bukkit.getVersion().contains("1.9");
    }

    // 1.10.x
    private static boolean is1_10() {
        return Bukkit.getVersion().contains("1.10");
    }

    public static boolean checkVersion() {
        return is1_9() || is1_10();
    }
}
