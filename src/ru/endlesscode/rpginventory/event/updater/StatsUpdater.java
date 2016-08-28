package ru.endlesscode.rpginventory.event.updater;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import ru.endlesscode.rpginventory.inventory.InventoryManager;
import ru.endlesscode.rpginventory.inventory.PlayerWrapper;
import ru.endlesscode.rpginventory.item.ItemManager;
import ru.endlesscode.rpginventory.item.ItemStat;
import ru.endlesscode.rpginventory.pet.Attributes;

/**
 * Created by OsipXD on 21.09.2015
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
public class StatsUpdater extends BukkitRunnable {
    private final Player player;

    public StatsUpdater(Player player) {
        this.player = player;
    }

    @Override
    public void run() {
        if (!InventoryManager.playerIsLoaded(this.player)) {
            return;
        }

        PlayerWrapper playerWrapper = InventoryManager.get(this.player);
        playerWrapper.updatePermissions();

        // Update speed
        AttributeInstance speedAttribute = this.player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        AttributeModifier rpgInvModifier = null;
        for (AttributeModifier modifier : speedAttribute.getModifiers()) {
            if (modifier.getUniqueId().compareTo(Attributes.SPEED_MODIFIER_ID) == 0) {
                rpgInvModifier = modifier;
            }
        }

        if (rpgInvModifier != null) {
            speedAttribute.removeModifier(rpgInvModifier);
        }

        rpgInvModifier = new AttributeModifier(
                Attributes.SPEED_MODIFIER_ID, Attributes.SPEED_MODIFIER,
                ItemManager.getModifier(this.player, ItemStat.StatType.SPEED).getMultiplier() - 1,
                AttributeModifier.Operation.MULTIPLY_SCALAR_1
        );

        speedAttribute.addModifier(rpgInvModifier);

        // Update info slots
        if (playerWrapper.isOpened()) {
            InventoryManager.syncInfoSlots(player, playerWrapper.getInventory());
        }
    }
}