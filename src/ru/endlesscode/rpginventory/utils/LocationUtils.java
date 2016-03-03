package ru.endlesscode.rpginventory.utils;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by OsipXD on 28.08.2015
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2015 © «EndlessCode Group»
 */
public class LocationUtils {
    public static List<Player> getNearbyPlayers(Location location, double distance) {
        List<Player> nearbyPlayers = new ArrayList<>();
        for (LivingEntity entity : location.getWorld().getLivingEntities()) {
            if (entity.getType() == EntityType.PLAYER && entity.getLocation().distance(location) <= distance) {
                nearbyPlayers.add((Player) entity);
            }
        }

        return nearbyPlayers;
    }

    public static Location getLocationNearPlayer(@NotNull Player player, int radius) {
        Location playerLoc = player.getLocation();
        List<Location> availableLocations = new ArrayList<>();

        for (int x = playerLoc.getBlockX() - radius; x < playerLoc.getBlockX() + radius; x++) {
            for (int y = playerLoc.getBlockY() - radius; y < playerLoc.getBlockY() + radius; y++) {
                for (int z = playerLoc.getBlockZ() - radius; z < playerLoc.getBlockZ() + radius; z++) {
                    Location loc = new Location(player.getWorld(), x, y, z, (float) (-180 + Math.random() * 360), 0.0F);
                    if (loc.getBlock().isEmpty()) {
                        Location underLoc = new Location(player.getWorld(), x, y - 1, z);
                        if (!underLoc.getBlock().isEmpty() && !underLoc.getBlock().isLiquid()) {
                            availableLocations.add(loc);
                        }
                    }
                }
            }
        }

        if (availableLocations.size() == 0) {
            return playerLoc;
        }

        return availableLocations.get(new Random().nextInt(availableLocations.size()));
    }

    public static Vector getRandomVector() {
        Vector vector = new Vector();
        vector.setX(0.0D + Math.random() - Math.random());
        vector.setY(Math.random());
        vector.setZ(0.0D + Math.random() - Math.random());

        return vector;
    }
}
