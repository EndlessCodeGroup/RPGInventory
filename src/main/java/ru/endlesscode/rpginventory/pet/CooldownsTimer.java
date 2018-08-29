package ru.endlesscode.rpginventory.pet;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import ru.endlesscode.rpginventory.RPGInventory;
import ru.endlesscode.rpginventory.inventory.InventoryManager;
import ru.endlesscode.rpginventory.inventory.slot.Slot;
import ru.endlesscode.rpginventory.inventory.slot.SlotManager;
import ru.endlesscode.rpginventory.utils.ItemUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Dereku on 17.04.2018
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2018 © «EndlessCode Group»
 */
public class CooldownsTimer extends BukkitRunnable {

    public static final int TICK_PERIOD = 5;
    private static final int TICK_RATE = 20 / CooldownsTimer.TICK_PERIOD;
    //temporaryMap for avoid CME.
    private final HashMap<UUID, ValuePair> petItemsByPlayer = new HashMap<>();
    private final HashMap<UUID, ValuePair> temporaryMap = new HashMap<>();
    private final RPGInventory plugin;
    private final Slot petSlot;

    @SuppressWarnings("WeakerAccess")
    public CooldownsTimer(RPGInventory pluginInstance) {
        this.plugin = pluginInstance;
        this.petSlot = Objects.requireNonNull(SlotManager.instance().getPetSlot(), "Pet slot can't be null!");
    }

    public void addPetCooldown(Player player, ItemStack itemStack) {
        if (player == null || ItemUtils.isEmpty(itemStack)) {
            //throw new IllegalArgumentException?
            return;
        }

        if (PetManager.getPetFromItem(itemStack) == null) {
            //throw new IllegalArgumentException?
            return;
        }

        this.temporaryMap.put(player.getUniqueId(), new ValuePair(itemStack, new AtomicInteger(0)));
    }

    @Override
    public void run() {
        //Because there is no safe way to iterate map.
        if (!this.temporaryMap.isEmpty()) {
            this.petItemsByPlayer.putAll(this.temporaryMap);
            this.temporaryMap.clear();
        }

        final Iterator<Map.Entry<UUID, ValuePair>> iterator = this.petItemsByPlayer.entrySet().iterator();
        while (iterator.hasNext()) {
            final Map.Entry<UUID, ValuePair> next = iterator.next();
            int ticks = next.getValue().getTimer().incrementAndGet();

            if (ticks % CooldownsTimer.TICK_RATE != 0) {
                continue;
            }

            final Player player = this.plugin.getServer().getPlayer(next.getKey());
            if (!InventoryManager.playerIsLoaded(player)) {
                iterator.remove();
                continue;
            }

            final Inventory inventory = InventoryManager.get(player).getInventory();
            if (inventory == null || inventory.getItem(PetManager.getPetSlotId()) == null) {
                iterator.remove();
                continue;
            }

            final ItemStack originalItemStack = next.getValue().getItemStack();
            int cooldown = PetManager.getCooldown(originalItemStack);
            if (1 > cooldown) {
                PetManager.saveDeathTime(originalItemStack, 0);
                PetManager.spawnPet(player, originalItemStack);
                inventory.setItem(PetManager.getPetSlotId(), originalItemStack);
                iterator.remove();
            } else if (60 >= cooldown) {
                final ItemStack item = originalItemStack.clone();
                final String displayName = next.getValue().getDisplayName();
                final ItemMeta itemMeta = item.getItemMeta();
                itemMeta.setDisplayName(
                        displayName + RPGInventory.getLanguage().getMessage("pet.cooldown", cooldown)
                );
                item.setItemMeta(itemMeta);
                PetManager.addGlow(item);

                String itemTag = ItemUtils.getTag(item, ItemUtils.PET_TAG);
                if (!itemTag.isEmpty()) {
                    ItemUtils.setTag(item, ItemUtils.PET_TAG, itemTag);
                    inventory.setItem(PetManager.getPetSlotId(), item);
                } else {
                    inventory.setItem(PetManager.getPetSlotId(), this.petSlot.getCup());
                    iterator.remove();
                }
            }
        }
    }

    private class ValuePair {
        private final ItemStack itemStack;
        private final AtomicInteger timer;
        private final String displayName;

        private ValuePair(ItemStack itemStack, AtomicInteger timer) {
            this.itemStack = itemStack;
            //I have no idea why displayName with countdown applies to the original ItemStack after restoring inventory.
            this.displayName = itemStack.getItemMeta().getDisplayName();
            this.timer = timer;
        }

        private ItemStack getItemStack() {
            return itemStack;
        }

        private String getDisplayName() {
            return displayName;
        }

        private AtomicInteger getTimer() {
            return timer;
        }
    }
}
