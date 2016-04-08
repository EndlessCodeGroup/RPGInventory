package ru.endlesscode.rpginventory.inventory;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import ru.endlesscode.rpginventory.RPGInventory;
import ru.endlesscode.rpginventory.inventory.backpack.Backpack;
import ru.endlesscode.rpginventory.inventory.slot.Slot;
import ru.endlesscode.rpginventory.inventory.slot.SlotManager;
import ru.endlesscode.rpginventory.item.ItemManager;
import ru.endlesscode.rpginventory.item.ItemStat;
import ru.endlesscode.rpginventory.item.Modifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by OsipXD on 09.11.2015
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2015 © «EndlessCode Group»
 */
public class PlayerWrapper implements InventoryHolder {
    private final OfflinePlayer player;
    private final Inventory inventory;
    private final Map<String, Integer> buyedSlots = new HashMap<>();
    private final List<String> permissions = new ArrayList<>();
    private final float baseSpeed;
    private final double baseHealth;

    private InventoryView inventoryView;
    private long timeWhenPreparedToBuy = 0;
    private Backpack backpack;

    private LivingEntity pet;
    private Modifier currentHealthModifier = new Modifier(0, 1);

    private ItemStack savedChestplate = null;
    private boolean falling = false;
    private boolean flying = false;
    private int fallTime = 0;

    public PlayerWrapper(OfflinePlayer player) {
        this.player = player;
        this.inventory = Bukkit.createInventory(this, 54, InventoryManager.TITLE);

        this.baseSpeed = player.getPlayer().getWalkSpeed();
        this.baseHealth = player.getPlayer().getMaxHealth();
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public InventoryView getInventoryView() {
        return inventoryView;
    }

    public void openInventory() {
        this.openInventory(false);
    }

    public void openInventory(boolean softOpen) {
        Player player = this.player.getPlayer();

        if (!softOpen) {
            player.closeInventory();
        }

        this.inventoryView = player.getOpenInventory();
        player.openInventory(this.inventory);
    }

    void prepareToBuy() {
        this.timeWhenPreparedToBuy = System.currentTimeMillis();
    }

    public boolean isOpened() {
        return this.inventoryView != null;
    }

    public void onClose() {
        this.inventoryView = null;
    }

    public int getBuyedGenericSlots() {
        return this.buyedSlots.get("{generic}");
    }

    public void setBuyedSlots(int buyedSlots) {
        this.setBuyedSlots("{generic}", buyedSlots);
    }

    void setBuyedSlots(String slotType) {
        this.setBuyedSlots(slotType, 1);
    }

    private void setBuyedSlots(String slotType, int buyedSlots) {
        this.buyedSlots.put(slotType, buyedSlots);
    }

    public boolean isBuyedSlot(String slotType) {
        return this.buyedSlots.containsKey(slotType);
    }

    boolean isPreparedToBuy() {
        if (this.timeWhenPreparedToBuy == 0 || System.currentTimeMillis() - this.timeWhenPreparedToBuy > 10 * 1000) {
            return false;
        } else {
            this.timeWhenPreparedToBuy = 0;
            return true;
        }
    }

    public void addPermissions(List<String> permissions) {
        this.permissions.addAll(permissions);

        for (String permission : permissions) {
            RPGInventory.getPermissions().playerAdd(this.player.getPlayer(), permission);
        }
    }

    public void clearPermissions() {
        for (String permission : this.permissions) {
            RPGInventory.getPermissions().playerRemove(this.player.getPlayer(), permission);
        }

        this.permissions.clear();
    }

    public float getBaseSpeed() {
        return baseSpeed;
    }

    void clearStats() {
        Player player = this.player.getPlayer();
        player.setWalkSpeed(this.baseSpeed);

        if (RPGInventory.isMythicMobsEnabled()) {
            this.player.getPlayer().setMaxHealth(this.baseHealth);
        } else {
            player.setMaxHealth(player.getMaxHealth() / this.currentHealthModifier.getMultiplier() - this.currentHealthModifier.getBonus());
        }
    }

    public Backpack getBackpack() {
        return this.backpack;
    }

    public void setBackpack(Backpack backpack) {
        this.backpack = backpack;
    }

    public LivingEntity getPet() {
        return pet;
    }

    public void setPet(LivingEntity pet) {
        this.pet = pet;
    }

    public boolean hasPet() {
        return pet != null;
    }

    public OfflinePlayer getPlayer() {
        return player;
    }

    public void updateHealth() {
        final Player player = this.player.getPlayer();
        Modifier healthModifier = ItemManager.getModifier(player, ItemStat.StatType.HEALTH, false);
        final double oldMaxHealth = player.getMaxHealth();

        // TODO: Добавить нормальную интеграцию c MythicMobs
        final double mythicMobsBonus;
        if (RPGInventory.isMythicMobsEnabled()) {
            mythicMobsBonus = oldMaxHealth - (this.baseHealth + this.currentHealthModifier.getBonus())
                    * this.currentHealthModifier.getMultiplier();
        } else {
            mythicMobsBonus = 0;
        }

        double newMaxHealth;
        if (RPGInventory.isMythicMobsEnabled()) {
            newMaxHealth = (this.baseHealth + healthModifier.getBonus()) * healthModifier.getMultiplier();
        } else {
            newMaxHealth =
                    (oldMaxHealth / this.currentHealthModifier.getMultiplier() - this.currentHealthModifier.getBonus()
                            + healthModifier.getBonus()) * healthModifier.getMultiplier();
        }

        if (oldMaxHealth == newMaxHealth) {
            return;
        }

        this.currentHealthModifier = healthModifier;

        final double oldHealth = player.getHealth();
        player.setMaxHealth(newMaxHealth);

        double currentHealth;
        if (newMaxHealth > oldMaxHealth) {
            currentHealth = oldHealth + newMaxHealth - oldMaxHealth;
        } else if (newMaxHealth < oldMaxHealth) {
            currentHealth = oldHealth - oldMaxHealth + newMaxHealth;
            if (currentHealth < 1) {
                currentHealth = 1;
            }
        } else {
            currentHealth = oldHealth;
        }

        final double finalCurrentHealth = currentHealth;
        new BukkitRunnable() {
            @Override
            public void run() {
                double health = finalCurrentHealth + mythicMobsBonus;
                player.setHealth(health > player.getMaxHealth() ? player.getMaxHealth() : health);
            }
        }.runTaskLater(RPGInventory.getInstance(), 1);
    }

    private void startFlight() {
        Slot elytraSlot = SlotManager.getSlotManager().getElytraSlot();
        ItemStack itemStack = this.inventory.getItem(elytraSlot.getSlotId());
        if (!elytraSlot.isCup(itemStack)) {
            Player player = this.player.getPlayer();
            this.savedChestplate = player.getEquipment().getChestplate();
            player.getEquipment().setChestplate(this.inventory.getItem(elytraSlot.getSlotId()));

            this.flying = true;
        }
    }

    private void stopFlight() {
        Player player = this.player.getPlayer();
        Slot elytraSlot = SlotManager.getSlotManager().getElytraSlot();
        this.inventory.setItem(elytraSlot.getSlotId(), player.getEquipment().getChestplate());
        player.getEquipment().setChestplate(this.savedChestplate);
        this.flying = false;
        this.savedChestplate = null;
    }

    public boolean isFalling() {
        return falling;
    }

    public void setFalling(boolean falling) {
        if (!falling && flying) {
            stopFlight();
        }

        fallTime = 0;
        this.falling = falling;
    }

    public void onFall() {
        if (this.fallTime++ == 4) {
            this.startFlight();
        }
    }
}
