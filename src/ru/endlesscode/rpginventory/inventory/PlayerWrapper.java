package ru.endlesscode.rpginventory.inventory;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import ru.endlesscode.rpginventory.RPGInventory;
import ru.endlesscode.rpginventory.event.updater.HealthUpdater;
import ru.endlesscode.rpginventory.event.updater.StatsUpdater;
import ru.endlesscode.rpginventory.inventory.backpack.Backpack;
import ru.endlesscode.rpginventory.inventory.slot.Slot;
import ru.endlesscode.rpginventory.inventory.slot.SlotManager;
import ru.endlesscode.rpginventory.item.CustomItem;
import ru.endlesscode.rpginventory.item.ItemManager;
import ru.endlesscode.rpginventory.nms.VersionHandler;
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
 * All rights reserved 2014 - 2015 © «EndlessCode Group»
 */
public class PlayerWrapper implements InventoryHolder {
    private final static float BASE_SPEED = 0.2f;

    private final OfflinePlayer player;
    private final Inventory inventory;
    private final Map<String, Integer> buyedSlots = new HashMap<>();
    private final List<String> permissions = new ArrayList<>();
    private final HealthUpdater healthUpdater;

    private InventoryView inventoryView;
    private long timeWhenPreparedToBuy = 0;
    private Backpack backpack;
    private LivingEntity pet;

    private ItemStack savedChestplate = null;
    private boolean falling = false;
    private boolean flying = false;
    private int fallTime = 0;

    public PlayerWrapper(OfflinePlayer player) {
        this.player = player;
        this.inventory = Bukkit.createInventory(this, 54, InventoryManager.TITLE);

        this.healthUpdater = new HealthUpdater(player.getPlayer());
    }

    void startHealthUpdater() {
        this.healthUpdater.runTaskTimer(RPGInventory.getInstance(), 1, 1);
    }

    public HealthUpdater getHealthUpdater() {
        return healthUpdater;
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

    private void clearPermissions() {
        for (String permission : this.permissions) {
            RPGInventory.getPermissions().playerRemove(this.player.getPlayer(), permission);
        }

        this.permissions.clear();
    }

    public float getBaseSpeed() {
        return BASE_SPEED;
    }

    private void clearStats() {
        Player player = this.player.getPlayer();
        player.setWalkSpeed(BASE_SPEED);
        this.healthUpdater.setHealth(player.getHealth());
        this.healthUpdater.stop();
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

    private void startFlight() {
        Slot elytraSlot = SlotManager.getSlotManager().getElytraSlot();
        ItemStack itemStack = this.inventory.getItem(elytraSlot.getSlotId());
        if (!elytraSlot.isCup(itemStack)) {
            Player player = this.player.getPlayer();
            ItemStack chestplate = player.getEquipment().getChestplate();
            this.savedChestplate = ItemUtils.isEmpty(chestplate) ? new ItemStack(Material.AIR) : chestplate;
            player.getEquipment().setChestplate(this.inventory.getItem(elytraSlot.getSlotId()));

            this.flying = true;
        }
    }

    private void stopFlight() {
        if (savedChestplate != null) {
            Player player = this.player.getPlayer();
            Slot elytraSlot = SlotManager.getSlotManager().getElytraSlot();
            this.inventory.setItem(elytraSlot.getSlotId(), player.getEquipment().getChestplate());
            player.getEquipment().setChestplate(this.savedChestplate);
            this.savedChestplate = null;
        }
        this.flying = false;
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

    public boolean isFlying() {
        return flying;
    }

    ItemStack getSavedChestplate() {
        return savedChestplate;
    }

    void onUnload() {
        // Disabling of flight mode
        if (this.flying) {
            this.setFalling(false);
        }

        this.clearStats();

        // Restoring of scale settings
        player.getPlayer().setHealthScaled(false);

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

    public boolean resetMaxHealth() {
        if (this.healthUpdater.getAttributesBonus() == 0 && this.healthUpdater.getOtherPluginsBonus() == 0) {
            return false;
        }

        this.healthUpdater.setAttributesBonus(0);
        this.healthUpdater.setOtherPluginsBonus(0);
        this.player.getPlayer().kickPlayer(RPGInventory.getLanguage().getCaption("message.fixhp"));
        return true;
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

        if (VersionHandler.is1_9()) {
            ItemStack itemInOffHand = player.getEquipment().getItemInOffHand();
            ItemStack itemInMainHand = player.getEquipment().getItemInMainHand();

            if (CustomItem.isCustomItem(itemInOffHand)) {
                customItems.add(ItemManager.getCustomItem(itemInOffHand));
            }

            if (CustomItem.isCustomItem(itemInMainHand)) {
                customItems.add(ItemManager.getCustomItem(itemInMainHand));
            }
        } else {
            //noinspection deprecation
            ItemStack itemInHand = player.getItemInHand();
            if (CustomItem.isCustomItem(itemInHand)) {
                customItems.add(ItemManager.getCustomItem(itemInHand));
            }
        }

        for (CustomItem customItem : customItems) {
            customItem.onEquip(player);
        }
    }

}
