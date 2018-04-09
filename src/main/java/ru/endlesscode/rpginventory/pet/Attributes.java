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

package ru.endlesscode.rpginventory.pet;

import com.comphenix.protocol.utility.MinecraftReflection;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.*;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

public class Attributes {
    public static final String SPEED_MODIFIER = "RPGInventory Speed Bonus";
    public static final UUID SPEED_MODIFIER_ID = UUID.fromString("2deaf4fc-1673-4c5b-ac4f-25e37e08760f");

    static final double ONE_BPS = 0.10638297872;
    static final double GALLOP_MULTIPLIER = 4.46808510803;

    @Nullable
    private Object entity = null;

    Attributes(LivingEntity entity) {
        try {
            Method handleMethod = entity.getClass().getMethod("getHandle");
            this.entity = handleMethod.invoke(entity);
        } catch (@NotNull NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public void setSpeed(double speed) {
        String type = "MOVEMENT_SPEED";

        this.setAttribute(type, speed);
    }

    private void setAttribute(@NotNull String type, double speed) {
        try {
            Method getAttributeMethod = MinecraftReflection.getMinecraftClass("EntityLiving")
                    .getMethod("getAttributeInstance", MinecraftReflection.getMinecraftClass("IAttribute"));

            Field iAttributeField = MinecraftReflection.getMinecraftClass("GenericAttributes").getField(type);
            Object attribute = getAttributeMethod.invoke(this.entity, iAttributeField.get(null));

            Method aMethod = MinecraftReflection.getMinecraftClass("AttributeInstance").getMethod("setValue", double.class);
            aMethod.invoke(attribute, speed);
        } catch (@NotNull NoSuchMethodException | IllegalAccessException | InvocationTargetException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }
}
