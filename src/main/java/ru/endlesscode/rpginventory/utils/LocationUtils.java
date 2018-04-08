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
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by OsipXD on 28.08.2015
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
public class LocationUtils {
    public static Location getLocationNearPlayer(Player player, int radius) {
        Block playerBlock = player.getLocation().getBlock();
        List<Location> availableLocations = new ArrayList<>();

        World world = player.getWorld();
        for (int x = playerBlock.getX() - radius; x < playerBlock.getX() + radius; x++) {
            for (int y = playerBlock.getY() - radius; y < playerBlock.getY() + radius; y++) {
                for (int z = playerBlock.getZ() - radius; z < playerBlock.getZ() + radius; z++) {
                    Location loc = getBlockCenter(new Location(world, x, y, z));
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
            return getBlockCenter(playerBlock.getLocation().clone());
        }

        return availableLocations.get(new Random().nextInt(availableLocations.size()));
    }

    public static Location getBlockCenter(Location loc) {
        return loc.add(0.5, 0, 0.5);
    }

    public static boolean isUnderAnyBlockHonestly(@NotNull Location loc, double entityWidth, int distance) {
        for (Block block : getStandingOn(loc, entityWidth)) {
            if (isUnderAnyBlock(block, distance)) {
                return true;
            }
        }

        return false;
    }

    @NotNull
    public static List<Block> getStandingOn(@NotNull Location playerLoc, double entityWidth) {
        double halfWidth = entityWidth / 2;

        List<Block> blocksUnderPlayer = new ArrayList<>(2);
        for (int firstSign = -1; firstSign <= 1; firstSign += 2) {
            for (int secondSign = -1; secondSign <= 1; secondSign += 2) {
                Location blockLoc = playerLoc.clone().add(firstSign * halfWidth, 0, secondSign * halfWidth);
                Block block = blockLoc.getBlock();
                if (!blocksUnderPlayer.contains(block)) {
                    blocksUnderPlayer.add(block);
                }
            }
        }

        return blocksUnderPlayer;
    }

    public static boolean isUnderAnyBlock(@NotNull Block block, int distance) {
        for (int i = 1; i <= distance; i++) {
            Block blockUnderPlayer = block.getRelative(BlockFace.DOWN, i);
            if (!blockUnderPlayer.isEmpty()) {
                return true;
            }
        }

        return false;
    }

    @NotNull
    public static List<Player> getNearbyPlayers(Location location, double distance) {
        List<Player> nearbyPlayers = new ArrayList<>();
        for (LivingEntity entity : location.getWorld().getLivingEntities()) {
            if (entity.getType() == EntityType.PLAYER && entity.getLocation().distance(location) <= distance) {
                nearbyPlayers.add((Player) entity);
            }
        }

        return nearbyPlayers;
    }

    @NotNull
    public static Vector getRandomVector() {
        Vector vector = new Vector();
        vector.setX(0.0D + Math.random() - Math.random());
        vector.setY(Math.random());
        vector.setZ(0.0D + Math.random() - Math.random());

        return vector;
    }
}
