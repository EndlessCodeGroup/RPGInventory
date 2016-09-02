package ru.endlesscode.rpginventory.event.updater;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import ru.endlesscode.rpginventory.inventory.InventoryManager;
import ru.endlesscode.rpginventory.item.ItemManager;
import ru.endlesscode.rpginventory.item.ItemStat;
import ru.endlesscode.rpginventory.item.Modifier;
import ru.endlesscode.rpginventory.misc.Config;

/**
 * Created by OsipXD on 14.05.2016
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
public class HealthUpdater extends BukkitRunnable {
    private final static double BASE_HEALTH = Config.getConfig().getDouble("health.base", 20.0D);

    private final Player player;
    private State state;

    private boolean initHealth = true;

    private Modifier currentModifier = Modifier.EMPTY;
    private double otherPluginsBonus = 0;
    private double attributesBonus = 0;
    private boolean accepted = true;

    // Temporary variables
    private double tOtherPluginsBonus = 0;
    private double tAttributesBonus = 0;
    private double newBonus = 0;
    private double newMaxHealth = 0;
    private double newHealth = 0;

    public HealthUpdater(Player player) {
        this.player = player;
        this.state = State.WAITING;
    }

    public double getModifiedHealth() {
        return (BASE_HEALTH + this.currentModifier.getBonus()) * this.currentModifier.getMultiplier();
    }

    @Override
    public void run() {
        if (!player.isOnline()) {
            cancel();
            return;
        }

        if (this.initHealth && this.newHealth == 0) {
            this.initHealth = false;
        }

        this.waiting();
        switch (this.state) {
            case CHECKING:
                this.checking();
                break;
            case APPLYING:
                this.applying();
                break;
            case ACCEPTING:
                this.accepting();
                break;
        }
    }

    private void setMaxHealth(double newMaxHealth) {
        // Validate max health
        if (newMaxHealth <= 0) {
            return;
        }

        double currentMaxHealth = this.player.getMaxHealth();
        double currentHealth = this.player.getHealth();

        if (this.initHealth) {
            this.initHealth = false;
        } else {
            // Compensating of health changes
            double newHealth;
            if (newMaxHealth > currentMaxHealth) {
                newHealth = currentHealth + newMaxHealth - currentMaxHealth;
            } else if (newMaxHealth < currentMaxHealth) {
                newHealth = currentHealth - currentMaxHealth + newMaxHealth;
                if (newHealth < 1) {
                    newHealth = 1;
                }
            } else {
                newHealth = currentHealth;
            }

            this.newHealth = newHealth;
        }
        this.newMaxHealth = newMaxHealth;

        this.player.setMaxHealth(this.getModifiedHealth());
        this.accepted = false;
        this.state = State.CHECKING;
    }

    private void waiting() {
        double currentMaxHealth = player.getMaxHealth();
        double currentBonus =
                this.initHealth
                        ? this.attributesBonus + this.otherPluginsBonus
                        : currentMaxHealth - this.getModifiedHealth();
        Modifier newModifier = ItemManager.getModifier(player, ItemStat.StatType.HEALTH);

        // Check if we need update health
        if (!this.currentModifier.equals(newModifier) || this.initHealth
                || currentBonus != this.otherPluginsBonus + this.attributesBonus) {
            if (!this.accepted) {
                return;
            }

            this.newBonus = currentBonus;
            this.currentModifier = newModifier;
            this.setMaxHealth(this.getModifiedHealth() + this.newBonus);
        }
    }

    private void checking() {
        this.tAttributesBonus = this.player.getMaxHealth() - this.getModifiedHealth();
        this.tOtherPluginsBonus = this.newBonus - this.tAttributesBonus;


        // Next state
        this.state = State.APPLYING;
    }

    private void applying() {
        this.player.setMaxHealth(this.getModifiedHealth() + this.tOtherPluginsBonus);


        this.state = State.ACCEPTING;
    }

    private void accepting() {
        player.setHealth(newHealth > player.getMaxHealth() ? player.getMaxHealth() : newHealth);

        this.otherPluginsBonus = this.tOtherPluginsBonus;
        this.attributesBonus = this.tAttributesBonus - (newMaxHealth - player.getMaxHealth());

        this.scaleHealth();

        // Reset variables
        this.tOtherPluginsBonus = 0;
        this.tAttributesBonus = 0;
        this.newBonus = 0;
        this.newMaxHealth = 0;
        this.newHealth = 0;

        this.accepted = true;
        this.state = State.WAITING;

        // Update info slots
        InventoryManager.syncInfoSlots(InventoryManager.get(player));
    }

    private void scaleHealth() {
        if (!Config.getConfig().getBoolean("health.scale", false)) {
            return;
        }

        int hearts = Config.getConfig().getInt("health.hearts", 20);
        double minInHeart = Config.getConfig().getDouble("health.heart-value.min", 1);
        double maxInHeart = Config.getConfig().getDouble("health.heart-value.max", 5);

        double healthScale;
        if (this.player.getMaxHealth() / minInHeart < hearts) {
            healthScale = this.player.getMaxHealth() / minInHeart;
        } else if (this.player.getMaxHealth() / maxInHeart > hearts) {
            healthScale = this.player.getMaxHealth() / maxInHeart;
        } else {
            healthScale = hearts;
        }

        if (healthScale != this.player.getHealthScale() || !this.player.isHealthScaled()) {
            this.player.setHealthScale(healthScale);
        }
    }

    public double getHealth() {
        return newHealth;
    }

    public void setHealth(double newHealth) {
        this.newHealth = newHealth;
    }

    public double getAttributesBonus() {
        return attributesBonus;
    }

    public void setAttributesBonus(double attributesBonus) {
        this.attributesBonus = attributesBonus;
    }

    public double getOtherPluginsBonus() {
        return otherPluginsBonus;
    }

    public void setOtherPluginsBonus(double otherPluginsBonus) {
        this.otherPluginsBonus = otherPluginsBonus;
    }

    public void stop() {
        player.setMaxHealth(BASE_HEALTH - this.attributesBonus);
        this.cancel();
    }

    private enum State {
        WAITING, APPLYING, CHECKING, ACCEPTING
    }
}
