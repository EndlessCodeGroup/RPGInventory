package ru.endlesscode.rpginventory.compat;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import ru.endlesscode.rpginventory.utils.ItemUtils;

public final class ItemStackCompat {

    private static final String UNBREAKABLE_TAG = "Unbreakable";

    private ItemStackCompat() {
    }

    public static ItemStack setUnbreakable(@NotNull ItemStack itemStack, boolean value) {
        if (VersionHandler.getVersionCode() >= VersionHandler.VERSION_1_11) {
            ItemMeta meta = itemStack.getItemMeta();
            meta.setUnbreakable(value);
            itemStack.setItemMeta(meta);
            return itemStack;
        } else {
            return ItemUtils.setTag(itemStack, UNBREAKABLE_TAG, "1");
        }
    }
}
