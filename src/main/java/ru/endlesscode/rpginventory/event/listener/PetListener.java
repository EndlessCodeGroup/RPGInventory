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

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.inventory.HorseInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import ru.endlesscode.rpginventory.RPGInventory;
import ru.endlesscode.rpginventory.inventory.InventoryManager;
import ru.endlesscode.rpginventory.inventory.PlayerWrapper;
import ru.endlesscode.rpginventory.inventory.slot.Slot;
import ru.endlesscode.rpginventory.inventory.slot.SlotManager;
import ru.endlesscode.rpginventory.item.ItemManager;
import ru.endlesscode.rpginventory.misc.Config;
import ru.endlesscode.rpginventory.pet.PetFood;
import ru.endlesscode.rpginventory.pet.PetManager;
import ru.endlesscode.rpginventory.pet.PetType;
import ru.endlesscode.rpginventory.utils.EntityUtils;
import ru.endlesscode.rpginventory.utils.ItemUtils;
import ru.endlesscode.rpginventory.utils.PlayerUtils;

/**
 * Created by OsipXD on 18.09.2015
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
public class PetListener implements Listener {
    @EventHandler
    public void onItemUse(PlayerInteractEvent event) {
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
            Slot petSlot = SlotManager.getSlotManager().getPetSlot();
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
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        if (!InventoryManager.playerIsLoaded(player) || !PetManager.isEnabled()) {
            return;
        }

        PetManager.despawnPet(player);
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        if (!InventoryManager.playerIsLoaded(player) || !PetManager.isEnabled()) {
            return;
        }

        ItemStack petItem = InventoryManager.get(player).getInventory().getItem(PetManager.getPetSlotId());
        if (!ItemUtils.isEmpty(petItem)) {
            PetManager.spawnPet(player, petItem);
        }
    }

    @EventHandler
    public void onPetFeed(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        ItemStack itemInHand = player.getEquipment().getItemInMainHand();

        if (!InventoryManager.playerIsLoaded(player)) {
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

            pet.getWorld().playSound(pet.getLocation(), Sound.ENTITY_GENERIC_EAT, 1.0f, (float) (1.0 + Math.random()*0.4));
        }
    }

    @EventHandler
    public void onPetDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Tameable)) {
            return;
        }

        Tameable petEntity = (Tameable) event.getEntity();
        final OfflinePlayer player;
        if (!petEntity.isTamed() || (player = (OfflinePlayer) petEntity.getOwner()) == null || !player.isOnline()) {
            return;
        }

        if (!InventoryManager.playerIsLoaded(player)) {
            return;
        }

        PlayerWrapper playerWrapper = InventoryManager.get(player);
        if (petEntity == playerWrapper.getPet()) {
            Inventory inventory = playerWrapper.getInventory();
            final ItemStack petItem = inventory.getItem(PetManager.getPetSlotId());
            PetType petType = PetManager.getPetFromItem(petItem);

            if (petType != null && petType.isRevival()) {
                PetManager.setCooldown(petItem, petType.getCooldown());
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
    public void onTarget(EntityTargetLivingEntityEvent event) {
        if (!(event.getEntity() instanceof Tameable) || !(event.getEntity() instanceof LivingEntity)
                || event.getTarget() == null || !InventoryManager.isAllowedWorld(event.getTarget().getWorld())) {
            return;
        }

        LivingEntity petEntity = (LivingEntity) event.getEntity();
        final OfflinePlayer player = (OfflinePlayer) ((Tameable) petEntity).getOwner();

        if (!InventoryManager.playerIsLoaded(player)) {
            return;
        }

        PlayerWrapper playerWrapper = InventoryManager.get(player);
        if (playerWrapper.hasPet() && petEntity == playerWrapper.getPet()) {
            PetType petType = PetManager.getPetFromEntity((Tameable) petEntity);

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

    @EventHandler
    public void onAttack(EntityDamageByEntityEvent event) {
        if (event.isCancelled() || !InventoryManager.isAllowedWorld(event.getEntity().getWorld())) {
            return;
        }

        Player player = null;
        if (event.getDamager().getType() == EntityType.PLAYER) {
            player = (Player) event.getDamager();
        } else if (event.getDamager().getType() == EntityType.ARROW) {
            Arrow arrow = (Arrow) event.getDamager();
            if (arrow.getShooter() instanceof Player) {
                player = (Player) arrow.getShooter();
            }
        }

        if (player != null && !InventoryManager.playerIsLoaded(player)) {
            return;
        }

        LivingEntity petEntity;
        if (event.getDamager() instanceof LivingEntity && (petEntity = (LivingEntity) event.getDamager()) instanceof Tameable) {
            PetType petType = PetManager.getPetFromEntity((Tameable) petEntity);

            if (petType != null) {
                event.setDamage(petType.getDamage());
            }
        } else if (event.getEntity() instanceof LivingEntity && (petEntity = (LivingEntity) event.getEntity()) instanceof Tameable
                && !Config.getConfig().getBoolean("attack.own-pet") && player != null) {
            Tameable ownedEntity = (Tameable) petEntity;
            if (ownedEntity.isTamed() && ownedEntity.getOwner().getUniqueId().equals(event.getDamager().getUniqueId())) {
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
    public void onMountPet(VehicleEnterEvent event) {
        if (event.getEntered().getType() != EntityType.PLAYER || event.getVehicle().getType() != EntityType.HORSE) {
            return;
        }

        Player player = (Player) event.getEntered();
        if (!InventoryManager.playerIsLoaded(player)) {
            return;
        }

        Horse vehicle = (Horse) event.getVehicle();
        if (PetManager.getPetFromEntity(vehicle) != null && player != vehicle.getOwner()) {
            PlayerUtils.sendMessage(player, RPGInventory.getLanguage().getCaption("error.mount.owner", vehicle.getOwner().getName()));
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onWorldChanged(EntityPortalEnterEvent event) {
        if (!(event.getEntity() instanceof Tameable) || !(event.getEntity() instanceof LivingEntity)) {
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
    public void onPetInventoryOpened(InventoryOpenEvent event) {
        Player player = (Player) event.getPlayer();
        if (!InventoryManager.playerIsLoaded(player)) {
            return;
        }

        final PlayerWrapper playerWrapper = InventoryManager.get(player);
        if (playerWrapper.hasPet() && event.getInventory().getHolder() == playerWrapper.getPet()) {
            playerWrapper.openInventory();
            event.setCancelled(true);

            new BukkitRunnable() {
                @Override
                public void run() {
                    HorseInventory horseInv = ((Horse) playerWrapper.getPet()).getInventory();
                    horseInv.setSaddle(new ItemStack(Material.SADDLE));
                }
            }.runTaskLater(RPGInventory.getInstance(), 1);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (event.isCancelled() || !InventoryManager.playerIsLoaded(player)) {
            return;
        }

        PlayerWrapper playerWrapper = InventoryManager.get(player);
        if (playerWrapper.hasPet() && playerWrapper.getPet().getPassenger() != player) {
            LivingEntity petEntity = playerWrapper.getPet();
            PetType pet = PetManager.getPetFromEntity((Tameable) petEntity);
            if (pet != null && pet.getRole() != PetType.Role.COMPANION) {
                EntityUtils.goPetToPlayer(player, petEntity);
            }
        }
    }
}
