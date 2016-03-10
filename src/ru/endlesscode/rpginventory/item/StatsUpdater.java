package ru.endlesscode.rpginventory.item;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import ru.endlesscode.rpginventory.inventory.InventoryManager;
import ru.endlesscode.rpginventory.inventory.InventoryWrapper;

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