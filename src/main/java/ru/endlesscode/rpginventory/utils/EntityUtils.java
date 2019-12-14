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

import com.comphenix.protocol.utility.MinecraftReflection;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.endlesscode.inspector.report.Reporter;
import ru.endlesscode.rpginventory.RPGInventory;
import ru.endlesscode.rpginventory.inventory.InventoryManager;
import ru.endlesscode.rpginventory.pet.PetManager;
import ru.endlesscode.rpginventory.pet.PetType;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by OsipXD on 02.12.2015
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
public class EntityUtils {

    private static Method craftEntity_getHandle;
    private static Method navigationAbstract_a;
    private static Method entityInsentient_getNavigation;
    private static Class<?> entityInsentientClass = MinecraftReflection.getMinecraftClass("EntityInsentient");

    private static Reporter reporter = RPGInventory.getInstance().getReporter();

    static {
        try {
            craftEntity_getHandle = MinecraftReflection.getCraftEntityClass().getDeclaredMethod("getHandle");
            entityInsentient_getNavigation = entityInsentientClass.getDeclaredMethod("getNavigation");
            navigationAbstract_a = MinecraftReflection.getMinecraftClass("NavigationAbstract")
                    .getDeclaredMethod("a", double.class, double.class, double.class, double.class);
        } catch (NoSuchMethodException e) {
            reporter.report("Error on EntityUtils initialization", e);
        }
    }

    public static void goPetToPlayer(@NotNull final Player player, @NotNull final LivingEntity entity) {
        if (!InventoryManager.playerIsLoaded(player) || !player.isOnline() || entity.isDead()) {
            return;
        }

        //Issue #120, by 12 Feb 18 : https://github.com/EndlessCodeGroup/RPGInventory/issues/120#issuecomment-364834420
        if (!player.getWorld().getName().equals(entity.getWorld().getName())) {
            PetManager.teleportPet(player, null);
            return;
        }

        Location target = player.getLocation();
        final double distance = target.distance(entity.getLocation());
        if (distance > 20D && LocationUtils.isSafeLocation(target)) {
            PetManager.teleportPet(player, null);
        } else if (distance < 4D) {
            return;
        }

        PetType petType = PetManager.getPetFromEntity(entity, player);
        double speedModifier = petType == null ? 1.0 : 0.4 / petType.getSpeed();

        try {
            Object insentient = entityInsentientClass.cast(craftEntity_getHandle.invoke(entity));
            Object navigation = entityInsentient_getNavigation.invoke(insentient);
            navigationAbstract_a.invoke(navigation, target.getX(), target.getY(), target.getZ(), speedModifier);
        } catch (IllegalAccessException | InvocationTargetException e) {
            reporter.report("Error on going pet to player", e);
        }
    }
}
