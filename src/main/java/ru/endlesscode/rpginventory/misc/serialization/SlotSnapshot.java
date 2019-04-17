package ru.endlesscode.rpginventory.misc.serialization;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import ru.endlesscode.rpginventory.inventory.PlayerWrapper;
import ru.endlesscode.rpginventory.inventory.slot.Slot;
import ru.endlesscode.rpginventory.utils.ItemUtils;
import ru.endlesscode.rpginventory.utils.Log;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SlotSnapshot implements ConfigurationSerializable {

    private static final String SLOT_TYPE = "type";
    private static final String SLOT_BOUGHT = "bought";
    private static final String SLOT_ITEMS = "items";

    private String name;
    private String type;
    private boolean bought;
    private List<ItemStack> items;

    private SlotSnapshot(@NotNull String name, @NotNull String type, boolean bought, @NotNull List<ItemStack> items) {
        this.name = name;
        this.type = type;
        this.bought = bought;
        this.items = items;
    }

    @NotNull
    public static SlotSnapshot create(@NotNull Slot slot, @NotNull PlayerWrapper playerWrapper) {
        boolean bought = playerWrapper.isBuyedSlot(slot.getName());

        final Inventory inventory = playerWrapper.getInventory();
        final List<ItemStack> items = slot.getSlotIds().stream()
                .map(inventory::getItem)
                .filter(stack -> ItemUtils.isNotEmpty(stack) && !slot.isCup(stack))
                .collect(Collectors.toList());

        return new SlotSnapshot(slot.getName(), slot.getSlotType().name(), bought, items);
    }

    @NotNull
    public static SlotSnapshot deserialize(@NotNull Map<String, Object> map) {
        String type = (String) map.getOrDefault(SLOT_TYPE, "");
        boolean bought = map.containsKey(SLOT_BOUGHT);
        List<ItemStack> items = (List<ItemStack>) map.getOrDefault(SLOT_ITEMS, Collections.emptyList());

        return new SlotSnapshot("", type, bought, items);
    }

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        final Map<String, Object> serializedSlot = new LinkedHashMap<>();
        serializedSlot.put(SLOT_TYPE, this.type);
        serializedSlot.put(SLOT_ITEMS, this.items);
        if (this.bought) {
            serializedSlot.put(SLOT_BOUGHT, true);
        }

        return serializedSlot;
    }

    void restore(@NotNull PlayerWrapper playerWrapper, @NotNull Slot slot) {
        if (!slot.getSlotType().name().equals(type)) {
            Log.w("Slot ''{0}'' skipped. Wrong type of saved slot: {1}", slot.getName(), type);
            return;
        }

        if (bought) {
            playerWrapper.setBuyedSlots(slot.getName());
        }

        final Inventory inventory = playerWrapper.getInventory();
        final List<Integer> slotIds = slot.getSlotIds();
        for (int i = 0; i < Math.min(slotIds.size(), items.size()); i++) {
            inventory.setItem(slotIds.get(i), items.get(i));
        }
    }

    boolean shouldBeSaved() {
        return !items.isEmpty() || bought;
    }

    public String getName() {
        return name;
    }
}
