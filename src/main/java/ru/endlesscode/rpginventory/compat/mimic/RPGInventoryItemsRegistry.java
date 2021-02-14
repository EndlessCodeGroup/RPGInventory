package ru.endlesscode.rpginventory.compat.mimic;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.endlesscode.mimic.items.BukkitItemsRegistry;
import ru.endlesscode.rpginventory.RPGInventory;
import ru.endlesscode.rpginventory.item.CustomItem;
import ru.endlesscode.rpginventory.item.ItemManager;

import java.util.Collection;

public class RPGInventoryItemsRegistry implements BukkitItemsRegistry {

    public static String ID = "rpginventory";

    @NotNull
    @Override
    public String getId() {
        return ID;
    }

    @Override
    public boolean isEnabled() {
        return RPGInventory.getInstance().isEnabled();
    }

    @NotNull
    @Override
    public Collection<String> getKnownIds() {
        return ItemManager.getItemList();
    }

    @Nullable
    @Override
    public ItemStack getItem(@NotNull String itemId, int amount) {
        ItemStack item = ItemManager.getItem(itemId);
        if (item.getType() != Material.AIR) {
            item.setAmount(amount);
            return item;
        } else {
            return null;
        }
    }

    @Nullable
    @Override
    public String getItemId(@NotNull ItemStack item) {
        CustomItem customItem = ItemManager.getCustomItem(item);
        if (customItem != null) {
            return customItem.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean isItemExists(@NotNull String itemId) {
        return ItemManager.hasItem(itemId);
    }
}
