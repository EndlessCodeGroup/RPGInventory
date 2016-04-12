package ru.endlesscode.rpginventory.pet;

import com.comphenix.protocol.utility.MinecraftReflection;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.endlesscode.rpginventory.nms.VersionHandler;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

class Attributes {
    static final double BASE_SPEED = 0.30000001192092896D;

    private static final UUID MOVEMENT_SPEED_UID = UUID.fromString("2deaf4fc-1673-4c5b-ac4f-25e37e08760f");

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
        String type = "d";
        if (!VersionHandler.is1_7_10() && !VersionHandler.is1_8()) {
            type = "MOVEMENT_SPEED";
        }

        this.setAttributeMultiplier(type, MOVEMENT_SPEED_UID, "RPGInventory movement speed", speed, 1);
    }

    private void setAttributeMultiplier(@NotNull String type, UUID uid, String desc, double multiplier, int xz) {
        try {
            Method getAttributeMethod = MinecraftReflection.getMinecraftClass("EntityLiving")
                    .getMethod("getAttributeInstance", MinecraftReflection.getMinecraftClass("IAttribute"));

            Field iAttributeField = MinecraftReflection.getMinecraftClass("GenericAttributes").getField(type);
            Object attributes = getAttributeMethod.invoke(this.entity, iAttributeField.get(null));
            Constructor modifierConstructor = MinecraftReflection.getAttributeModifierClass().getConstructor(UUID.class, String.class, double.class, int.class);
            Object modifier = modifierConstructor.newInstance(uid, desc, multiplier, xz);

            Method aMethod = MinecraftReflection.getMinecraftClass("AttributeInstance").getMethod("a", MinecraftReflection.getAttributeModifierClass());
            aMethod.invoke(attributes, modifier);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }
}
