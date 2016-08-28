package ru.endlesscode.rpginventory.pet;

import com.comphenix.protocol.utility.MinecraftReflection;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    Attributes(@NotNull LivingEntity entity) {
        try {
            Method handleMethod = entity.getClass().getMethod("getHandle");
            this.entity = handleMethod.invoke(entity);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
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
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }
}
