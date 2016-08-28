package ru.endlesscode.rpginventory.inventory.backpack;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;
import ru.endlesscode.rpginventory.RPGInventory;

import java.util.Arrays;

/**
 * Created by OsipXD on 26.08.2016
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
public class BackpackUpdater extends BukkitRunnable {
    private final Player player;
    private final Inventory inventory;
    private final Backpack backpack;

    private BackpackUpdater(Player player, Inventory inventory, Backpack backpack) {
        this.player = player;
        this.inventory = inventory;
        this.backpack = backpack;
    }

    public static void update(Player player, Inventory inventory, Backpack backpack) {
        new BackpackUpdater(player, inventory, backpack).runTaskLater(RPGInventory.getInstance(), 2);
    }

    @Override
    public void run() {
        backpack.onUse();
        backpack.setContents(Arrays.copyOfRange(inventory.getContents(), 0, backpack.getType().getSize()));
    }
}
