package ru.endlesscode.rpginventory.utils;

import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.BukkitConverters;
import com.comphenix.protocol.wrappers.nbt.NbtBase;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import com.comphenix.protocol.wrappers.nbt.NbtWrapper;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * For some kind of shit, ProtocolLib's method works wrong in version 4.4.0 But if I just copy
 * their code to my class it works fine. Recompiling of ProtocolLib also helps. (╯°□°)╯︵ ┻━┻
 * TODO: Remove when new version of ProtocolLib will be released.
 * Track progress of the issue here: https://github.com/dmulloy2/ProtocolLib/issues/587
 */
public final class NbtFactoryMirror {

    private static StructureModifier<Object> itemStackModifier;

    private NbtFactoryMirror() {
    }

    public static NbtCompound fromItemCompound(ItemStack stack) {
        return NbtFactory.asCompound(fromItemTag(stack));
    }

    public static void setItemTag(ItemStack stack, NbtCompound compound) {
        checkItemStack(stack);
        StructureModifier<NbtBase<?>> modifier = getStackModifier(stack);
        modifier.write(0, compound);
    }

    private static NbtWrapper<?> fromItemTag(ItemStack stack) {
        checkItemStack(stack);
        StructureModifier<NbtBase<?>> modifier = getStackModifier(stack);
        NbtBase<?> result = modifier.read(0);
        if (result == null) {
            result = com.comphenix.protocol.wrappers.nbt.NbtFactory.ofCompound("tag");
            modifier.write(0, result);
        }

        return NbtFactory.fromBase((NbtBase<?>) result);
    }

    private static void checkItemStack(ItemStack stack) {
        if (stack == null) {
            throw new IllegalArgumentException("Stack cannot be NULL.");
        } else if (!MinecraftReflection.isCraftItemStack(stack)) {
            throw new IllegalArgumentException("Stack must be a CraftItemStack.");
        } else if (stack.getType() == Material.AIR) {
            throw new IllegalArgumentException("ItemStacks representing air cannot store NMS information.");
        }
    }

    private static StructureModifier<NbtBase<?>> getStackModifier(ItemStack stack) {
        Object nmsStack = MinecraftReflection.getMinecraftItemStack(stack);
        if (itemStackModifier == null) {
            itemStackModifier = new StructureModifier<>(nmsStack.getClass(), Object.class, false);
        }

        return itemStackModifier.withTarget(nmsStack).withType(MinecraftReflection.getNBTBaseClass(), BukkitConverters.getNbtConverter());
    }
}
