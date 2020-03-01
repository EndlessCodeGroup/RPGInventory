package ru.endlesscode.rpginventory.item;

import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.jetbrains.annotations.NotNull;
import ru.endlesscode.rpginventory.compat.MaterialCompat;
import ru.endlesscode.rpginventory.misc.config.Config;
import ru.endlesscode.rpginventory.misc.config.TexturesType;
import ru.endlesscode.rpginventory.utils.ItemUtils;
import ru.endlesscode.rpginventory.utils.Log;
import ru.endlesscode.rpginventory.utils.NbtFactoryMirror;

import java.util.Objects;

public class Texture {

    private static final Texture EMPTY_TEXTURE = new Texture(new ItemStack(Material.AIR));

    @NotNull
    private final ItemStack prototype;
    private final int damage;

    private Texture(@NotNull ItemStack prototype) {
        this(prototype, (short) -1);
    }

    private Texture(@NotNull ItemStack prototype, int damage) {
        this.prototype = prototype;
        this.damage = damage;
    }

    public boolean isEmpty() {
        return this.equals(EMPTY_TEXTURE);
    }

    @NotNull
    public ItemStack getItemStack() {
        return prototype.clone();
    }

    public int getDamage() {
        return damage;
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

        Material material = MaterialCompat.getMaterialOrNull(textureParts[0]);
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
                return parseLegacyMonsterEgg(item, textureParts[1]);
            } else if (material.name().startsWith("LEATHER_")) {
                return parseLeatherArmor(item, textureParts[1]);
            } else {
                return parseItemWithModifier(item, textureParts[1]);
            }
        }

        return new Texture(item);
    }

    private static Texture parseLegacyMonsterEgg(ItemStack item, String entityType) {
        NbtCompound nbt = NbtFactoryMirror.fromItemCompound(item);
        nbt.put(ItemUtils.ENTITY_TAG, NbtFactory.ofCompound("temp").put("id", entityType));

        return new Texture(item);
    }

    private static Texture parseLeatherArmor(ItemStack item, String hexColor) {
        try {
            LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
            assert meta != null;
            meta.setColor(Color.fromRGB(Integer.parseInt(hexColor, 16)));
            item.setItemMeta(meta);
        } catch (ClassCastException | IllegalArgumentException | NullPointerException e) {
            Log.w("Can''t parse leather color: {0}", e.toString());
        }

        return new Texture(item);
    }

    private static Texture parseItemWithModifier(ItemStack item, String textureModifierValue) {
        if (item.getItemMeta() == null) {
            return new Texture(item);
        }

        int textureModifier = -1;
        try {
            textureModifier = Integer.parseInt(textureModifierValue);
        } catch (NumberFormatException e) {
            Log.w("Can''t parse texture modifier. Specify a number instead of \"{0}\"", textureModifier);
        }

        if (Config.texturesType == TexturesType.DAMAGE) {
            return parseItemWithDurability(item, textureModifier);
        }
        return parseItemWithCustomModelData(item, textureModifier);
    }

    private static Texture parseItemWithDurability(ItemStack item, int damage) {
        ItemMeta meta = item.getItemMeta();
        assert meta != null;

        meta.addItemFlags(ItemFlag.values());
        if (damage != -1) {
            ((Damageable) meta).setDamage(damage);
            if (ItemUtils.isItemHasDurability(item)) {
                meta.setUnbreakable(true);
            }
        }
        item.setItemMeta(meta);

        return new Texture(item, damage);
    }

    private static Texture parseItemWithCustomModelData(ItemStack item, int customModelData) {
        ItemMeta meta = item.getItemMeta();
        assert meta != null;

        meta.addItemFlags(ItemFlag.values());
        if (customModelData != -1) {
            meta.setCustomModelData(customModelData);
        }
        item.setItemMeta(meta);

        return new Texture(item);
    }
}
