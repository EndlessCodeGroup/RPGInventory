package ru.endlesscode.rpginventory.item;

import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.jetbrains.annotations.NotNull;
import ru.endlesscode.rpginventory.compat.CompatMaterial;
import ru.endlesscode.rpginventory.utils.ItemUtils;
import ru.endlesscode.rpginventory.utils.Log;

import java.util.Objects;

public class Texture {

    private static Texture EMPTY_TEXTURE = new Texture(new ItemStack(Material.AIR));

    @NotNull
    private ItemStack prototype;
    private short durability;

    private Texture(@NotNull ItemStack prototype) {
        this(prototype, (short) -1);
    }

    private Texture(@NotNull ItemStack prototype, short durability) {
        this.prototype = prototype;
        this.durability = durability;
    }

    public boolean isEmpty() {
        return this.equals(EMPTY_TEXTURE);
    }

    public ItemStack getItemStack() {
        return prototype.clone();
    }

    public short getDurability() {
        return durability;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof Texture)) {
            return false;
        }

        Texture texture = (Texture) o;
        return prototype.equals(texture.prototype);
    }

    @Override
    public int hashCode() {
        return Objects.hash(prototype);
    }

    public static Texture parseTexture(String texture) {
        if (texture == null) {
            return EMPTY_TEXTURE;
        }

        String[] textureParts = texture.split(":");

        Material material = CompatMaterial.getMaterialOrNull(textureParts[0]);
        if (material == null) {
            Log.w("Unknown material: {0}", textureParts[0]);
            return EMPTY_TEXTURE;
        }

        ItemStack item = ItemUtils.toBukkitItemStack(new ItemStack(material));
        if (ItemUtils.isEmpty(item)) {
            return EMPTY_TEXTURE;
        }

        if (textureParts.length > 1) {
            // MONSTER_EGG before 1.13
            if (material.name().equals("MONSTER_EGG")) {
                return parseLegacyMonesterEgg(item, textureParts[1]);
            } else if (material.name().startsWith("LEATHER_")) {
                return parseLeatherArmor(item, textureParts[1]);
            } else {
                return parseItemWithDurability(item, textureParts[1]);
            }
        }

        return new Texture(item);
    }

    private static Texture parseLegacyMonesterEgg(ItemStack item, String entityType) {
        NbtCompound nbt = NbtFactory.asCompound(NbtFactory.fromItemTag(item));
        nbt.put(ItemUtils.ENTITY_TAG, NbtFactory.ofCompound("temp").put("id", entityType));

        return new Texture(item);
    }

    private static Texture parseLeatherArmor(ItemStack item, String hexColor) {
        try {
            LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
            meta.setColor(Color.fromRGB(Integer.parseInt(hexColor, 16)));
            item.setItemMeta(meta);
        } catch (ClassCastException | IllegalArgumentException | NullPointerException e) {
            Log.w("Can''t parse leather color: {0}", e.toString());
        }

        return new Texture(item);
    }

    private static Texture parseItemWithDurability(ItemStack item, String durabilityValue) {
        short durability = -1;
        try {
            durability = Short.parseShort(durabilityValue);
            item.setDurability(durability);

            if (ItemUtils.isItemHasDurability(item)) {
                item = ItemUtils.setTag(item, ItemUtils.UNBREAKABLE_TAG, "1");
                item = ItemUtils.setTag(item, ItemUtils.HIDE_FLAGS_TAG, "63");
            }
        } catch (NumberFormatException e) {
            Log.w("Can''t parse durability. Specify a number instead of \"{0}\"", durabilityValue);
        }

        return new Texture(item, durability);
    }
}
