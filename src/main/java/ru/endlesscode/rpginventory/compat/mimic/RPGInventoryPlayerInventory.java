package ru.endlesscode.rpginventory.compat.mimic;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import ru.endlesscode.mimic.inventory.BukkitPlayerInventory;
import ru.endlesscode.rpginventory.api.InventoryAPI;
import ru.endlesscode.rpginventory.inventory.slot.Slot;
import ru.endlesscode.rpginventory.inventory.slot.SlotManager;

import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("UnstableApiUsage")
public class RPGInventoryPlayerInventory extends BukkitPlayerInventory {

    public RPGInventoryPlayerInventory(@NotNull Player player) {
        super(player);
    }

    @NotNull
    @Override
    public List<ItemStack> getEquippedItems() {
        List<ItemStack> passiveItems = InventoryAPI.getPassiveItems(getPlayer());
        return collectEquippedItems(passiveItems);
    }

    @NotNull
    @Override
    public List<ItemStack> getStoredItems() {
        List<Slot> quickSlots = SlotManager.instance().getQuickSlots();

        return collectStoredItems()
                .stream()
                .filter(item -> quickSlots.stream().noneMatch(slot -> slot.isCup(item)))
                .collect(Collectors.toList());
    }
}
