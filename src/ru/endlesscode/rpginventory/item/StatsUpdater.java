package ru.endlesscode.rpginventory.item;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import ru.endlesscode.rpginventory.inventory.InventoryManager;
import ru.endlesscode.rpginventory.inventory.InventoryWrapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by OsipXD on 21.09.2015
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2015 © «EndlessCode Group»
 */
class StatsUpdater extends BukkitRunnable {
    private final Player player;

    public StatsUpdater(Player player) {
        this.player = player;
    }

    @Override
    public void run() {
        if (!InventoryManager.playerIsLoaded(this.player)) {
            return;
        }

        InventoryWrapper inventoryWrapper = InventoryManager.get(this.player);

        // Update permissions
        inventoryWrapper.clearPermissions();
        List<ItemStack> items = new ArrayList<>(inventoryWrapper.getInventory().getContents().length + 1);
        Collections.addAll(items, inventoryWrapper.getInventory().getContents());
        items.add(this.player.getItemInHand());
        for (ItemStack item : items) {
            if (CustomItem.isCustomItem(item)) {
                CustomItem customItem = ItemManager.getCustomItem(item);
                customItem.onEquip(this.player);
            }
        }

        // Update health
        Modifier healthModifier = ItemManager.getModifier(this.player, ItemStat.StatType.HEALTH, false);
        double oldHealth = this.player.getMaxHealth();
        double newHealth = (inventoryWrapper.getBaseHealth() + healthModifier.getBonus()) * healthModifier.getMultiplier();
        double currentHealth = this.player.getHealth();
        this.player.setMaxHealth(newHealth);

        if (newHealth > oldHealth) {
            currentHealth = currentHealth + newHealth - oldHealth;
        } else if (newHealth < oldHealth) {
            currentHealth = currentHealth - oldHealth + newHealth;
            if (currentHealth < 1) {
                currentHealth = 1;
            }
        }
        this.player.setHealth(currentHealth);

        // Update speed
        this.player.setWalkSpeed(inventoryWrapper.getBaseSpeed() * ItemManager.getModifier(this.player, ItemStat.StatType.SPEED, false).getMultiplier());
    }
}