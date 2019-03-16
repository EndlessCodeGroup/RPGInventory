/*
 * This file is part of RPGInventory.
 * Copyright (C) 2015-2017 Osip Fatkullin
 *
 * RPGInventory is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * RPGInventory is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with RPGInventory.  If not, see <http://www.gnu.org/licenses/>.
 */

package ru.endlesscode.rpginventory.inventory;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.endlesscode.inspector.bukkit.scheduler.TrackedBukkitRunnable;
import ru.endlesscode.rpginventory.RPGInventory;
import ru.endlesscode.rpginventory.event.updater.StatsUpdater;
import ru.endlesscode.rpginventory.inventory.backpack.Backpack;
import ru.endlesscode.rpginventory.inventory.slot.Slot;
import ru.endlesscode.rpginventory.inventory.slot.SlotManager;
import ru.endlesscode.rpginventory.item.CustomItem;
import ru.endlesscode.rpginventory.item.ItemManager;
import ru.endlesscode.rpginventory.pet.Attributes;
import ru.endlesscode.rpginventory.pet.PetManager;
import ru.endlesscode.rpginventory.pet.PetType;
import ru.endlesscode.rpginventory.utils.ItemUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by OsipXD on 09.11.2015
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
public class PlayerWrapper implements InventoryHolder {

    private final OfflinePlayer player;
    private final Inventory inventory;
    private final Map<String, Integer> buyedSlots = new HashMap<>();
    private final List<String> permissions = new ArrayList<>();

    @Nullable
    private InventoryView inventoryView;
    @Nullable
    private Slot slotPreparedToBuy = null;
    private long timeWhenPreparedToBuy = 0;
    @Nullable
    private Backpack backpack = null;
    private LivingEntity pet;

    @Nullable
    private ItemStack savedChestplate = null;
    private boolean falling = false;
    private boolean flying = false;
    private int fallTime = 0;

    private String lastMessage = "";
    private long lastMessageTime = 0;
    private boolean pocketCraft = false;

