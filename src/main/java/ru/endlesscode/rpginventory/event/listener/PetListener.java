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

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.inventory.HorseInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import ru.endlesscode.inspector.bukkit.scheduler.TrackedBukkitRunnable;
import ru.endlesscode.rpginventory.RPGInventory;
import ru.endlesscode.rpginventory.inventory.InventoryManager;
import ru.endlesscode.rpginventory.inventory.PlayerWrapper;
import ru.endlesscode.rpginventory.inventory.slot.Slot;
import ru.endlesscode.rpginventory.inventory.slot.SlotManager;
import ru.endlesscode.rpginventory.item.ItemManager;
import ru.endlesscode.rpginventory.misc.config.Config;
import ru.endlesscode.rpginventory.pet.PetFood;
import ru.endlesscode.rpginventory.pet.PetManager;
import ru.endlesscode.rpginventory.pet.PetType;
import ru.endlesscode.rpginventory.utils.EntityUtils;
import ru.endlesscode.rpginventory.utils.ItemUtils;
import ru.endlesscode.rpginventory.utils.LocationUtils;
import ru.endlesscode.rpginventory.utils.PlayerUtils;

import java.util.UUID;

/**
 * Created by OsipXD on 18.09.2015
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
public class PetListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() == null
                || !(event.getRightClicked() instanceof LivingEntity)) {
            return;
        }

        //Avoid using owner's pet by other players
        final LivingEntity rightClicked = (LivingEntity) event.getRightClicked();
        final UUID petOwner = PetManager.getPetOwner(rightClicked);
        if (petOwner == null) {
            return;
        }
        if (!event.getPlayer().getUniqueId().equals(petOwner)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemUse(@NotNull PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (!event.hasItem() || !InventoryManager.playerIsLoaded(player)) {
            return;
        }

        Inventory inventory = InventoryManager.get(player).getInventory();
        ItemStack petItem = event.getItem();

        if (player.getGameMode() == GameMode.CREATIVE && PetManager.isPetItem(petItem)) {
            petItem = PetManager.toPetItem(petItem);
            player.getEquipment().setItemInMainHand(petItem);
        }

        if (PetType.isPetItem(petItem)
                && (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR)) {
            Slot petSlot = SlotManager.instance().getPetSlot();
            if (petSlot != null && petSlot.isCup(inventory.getItem(PetManager.getPetSlotId()))
                    && ItemManager.allowedForPlayer(player, petItem, false)) {
                inventory.setItem(PetManager.getPetSlotId(), event.getItem());
                PetManager.spawnPet(player, petItem);
                player.getEquipment().setItemInMainHand(null);
            }

            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDeath(@NotNull PlayerDeathEvent event) {
        Player player = event.getEntity();

        if (!InventoryManager.playerIsLoaded(player) || !PetManager.isEnabled()) {
            return;
        }

        PetManager.despawnPet(player);
    }

    @EventHandler
    public void onPlayerRespawn(@NotNull PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        if (!InventoryManager.playerIsLoaded(player) || !PetManager.isEnabled()) {
            return;
        }

        ItemStack petItem = InventoryManager.get(player).getInventory().getItem(PetManager.getPetSlotId());
        if (!ItemUtils.isEmpty(petItem)) {
            PetManager.spawnPet(player, petItem);
        }
    }

    //Possible fix #110
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerTeleport(@NotNull PlayerTeleportEvent event) {
        Player player = event.getPlayer();

        if (!InventoryManager.playerIsLoaded(player) || !PetManager.isEnabled()) {
            return;
        }

        if (!InventoryManager.get(player).hasPet()) {
            return;
        }

        // Ugly trick to avoid infinite pet spawning when player teleports from non-solid/non-cuboid block
        final Location from = event.getFrom();
        final Location to = event.getTo();
        if (from.getBlockX() != to.getBlockX() || from.getBlockZ() != to.getBlockZ() || from.distance(to) < 0.775D) {
            return;
        }

        final double maxDistance = (event.getPlayer().getServer().getViewDistance() / 2.0D) * 15.75D;
        final ItemStack item = InventoryManager.get(player).getInventory().getItem(PetManager.getPetSlotId());
        if (from.distance(to) > maxDistance && item != null) {
            PetManager.spawnPet(player, item);
        } else if (LocationUtils.isSafeLocation(player.getLocation())) {
            PetManager.teleportPet(player, to);
        }

    }

    @EventHandler
    public void onPetFeed(@NotNull PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        ItemStack itemInHand = player.getEquipment().getItemInMainHand();

        if (event.getRightClicked() == null || !InventoryManager.playerIsLoaded(player)) {
            return;
        }

        PlayerWrapper playerWrapper = InventoryManager.get(player);
        if (PetFood.isFoodItem(itemInHand) && playerWrapper.hasPet() && event.getRightClicked() == playerWrapper.getPet()) {
            event.setCancelled(true);

            LivingEntity pet = (LivingEntity) event.getRightClicked();
            PetFood petFood = PetManager.getFoodFromItem(itemInHand);

            if (pet.getHealth() == pet.getMaxHealth() || petFood == null || !petFood.canBeEaten(playerWrapper.getPet())) {
                return;
            }

            double health = pet.getHealth() + petFood.getValue();
            pet.setHealth(health < pet.getMaxHealth() ? health : pet.getMaxHealth());
            itemInHand.setAmount(itemInHand.getAmount() - 1);
            player.getEquipment().setItemInMainHand(itemInHand);

            pet.getWorld().playSound(pet.getLocation(), Sound.ENTITY_GENERIC_EAT, 1.0f, (float) (1.0 + Math.random() * 0.4));
        }
    }

    @EventHandler
    public void onPetDeath(@NotNull EntityDeathEvent event) {
        if (event.getEntity() == null || PetManager.getPetOwner(event.getEntity()) == null) {
            return;
        }

        LivingEntity petEntity = event.getEntity();
        final Player player = Bukkit.getPlayer(PetManager.getPetOwner(petEntity));

        if (!InventoryManager.playerIsLoaded(player)) {
            return;
        }

        final PlayerWrapper playerWrapper = InventoryManager.get(player);
        if (petEntity == playerWrapper.getPet()) {
            Inventory inventory = playerWrapper.getInventory();
            ItemStack petItem = inventory.getItem(PetManager.getPetSlotId());
            PetType petType = PetManager.getPetFromItem(petItem);

            if (petType != null && petType.isRevival()) {
                PetManager.saveDeathTime(petItem);
                PetManager.saveHealth(petItem, 0);
                inventory.setItem(PetManager.getPetSlotId(), petItem);
                PetManager.startCooldownTimer(player.getPlayer(), petItem);
            } else {
                inventory.setItem(PetManager.getPetSlotId(), null);
            }

            event.getDrops().clear();
            playerWrapper.setPet(null);
        }
    }

    @EventHandler
    public void onTarget(@NotNull EntityTargetLivingEntityEvent event) {
        if (event.getEntity() == null
                || !(event.getEntity() instanceof Tameable)
                || !(event.getEntity() instanceof LivingEntity)
                || event.getTarget() == null
                || !InventoryManager.isAllowedWorld(event.getTarget().getWorld())) {
            return;
        }

        LivingEntity petEntity = (LivingEntity) event.getEntity();
        final OfflinePlayer player = (OfflinePlayer) ((Tameable) petEntity).getOwner();

        if (!InventoryManager.playerIsLoaded(player)) {
            return;
        }

        PlayerWrapper playerWrapper = InventoryManager.get(player);
        if (playerWrapper.hasPet() && petEntity == playerWrapper.getPet()) {
            PetType petType = PetManager.getPetFromEntity(petEntity, player);

            if (petType == null) {
                return;
            }

            if (event.getTarget() != null && event.getTarget().getType() == EntityType.PLAYER) {
                event.setCancelled(!petType.isAttackPlayers());
            } else {
                event.setCancelled(!petType.isAttackMobs());
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onAttack(@NotNull EntityDamageByEntityEvent event) {
        if (event.getEntity() == null
                || event.getDamager() == null
                || !InventoryManager.isAllowedWorld(event.getEntity().getWorld())) {
            return;
        }

        Player player = null;
        if (event.getDamager().getType() == EntityType.PLAYER) {
            player = (Player) event.getDamager();
        } else if (event.getDamager() instanceof Projectile) {
            Projectile projectile = (Projectile) event.getDamager();
            if (projectile.getShooter() instanceof Player) {
                player = (Player) projectile.getShooter();
            }
        }

        if (player != null && !InventoryManager.playerIsLoaded(player)) {
            return;
        }

        //Trying to get a pet as damager
        if (event.getDamager() instanceof LivingEntity && event.getDamager() instanceof Tameable) {
            //Casting to Tameable because we don't need in LivingEntity
            final Tameable petEntity = (Tameable) event.getDamager();
            final AnimalTamer petOwner = petEntity.getOwner();
            if (petOwner != null) {
                OfflinePlayer petOwnerPlayer = (OfflinePlayer) petOwner;
                PetType petType = PetManager.getPetFromEntity((LivingEntity) petEntity, petOwnerPlayer);
                if (petType != null) {
                    if (petOwnerPlayer.isOnline()) {
                        event.setDamage(petType.getDamage());
                    } else {
                        PetManager.despawnPet(petEntity);
                    }
                }
            }
            //or as damage reciever
        } else if (event.getEntity() instanceof LivingEntity && event.getEntity() instanceof Tameable
                && !Config.getConfig().getBoolean("attack.own-pet") && player != null) {
            final Tameable petEntity = (Tameable) event.getEntity();
            final AnimalTamer petOwner = petEntity.getOwner();
            if (petOwner != null && player.getUniqueId().equals(petOwner.getUniqueId())) {
                event.setCancelled(true);
            }
        } else if (player != null) {
            PlayerWrapper wrapper = InventoryManager.get(player);
            LivingEntity pet = wrapper.getPet();

            if (pet != null && pet.getType() == EntityType.WOLF && player != ((Wolf) pet).getTarget()) {
                Location target = player.getLocation();
                if (target.distance(pet.getLocation()) > 20) {
                    PetManager.respawnPet(player);
                }
            }
        }
    }

    @EventHandler
    public void onMountPet(@NotNull VehicleEnterEvent event) {
        if (event.getEntered().getType() != EntityType.PLAYER || event.getVehicle().getType() != EntityType.HORSE) {
            return;
        }

        Player player = (Player) event.getEntered();
        if (!InventoryManager.playerIsLoaded(player)) {
            return;
        }

        Horse vehicle = (Horse) event.getVehicle();
        if (PetManager.getPetFromEntity(vehicle, player) != null && player != vehicle.getOwner()) {
            PlayerUtils.sendMessage(player, RPGInventory.getLanguage().getMessage("error.mount.owner", vehicle.getOwner().getName()));
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onWorldChanged(@NotNull EntityPortalEnterEvent event) {
        if (event.getEntity() == null
                || !(event.getEntity() instanceof Tameable)
                || !(event.getEntity() instanceof LivingEntity)) {
            return;
        }

        OfflinePlayer player = (OfflinePlayer) ((Tameable) event.getEntity()).getOwner();
        if (!InventoryManager.playerIsLoaded(player)) {
            return;
        }

        PlayerWrapper playerWrapper = InventoryManager.get(player);
        if (event.getEntity() == playerWrapper.getPet()) {
            PetManager.respawnPet(player);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPetInventoryOpened(@NotNull InventoryOpenEvent event) {
        Player player = (Player) event.getPlayer();
        if (!InventoryManager.playerIsLoaded(player)) {
            return;
        }

        final PlayerWrapper playerWrapper = InventoryManager.get(player);
        if (playerWrapper.hasPet() && event.getInventory().getHolder() == playerWrapper.getPet()) {
            playerWrapper.openInventory();
            event.setCancelled(true);

            new TrackedBukkitRunnable() {
                @Override
                public void run() {
                    HorseInventory horseInv = ((Horse) playerWrapper.getPet()).getInventory();
                    horseInv.setSaddle(new ItemStack(Material.SADDLE));
                }
            }.runTaskLater(RPGInventory.getInstance(), 1);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerMove(@NotNull PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!InventoryManager.playerIsLoaded(player)) {
            return;
        }

        PlayerWrapper playerWrapper = InventoryManager.get(player);
        if (playerWrapper.hasPet() && playerWrapper.getPet().getPassenger() != player) {
            LivingEntity petEntity = playerWrapper.getPet();
            PetType pet = PetManager.getPetFromEntity(petEntity, player);
            if (pet != null && pet.getRole() != PetType.Role.COMPANION) {
                EntityUtils.goPetToPlayer(player, petEntity);
            }
        }
    }
}
