package ru.endlesscode.rpginventory.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

/**
 * Created by OsipXD on 11.09.2015
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2015 © «EndlessCode Group»
 */
public class PlayerInventoryUnloadEvent extends PlayerEvent {
    private static final HandlerList handlers = new HandlerList();

    private PlayerInventoryUnloadEvent(Player who) {
        super(who);
    }

    @SuppressWarnings("unused")
    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static class Post extends PlayerInventoryUnloadEvent {
        public Post(Player who) {
            super(who);
        }
    }
}
