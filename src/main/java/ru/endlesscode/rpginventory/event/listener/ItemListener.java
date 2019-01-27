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

package ru.endlesscode.rpginventory.event.listener;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import ru.endlesscode.inspector.bukkit.scheduler.TrackedBukkitRunnable;
import ru.endlesscode.rpginventory.RPGInventory;
import ru.endlesscode.rpginventory.compat.Sound;
import ru.endlesscode.rpginventory.inventory.InventoryManager;
import ru.endlesscode.rpginventory.inventory.slot.Slot;
import ru.endlesscode.rpginventory.inventory.slot.SlotManager;
import ru.endlesscode.rpginventory.item.CustomItem;
import ru.endlesscode.rpginventory.item.ItemManager;
import ru.endlesscode.rpginventory.item.ItemStat;
import ru.endlesscode.rpginventory.item.Modifier;
import ru.endlesscode.rpginventory.misc.config.Config;
import ru.endlesscode.rpginventory.utils.EffectUtils;
import ru.endlesscode.rpginventory.utils.ItemUtils;
import ru.endlesscode.rpginventory.utils.PlayerUtils;

import java.util.List;

/**
 * Created by OsipXD on 19.09.2015
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
public class ItemListener implements Listener {
    @EventHandler(priority = EventPriority.LOW)
    public void onDamage(@NotNull EntityDamageByEntityEvent event) {
        if (event.getEntity() == null) {
            return;
        }

        Player damager;

        // Defensive stats
        try {
            if (event.getEntityType() == EntityType.PLAYER) {
                Modifier armorModifier = ItemManager.getModifier((Player) event.getEntity(), ItemStat.StatType.ARMOR);
                double armorReduction = event.getDamage(EntityDamageEvent.DamageModifier.ARMOR);
                double armor = (armorReduction - armorModifier.getBonus()) * armorModifier.getMultiplier();
                if (armor > 0.0D) {
                    armor = 0.0D;
                }

                event.setDamage(EntityDamageEvent.DamageModifier.ARMOR, armor);
            }
        } catch (UnsupportedOperationException ignored) {
        }

        if (event.getDamager() == null) {
            return;
        }

        // Attack stats
        Modifier damageModifier;
        ItemStack itemInHand;
        if (event.getDamager().getType() == EntityType.PLAYER) {
            damager = (Player) event.getDamager();
            itemInHand = damager.getEquipment().getItemInMainHand();
            damageModifier = ItemManager.getModifier(damager,
                    ItemUtils.isEmpty(itemInHand) ? ItemStat.StatType.HAND_DAMAGE : ItemStat.StatType.DAMAGE);
        } else if (event.getDamager().getType() == EntityType.ARROW &&
                ((Arrow) event.getDamager()).getShooter() instanceof Player) {
            damager = (Player) ((Arrow) event.getDamager()).getShooter();
            itemInHand = damager.getEquipment().getItemInMainHand();
            damageModifier = ItemManager.getModifier(damager, ItemStat.StatType.BOW_DAMAGE);
        } else {
            return;
        }

        if (!InventoryManager.playerIsLoaded(damager)) {
            return;
        }

        if (!ItemManager.allowedForPlayer(damager, itemInHand, true)) {
            event.setCancelled(true);
            return;
        }

        // Checking battle system restrictions
        boolean forceWeapon = Config.getConfig().getBoolean("attack.force-weapon");
        boolean requireWeapon = Config.getConfig().getBoolean("attack.require-weapon");
        PlayerInventory inventory = damager.getInventory();
        if ((forceWeapon || requireWeapon)
                && SlotManager.instance().getSlot(inventory.getHeldItemSlot(), InventoryType.SlotType.QUICKBAR) == null) {
            List<Slot> activeSlots = SlotManager.instance().getActiveSlots();
            if (activeSlots.size() != 0) {
                if (forceWeapon) {
                    for (Slot activeSlot : activeSlots) {
                        if (!InventoryManager.isQuickEmptySlot(inventory.getItem(activeSlot.getQuickSlot()))) {
                            inventory.setHeldItemSlot(activeSlot.getQuickSlot());
                            break;
                        }
                    }
                }

                if (requireWeapon) {
                    event.setCancelled(true);
                    return;
                }
            }
        }

        double baseDamage = event.getDamage(EntityDamageEvent.DamageModifier.BASE);
        double damage = (CustomItem.isCustomItem(itemInHand) ? 1 : baseDamage)
                + damageModifier.getBonus() * damageModifier.getMultiplier();
        double critChance = ItemManager.getModifier(damager, ItemStat.StatType.CRIT_CHANCE).getMultiplier() - 1.0;
        if (Math.random() <= critChance) {
            damage *= ItemManager.getModifier(damager, ItemStat.StatType.CRIT_DAMAGE).getMultiplier();
            damager.getWorld().playSound(
                    event.getEntity().getLocation(),
                    Sound.SUCCESSFUL_HIT.bukkitSound(),
                    1,
                    (float) (0.5 + Math.random() * 0.4)
            );
            EffectUtils.playParticlesToAll(Particle.CRIT, 10, event.getEntity().getLocation());
        }

        event.setDamage(EntityDamageEvent.DamageModifier.BASE, damage);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJump(@NotNull PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (!InventoryManager.playerIsLoaded(player)) {
            return;
        }

        Vector velocity = player.getVelocity();
        Modifier jumpModifier = ItemManager.getModifier(player, ItemStat.StatType.JUMP);

        if (jumpModifier.getBonus() == 0 && jumpModifier.getMultiplier() == 1) {
            return;
        }

        // === START: Magic constants ===
        if (velocity.getY() == 0.41999998688697815D) {
            double jump = (1.5 + Math.sqrt(jumpModifier.getBonus())) * jumpModifier.getMultiplier();
            Vector moveDirection = event.getTo().toVector().subtract(event.getFrom().toVector());
            velocity.setX(moveDirection.getX() * jump * player.getWalkSpeed());
            velocity.setY(velocity.getY() * jump / 1.5);
            velocity.setZ(moveDirection.getZ() * jump * player.getWalkSpeed());
            player.setVelocity(velocity);
        }
        // === END: Magic constants ===
    }

    @EventHandler
    public void onPlayerFall(@NotNull EntityDamageEvent event) {
        if (event.getEntity() == null
                || event.getEntity().getType() != EntityType.PLAYER
                || event.getCause() != EntityDamageEvent.DamageCause.FALL) {
            return;
        }

        Player player = (Player) event.getEntity();

        if (!InventoryManager.playerIsLoaded(player)) {
            return;
        }

        Modifier jumpModifier = ItemManager.getModifier(player, ItemStat.StatType.JUMP);
        double height = (1.5D + jumpModifier.getBonus()) * jumpModifier.getMultiplier() * 1.5;
        event.setDamage(event.getDamage() - height);

        if (event.getDamage() <= 0.0D) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onItemUse(@NotNull PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (!InventoryManager.playerIsLoaded(player) || !event.hasItem()) {
            return;
        }

        if (!ItemManager.allowedForPlayer(player, event.getItem(), true)) {
            event.setCancelled(true);
            PlayerUtils.updateInventory(player);
            return;
        }

        if (CustomItem.isCustomItem(event.getItem())) {
            CustomItem customItem = ItemManager.getCustomItem(event.getItem());
            if (customItem == null) {
                return;
            }

            if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                customItem.onRightClick(player);
            } else {
                customItem.onLeftClick(player);
            }

            if (event.getItem().getType() != Material.BOW) {
                event.setCancelled(true);
            }
        }

        ItemManager.updateStats(player);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void afterPlayerRespawn(@NotNull PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        if (!InventoryManager.playerIsLoaded(player)) {
            return;
        }

        ItemManager.updateStats(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void afterEquipChange(@NotNull InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();

        if (!InventoryManager.playerIsLoaded(player)) {
            return;
        }

        if (CustomItem.isCustomItem(event.getCursor()) || CustomItem.isCustomItem(event.getCurrentItem())) {
            ItemManager.updateStats(player);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void afterEquipChange(@NotNull final InventoryDragEvent event) {
        final Player player = (Player) event.getWhoClicked();

        if (!InventoryManager.playerIsLoaded(player)) {
            return;
        }

        new TrackedBukkitRunnable() {
            @Override
            public void run() {
                InventoryView inventoryView = event.getView();
                for (int slot : event.getRawSlots()) {
                    ItemStack item = inventoryView.getItem(slot);
                    if (CustomItem.isCustomItem(item)) {
                        ItemManager.updateStats((Player) event.getWhoClicked());
                    }
                }
            }
        }.runTaskLater(RPGInventory.getInstance(), 1);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void afterItemHeld(@NotNull final PlayerItemHeldEvent event) {
        Player player = event.getPlayer();

        if (!InventoryManager.playerIsLoaded(player)) {
            return;
        }

        final ItemStack newItem = player.getInventory().getItem(event.getNewSlot());
        final ItemStack oldItem = player.getInventory().getItem(event.getPreviousSlot());

        new TrackedBukkitRunnable() {
            @Override
            public void run() {
                if (CustomItem.isCustomItem(oldItem) || CustomItem.isCustomItem(newItem)) {
                    ItemManager.updateStats(event.getPlayer());
                }
            }
        }.runTaskLater(RPGInventory.getInstance(), 2);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void afterPickupItem(@NotNull PlayerPickupItemEvent event) {
        Player player = event.getPlayer();

        if (!InventoryManager.playerIsLoaded(player)) {
            return;
        }

        ItemStack item = event.getItem().getItemStack();
        if (CustomItem.isCustomItem(item)) {
            ItemManager.updateStats(player);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void afterDropItem(@NotNull PlayerDropItemEvent event) {
        Player player = event.getPlayer();

        if (!InventoryManager.playerIsLoaded(player)) {
            return;
        }

        if (CustomItem.isCustomItem(event.getItemDrop().getItemStack())) {
            ItemManager.updateStats(player);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void afterBreakItem(@NotNull PlayerItemBreakEvent event) {
        Player player = event.getPlayer();

        if (!InventoryManager.playerIsLoaded(player)) {
            return;
        }

        if (CustomItem.isCustomItem(event.getBrokenItem())) {
            ItemManager.updateStats(player);
        }
    }
}
