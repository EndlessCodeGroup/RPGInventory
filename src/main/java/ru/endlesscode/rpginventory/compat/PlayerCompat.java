package ru.endlesscode.rpginventory.compat;

import org.bukkit.entity.Player;

public final class PlayerCompat {

    private static boolean isGetWidthMethodAvailable = VersionHandler.getVersionCode() >= VersionHandler.VERSION_1_11;

    private PlayerCompat() {
    }

    public static double getWidth(Player player) {
        double playerWidth = 0.6D;
        if (isGetWidthMethodAvailable) {
            try {
                playerWidth = player.getWidth();
            } catch (NoSuchMethodError ex) {
                isGetWidthMethodAvailable = false;
            }
        }
        return playerWidth;
    }
}
