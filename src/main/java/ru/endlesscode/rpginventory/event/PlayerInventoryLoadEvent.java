package ru.endlesscode.rpginventory.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

/**
 * Created by OsipXD on 11.09.2015
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
public class PlayerInventoryLoadEvent extends PlayerEvent {
    private static final HandlerList handlers = new HandlerList();

    private PlayerInventoryLoadEvent(Player who) {
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

    public static class Pre extends PlayerInventoryLoadEvent implements Cancellable {
        private boolean cancelled = false;

        public Pre(Player who) {
            super(who);
        }

        @Override
        public boolean isCancelled() {
            return this.cancelled;
        }

        @Override
        public void setCancelled(boolean cancelled) {
            this.cancelled = cancelled;
        }
    }

    public static class Post extends PlayerInventoryLoadEvent {
        public Post(Player who) {
            super(who);
        }
    }
}