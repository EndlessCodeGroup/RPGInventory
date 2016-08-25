package ru.endlesscode.rpginventory.inventory.chest;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import ru.endlesscode.rpginventory.RPGInventory;

/**
 * Created by OsipXD on 02.09.2015
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
public class ChestWrapper {
    private static final int PAGE_SIZE = 6;
    private static final int SIZE = 9;

    @NotNull
    private final Inventory inventory;
    private final int pageNum;
    private final HumanEntity player;
    private final InventoryView view;
    private int page;
    private boolean keepOpen;

    public ChestWrapper(@NotNull Inventory inventory, InventoryView view, HumanEntity player) {
        this.inventory = inventory;
        this.view = view;
        this.inventory.getViewers().add(player);
        this.player = player;

        int pageNum = (int) Math.ceil((float) inventory.getSize() / (float) PAGE_SIZE);
        int cupCount = pageNum * PAGE_SIZE % SIZE;
        for (ItemStack item : inventory.getContents()) {
            if (ChestManager.isCapSlot(item)) {
                cupCount++;
            }
        }
        this.pageNum = pageNum - cupCount / PAGE_SIZE;

        this.page = 0;
        this.keepOpen = false;
    }

    @NotNull
    public Inventory getInventory() {
        return this.inventory;
    }

    @NotNull
    private ItemStack[] getContents() {
        ItemStack[] contents = new ItemStack[SIZE];

        for (int i = 0; i < SIZE; i++) {
            if (i > 5) {
                contents[i] = new ItemStack(Material.AIR);
            } else {
                int index = PAGE_SIZE * this.page + i;
                contents[i] = index < this.inventory.getSize() ? this.inventory.getItem(index) : ChestManager.getCapSlot();
            }
        }

        return contents;
    }

    public void setContents(ItemStack[] contents) {
        for (int i = 0; i < 6; i++) {
            int index = PAGE_SIZE * this.page + i;

            if (index < this.inventory.getSize()) {
                this.inventory.setItem(index, contents[i]);
            }
        }
    }

    public void onCloseInventory() {
        this.inventory.getViewers().remove(this.player);
    }

    public Inventory getNextPage() {
        this.page = (this.page + 1) % this.pageNum;
        return createChestInventory();
    }

    public Inventory getPrevPage() {
        this.page = (this.page - 1 + this.pageNum) % this.pageNum;
        return createChestInventory();
    }

    public Inventory createChestInventory() {
        Inventory chest = Bukkit.createInventory(this.inventory.getHolder(), InventoryType.DISPENSER, this.getTitle());
        chest.setContents(this.getContents());
        return chest;
    }

    private String getTitle() {
        String title = this.inventory.getTitle().startsWith("container.") ? RPGInventory.getLanguage().getCaption(this.inventory.getTitle()) : this.inventory.getTitle();
        if (title.length() < 28) {
            title += " " + (this.page + 1) + "/" + this.pageNum;
        }

        return title;
    }

    public boolean isKeepOpen() {
        return this.keepOpen;
    }

    public void keepOpen(boolean keepOpen) {
        this.keepOpen = keepOpen;
    }

    public int convertSlot(int slotId) {
        return this.page * 6 + slotId;
    }

    public InventoryView getView() {
        return view;
    }
}