    public PlayerWrapper(OfflinePlayer player) {
        this.player = player;
        this.inventory = Bukkit.createInventory(this, 54, InventoryManager.TITLE);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    @Nullable
    public InventoryView getInventoryView() {
        return inventoryView;
    }

    public void openInventoryDeferred(boolean softOpen) {
        new TrackedBukkitRunnable() {
            @Override
            public void run() {
                openInventory(softOpen);
            }
        }.runTaskLater(RPGInventory.getInstance(), 0);
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

    void prepareToBuy(Slot slot) {
        this.slotPreparedToBuy = slot;
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

    boolean isPreparedToBuy(Slot slot) {
        if (this.timeWhenPreparedToBuy == 0 || this.slotPreparedToBuy != slot
                || System.currentTimeMillis() - this.timeWhenPreparedToBuy > 10 * 1000) {
            return false;
        }

        this.timeWhenPreparedToBuy = 0;
        this.slotPreparedToBuy = null;
        return true;
    }

    public void addPermissions(@NotNull List<String> permissions) {
        this.permissions.addAll(permissions);

        for (String permission : permissions) {
            RPGInventory.getPermissions().playerAdd(this.player.getPlayer(), permission);
        }
    }

    private void clearPermissions() {
        for (String permission : this.permissions) {
            RPGInventory.getPermissions().playerRemove(this.player.getPlayer(), permission);
        }

        this.permissions.clear();
    }

    private void clearStats() {
        Player player = this.player.getPlayer();

        AttributeInstance speedAttribute = player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        AttributeModifier rpgInvModifier = null;
        for (AttributeModifier modifier : speedAttribute.getModifiers()) {
            if (modifier.getUniqueId().compareTo(Attributes.SPEED_MODIFIER_ID) == 0) {
                rpgInvModifier = modifier;
            }
        }

        if (rpgInvModifier != null) {
            speedAttribute.removeModifier(rpgInvModifier);
        }
    }

    @Nullable
    public Backpack getBackpack() {
        return this.backpack;
    }

    public void setBackpack(@Nullable Backpack backpack) {
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

    public void onFall() {
        if (isFalling()) {
            if (this.fallTime == 0) {
                this.startFlight();
            }
            this.fallTime++;
        } else {
            this.setFalling(true);
        }
    }

    private void startFlight() {
        Slot elytraSlot = SlotManager.instance().getElytraSlot();
        if (elytraSlot == null) {
            return;
        }
        ItemStack itemStack = this.inventory.getItem(elytraSlot.getSlotId());
        if (!elytraSlot.isCup(itemStack)) {
            Player player = this.player.getPlayer();
            ItemStack chestplate = player.getEquipment().getChestplate();
            this.savedChestplate = ItemUtils.isEmpty(chestplate) ? new ItemStack(Material.AIR) : chestplate;
            player.getEquipment().setChestplate(this.inventory.getItem(elytraSlot.getSlotId()));

            this.flying = true;
        }
    }

    public void onStartGliding() {
        if (this.hasPet()) {
            PetManager.despawnPet(player);
        }
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

    private void stopFlight() {
        Slot elytraSlot = SlotManager.instance().getElytraSlot();
        if (savedChestplate != null && elytraSlot != null) {
            Player player = this.player.getPlayer();
            this.inventory.setItem(elytraSlot.getSlotId(), player.getEquipment().getChestplate());
            player.getEquipment().setChestplate(this.savedChestplate);
            this.savedChestplate = null;
        }

        this.flying = false;

        if (PetManager.isEnabled() && !this.hasPet()) {
            PetManager.respawnPet(player.getPlayer());
        }
    }

    public boolean isFlying() {
        return flying;
    }

    @Nullable ItemStack getSavedChestplate() {
        return savedChestplate;
    }

    void onUnload() {
        // Disabling of flight mode
        if (this.flying) {
            this.setFalling(false);
        }

        this.clearStats();

        // Removing pet
        if (PetManager.isEnabled()) {
            PetManager.despawnPet(player);
            Inventory inventory = this.inventory;
            ItemStack petItem = inventory.getItem(PetManager.getPetSlotId());
            if (petItem != null) {
                inventory.setItem(PetManager.getPetSlotId(), PetType.clone(petItem));
            }
        }
    }

    public void updateStatsLater() {
        new StatsUpdater(player.getPlayer()).runTaskLater(RPGInventory.getInstance(), 1);
    }

    public void updatePermissions() {
        Player player = this.player.getPlayer();
        this.clearPermissions();
        List<CustomItem> customItems = new ArrayList<>();
        for (ItemStack item : this.getInventory().getContents()) {
            if (CustomItem.isCustomItem(item)) {
                customItems.add(ItemManager.getCustomItem(item));
            }
        }

        ItemStack itemInOffHand = player.getEquipment().getItemInOffHand();
        ItemStack itemInMainHand = player.getEquipment().getItemInMainHand();

        if (CustomItem.isCustomItem(itemInOffHand)) {
            customItems.add(ItemManager.getCustomItem(itemInOffHand));
        }

        if (CustomItem.isCustomItem(itemInMainHand)) {
            customItems.add(ItemManager.getCustomItem(itemInMainHand));
        }

        for (CustomItem customItem : customItems) {
            customItem.onEquip(player);
        }
    }

    public String getLastMessage() {
        if (System.currentTimeMillis() - this.lastMessageTime > 5 * 1000) {
            lastMessage = "";
        }

        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
        this.lastMessageTime = System.currentTimeMillis();
    }

    public void openWorkbench() {
        player.getPlayer().openWorkbench(null, true);
        this.pocketCraft = true;
    }

    public void onWorkbenchClosed() {
        this.pocketCraft = false;
    }

    public boolean isPocketCraft() {
        return this.pocketCraft;
    }
}
