/*
 * This file is part of RPGInventory.
 * Copyright (C) 2015-2017 Osip Fatkullin
 *
 * RPGInventory is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * RPGInventory is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with RPGInventory.  If not, see <http://www.gnu.org/licenses/>.
 */

package ru.endlesscode.rpginventory.utils;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
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
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
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

        World world = player.getWorld();
        for (int x = playerLoc.getBlockX() - radius; x < playerLoc.getBlockX() + radius; x++) {
            for (int y = playerLoc.getBlockY() - radius; y < playerLoc.getBlockY() + radius; y++) {
                for (int z = playerLoc.getBlockZ() - radius; z < playerLoc.getBlockZ() + radius; z++) {
                    Location loc = new Location(world, x + 0.5, y, z + 0.5);
                    if (loc.getBlock().isEmpty()) {
                        Block underBlock = loc.clone().subtract(0, 1, 0).getBlock();
                        if (!underBlock.isEmpty() && !underBlock.isLiquid()) {
                            loc.setYaw((float) (-180 + Math.random() * 360));
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
