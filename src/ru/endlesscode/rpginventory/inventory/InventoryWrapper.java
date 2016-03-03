package ru.endlesscode.rpginventory.inventory;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import ru.endlesscode.rpginventory.RPGInventory;
import ru.endlesscode.rpginventory.inventory.backpack.Backpack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by OsipXD on 09.11.2015
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2015 © «EndlessCode Group»
 */
public class InventoryWrapper implements InventoryHolder {
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

    public InventoryWrapper(OfflinePlayer player) {
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

    public void prepareToBuy() {
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

    public void setBuyedSlots(String slotType) {
        this.setBuyedSlots(slotType, 1);
    }

    private void setBuyedSlots(String slotType, int buyedSlots) {
        this.buyedSlots.put(slotType, buyedSlots);
    }

    public boolean isBuyedSlot(String slotType) {
        return this.buyedSlots.containsKey(slotType);
    }

    public boolean isPreparedToBuy() {
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

    public double getBaseHealth() {
        return baseHealth;
    }

    public void clearStats() {
        this.player.getPlayer().setWalkSpeed(this.baseSpeed);
        this.player.getPlayer().setMaxHealth(this.baseHealth);
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
}
