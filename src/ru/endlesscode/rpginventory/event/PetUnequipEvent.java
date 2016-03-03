package ru.endlesscode.rpginventory.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Created by OsipXD on 26.08.2015
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2015 © «EndlessCode Group»
 */
public class PetUnequipEvent extends PlayerEvent {
    private static final HandlerList handlers = new HandlerList();

    public PetUnequipEvent(Player player) {
        super(player);
    }

    @NotNull
    @SuppressWarnings("unused")
    public static HandlerList getHandlerList() {
        return handlers;
    }

    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }
}