package ru.endlesscode.rpginventory.event.listener;

import com.comphenix.protocol.wrappers.EnumWrappers;
import org.bukkit.Sound;
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
import org.bukkit.event.player.*;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import ru.endlesscode.rpginventory.RPGInventory;
import ru.endlesscode.rpginventory.inventory.InventoryManager;
import ru.endlesscode.rpginventory.item.CustomItem;
import ru.endlesscode.rpginventory.item.ItemManager;
import ru.endlesscode.rpginventory.item.ItemStat;
import ru.endlesscode.rpginventory.item.Modifier;
import ru.endlesscode.rpginventory.utils.EffectUtils;
import ru.endlesscode.rpginventory.utils.ItemUtils;

/**
 * Created by OsipXD on 19.09.2015
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
public class ItemListener implements Listener {
    @EventHandler(priority = EventPriority.LOW)
    public void onDamage(EntityDamageByEntityEvent event) {
        Player damager;

        // Defensive stats
        try {
            if (event.getEntityType() == EntityType.PLAYER) {
                Modifier armorModifier = ItemManager.getModifier((Player) event.getEntity(), ItemStat.StatType.ARMOR);
                double armor = (event.getDamage(EntityDamageEvent.DamageModifier.ARMOR) - armorModifier.getBonus()) * armorModifier.getMultiplier();
                if (armor > 0.0D) {
                    armor = 0.0D;
                }

                event.setDamage(EntityDamageEvent.DamageModifier.ARMOR, armor);
            }
        } catch (UnsupportedOperationException ignored) {
        }

        // Attack stats
        Modifier damageModifier;
        ItemStack itemInHand;
        if (event.getDamager().getType() == EntityType.PLAYER) {
            damager = (Player) event.getDamager();
            itemInHand = damager.getEquipment().getItemInMainHand();
            damageModifier = ItemManager.getModifier(damager,
                    ItemUtils.isEmpty(itemInHand) ? ItemStat.StatType.HAND_DAMAGE : ItemStat.StatType.DAMAGE);
        } else if (event.getDamager().getType() == EntityType.ARROW && ((Arrow) event.getDamager()).getShooter() instanceof Player) {
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

        double damage = (CustomItem.isCustomItem(itemInHand) ? 1 : event.getDamage(EntityDamageEvent.DamageModifier.BASE))
                + damageModifier.getBonus() * damageModifier.getMultiplier();
        double critChance = ItemManager.getModifier(damager, ItemStat.StatType.CRIT_CHANCE).getMultiplier() - 1.0;
        if (Math.random() <= critChance) {
            damage *= ItemManager.getModifier(damager, ItemStat.StatType.CRIT_DAMAGE).getMultiplier();
            damager.getWorld().playSound(event.getEntity().getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT,
                    1, (float) (0.5 + Math.random() * 0.4));
            EffectUtils.playParticlesToAll(EnumWrappers.Particle.CRIT, 10, event.getEntity().getLocation());
        }

        event.setDamage(EntityDamageEvent.DamageModifier.BASE, damage);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJump(PlayerMoveEvent event) {
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
    public void onPlayerFall(EntityDamageEvent event) {
        if (event.getEntity().getType() == EntityType.PLAYER && event.getCause() == EntityDamageEvent.DamageCause.FALL) {
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
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onItemUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (!InventoryManager.playerIsLoaded(player) || !event.hasItem()) {
            return;
        }

        if (!ItemManager.allowedForPlayer(player, event.getItem(), true)) {
            event.setCancelled(true);
            return;
        }

        if (CustomItem.isCustomItem(event.getItem())) {
            CustomItem customItem = ItemManager.getCustomItem(event.getItem());

            if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                customItem.onRightClick(player);
            } else {
                customItem.onLeftClick(player);
            }

            event.setCancelled(true);
        }

        ItemManager.updateStats(player);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void afterPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        if (!InventoryManager.playerIsLoaded(player)) {
            return;
        }

        ItemManager.updateStats(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void afterEquipChange(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();

        if (!InventoryManager.playerIsLoaded(player)) {
            return;
        }

        if (CustomItem.isCustomItem(event.getCursor()) || CustomItem.isCustomItem(event.getCurrentItem())) {
            ItemManager.updateStats(player);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void afterEquipChange(final InventoryDragEvent event) {
        final Player player = (Player) event.getWhoClicked();

        if (!InventoryManager.playerIsLoaded(player)) {
            return;
        }

        new BukkitRunnable() {
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
    public void afterItemHeld(final PlayerItemHeldEvent event) {
        Player player = event.getPlayer();

        if (!InventoryManager.playerIsLoaded(player)) {
            return;
        }

        final ItemStack newItem = player.getInventory().getItem(event.getNewSlot());
        final ItemStack oldItem = player.getInventory().getItem(event.getPreviousSlot());

        new BukkitRunnable() {
            @Override
            public void run() {
                if (CustomItem.isCustomItem(oldItem) || CustomItem.isCustomItem(newItem)) {
                    ItemManager.updateStats(event.getPlayer());
                }
            }
        }.runTaskLater(RPGInventory.getInstance(), 2);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void afterPickupItem(PlayerPickupItemEvent event) {
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
    public void afterDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();

        if (!InventoryManager.playerIsLoaded(player)) {
            return;
        }

        if (CustomItem.isCustomItem(event.getItemDrop().getItemStack())) {
            ItemManager.updateStats(player);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void afterBreakItem(PlayerItemBreakEvent event) {
        Player player = event.getPlayer();

        if (!InventoryManager.playerIsLoaded(player)) {
            return;
        }

        if (CustomItem.isCustomItem(event.getBrokenItem())) {
            ItemManager.updateStats(player);
        }
    }
}
