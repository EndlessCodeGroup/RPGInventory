package ru.endlesscode.rpginventory.utils;

import com.comphenix.protocol.utility.MinecraftReflection;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import ru.endlesscode.rpginventory.inventory.InventoryManager;
import ru.endlesscode.rpginventory.pet.PetManager;
import ru.endlesscode.rpginventory.pet.PetType;

import java.lang.reflect.InvocationTargetException;

/**
 * Created by OsipXD on 02.12.2015
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
public class EntityUtils {
    public static void goPetToPlayer(final Player player, final LivingEntity entity) {
        if (!InventoryManager.playerIsLoaded(player) || !player.isOnline() || entity.isDead()) {
            return;
        }

        Location target = player.getLocation();
        if (target.distance(entity.getLocation()) > 20) {
            PetManager.respawnPet(player);
        } else if (target.distance(entity.getLocation()) < 4) {
            return;
        }

        PetType petType = PetManager.getPetFromEntity((Tameable) entity);
        double speedModifier = petType == null ? 1.0 : 0.4 / petType.getSpeed();

        Class<?> entityInsentientClass = MinecraftReflection.getMinecraftClass("EntityInsentient");
        Class<?> navigationAbstractClass = MinecraftReflection.getMinecraftClass("NavigationAbstract");

        try {
            Object insentient = entityInsentientClass.cast(MinecraftReflection.getCraftEntityClass().getDeclaredMethod("getHandle").invoke(entity));
            Object navigation = entityInsentientClass.getDeclaredMethod("getNavigation").invoke(insentient);
            navigationAbstractClass.getDeclaredMethod("a", double.class, double.class, double.class, double.class)
                    .invoke(navigation, target.getX(), target.getY(), target.getZ(), speedModifier);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }
}
