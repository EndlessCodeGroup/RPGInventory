package ru.endlesscode.rpginventory.inventory.backpack;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import ru.endlesscode.rpginventory.inventory.InventoryManager;
import ru.endlesscode.rpginventory.inventory.chest.ChestManager;
import ru.endlesscode.rpginventory.misc.Config;
import ru.endlesscode.rpginventory.utils.ItemUtils;

import java.util.UUID;

/**
 * Created by OsipXD on 19.10.2015
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2015 © «EndlessCode Group»
 */
public class Backpack {
    private final UUID id;
    private final BackpackType backpackType;
    private final ItemStack holder;

    private long lastUse;
    private ItemStack[] contents;

    public Backpack(BackpackType backpackType) {
        this(backpackType, UUID.randomUUID());
    }

    public Backpack(BackpackType backpackType, UUID uuid) {
        this.id = uuid;
        this.backpackType = backpackType;
        this.contents = new ItemStack[backpackType.getSize()];
        this.holder = ItemUtils.setTag(this.backpackType.getItem().clone(), ItemUtils.BACKPACK_UID_TAG, this.id.toString());
    }

    UUID getUniqueId() {
        return this.id;
    }

    public ItemStack getItem() {
        return this.holder;
    }

    public BackpackType getType() {
        return backpackType;
    }

    public void open(Player player) {
        int realSize = (int) Math.ceil(this.backpackType.getSize() / 9.0) * 9;
        BackpackHolder holder = new BackpackHolder();
        Inventory inventory = Bukkit.createInventory(holder, realSize, backpackType.getTitle());
        holder.setInventory(inventory);

        for (int i = 0; i < realSize; i++) {
            if (i < this.backpackType.getSize()) {
                if (this.contents[i] != null) {
                    inventory.setItem(i, this.contents[i]);
                }
            } else {
                inventory.setItem(i, ChestManager.getCapSlot());
            }
        }

        player.openInventory(inventory);
        InventoryManager.get(player).setBackpack(this);
    }

    ItemStack[] getContents() {
        return contents;
    }

    public void setContents(ItemStack[] contents) {
        this.contents = contents;
    }

    public void onClose() {
        this.lastUse = System.currentTimeMillis();
    }

    long getLastUse() {
        return this.lastUse;
    }

    void setLastUse(long lastUse) {
        this.lastUse = lastUse;
    }

    boolean isOverdue() {
        int lifeTime = Config.getConfig().getInt("backpacks.expiration-time", 0);
        return lifeTime != 0 && (System.currentTimeMillis() - this.lastUse) / (1_000 * 60 * 60 * 24) > lifeTime;
    }
}
