package ru.endlesscode.rpginventory.event.listener;

import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import ru.endlesscode.rpginventory.RPGInventory;
import ru.endlesscode.rpginventory.inventory.InventoryManager;
import ru.endlesscode.rpginventory.inventory.PlayerWrapper;
import ru.endlesscode.rpginventory.inventory.slot.Slot;
import ru.endlesscode.rpginventory.inventory.slot.SlotManager;
import ru.endlesscode.rpginventory.misc.Config;
import ru.endlesscode.rpginventory.nms.VersionHandler;
import ru.endlesscode.rpginventory.pet.PetFood;
import ru.endlesscode.rpginventory.pet.PetManager;
import ru.endlesscode.rpginventory.pet.PetType;
import ru.endlesscode.rpginventory.utils.EntityUtils;
import ru.endlesscode.rpginventory.utils.StringUtils;

import java.util.List;

/**
 * Created by OsipXD on 18.09.2015
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2015 © «EndlessCode Group»
 */
public class PetListener implements Listener {
    @EventHandler
    public void onItemUse(PlayerInteractEvent event) {
        if (event.getItem() != null) {
            Player player = event.getPlayer();

            if (!InventoryManager.playerIsLoaded(player)) {
                return;
            }

            Inventory inventory = InventoryManager.get(player).getInventory();
            ItemStack petItem = event.getItem();

            if (player.getGameMode() == GameMode.CREATIVE && PetManager.isPetItem(petItem)) {
                petItem = PetManager.toPetItem(petItem);
                if (VersionHandler.is1_9()) {
                    player.getEquipment().setItemInMainHand(petItem);
                } else {
                    //noinspection deprecation
                    player.setItemInHand(petItem);
                }
            }

            if (PetType.isPetItem(petItem) && (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR)) {
                Slot petSlot = SlotManager.getSlotManager().getPetSlot();
                if (petSlot != null && petSlot.isCup(inventory.getItem(PetManager.getPetSlotId()))) {
                    inventory.setItem(PetManager.getPetSlotId(), event.getItem());
                    PetManager.spawnPet(player, petItem);
                    if (VersionHandler.is1_9()) {
                        player.getEquipment().setItemInMainHand(null);
                    } else {
                        //noinspection deprecation
                        player.setItemInHand(null);
                    }
                }

                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        if (!InventoryManager.playerIsLoaded(player) || !PetManager.isEnabled()) {
            return;
        }

        ItemStack petItem = InventoryManager.get(player).getInventory().getItem(PetManager.getPetSlotId());
        if (petItem != null) {
            PetManager.spawnPet(player, petItem);
        }
    }

    @EventHandler
    public void onPetFeed(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        @SuppressWarnings("deprecation") ItemStack itemInHand = (VersionHandler.is1_9()) ? player.getEquipment().getItemInMainHand() : player.getItemInHand();

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
            if (VersionHandler.is1_9()) {
                player.getEquipment().setItemInMainHand(itemInHand);
            } else {
                //noinspection deprecation
                player.setItemInHand(itemInHand);
            }

            pet.getWorld().playSound(pet.getLocation(),
                    VersionHandler.is1_9() ? Sound.ENTITY_GENERIC_EAT : Sound.valueOf("EAT"),
                    1.0f, (float) (1.0 + Math.random() * 0.4));
        }
    }

    @EventHandler
    public void onPetDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Tameable)) {
            return;
        }

        LivingEntity petEntity = event.getEntity();
        if (!((OfflinePlayer) ((Tameable) petEntity).getOwner()).isOnline()) {
            return;
        }

        final Player player = (Player) ((Tameable) petEntity).getOwner();
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
                PetManager.startCooldownTimer(player, petItem);
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
        if (!InventoryManager.isAllowedWorld(event.getEntity().getWorld())) {
            return;
        }

        LivingEntity petEntity;
        if (event.getDamager() instanceof LivingEntity && (petEntity = (LivingEntity) event.getDamager()) instanceof Tameable) {
            PetType petType = PetManager.getPetFromEntity((Tameable) petEntity);

            if (petType != null) {
                event.setDamage(petType.getDamage());
            }
        } else if (event.getEntity() instanceof LivingEntity && (petEntity = (LivingEntity) event.getEntity()) instanceof Tameable
                && !Config.getConfig().getBoolean("attack.own-pet")) {
            Tameable ownedEntity = (Tameable) petEntity;
            if (event.getDamager().getType() == EntityType.PLAYER && ownedEntity.isTamed()
                    && ownedEntity.getOwner().getName().equals(event.getDamager().getName())) {
                event.setCancelled(true);
            } else if (event.getDamager().getType() == EntityType.ARROW) {
                Arrow arrow = (Arrow) event.getDamager();
                if (arrow.getShooter() instanceof Player && ownedEntity.isTamed()
                        && ownedEntity.getOwner().getUniqueId().equals(((Player) arrow.getShooter()).getUniqueId())) {
                    event.setCancelled(true);
                }
            }
        } else if (event.getDamager().getType() == EntityType.PLAYER) {
            @SuppressWarnings("ConstantConditions")
            Player player = (Player) event.getDamager();
            if (!InventoryManager.playerIsLoaded(player)) {
                return;
            }

            if (SlotManager.getSlotManager().getSlot(player.getInventory().getHeldItemSlot(), InventoryType.SlotType.QUICKBAR) == null) {
                List<Slot> activeSlots = SlotManager.getSlotManager().getActiveSlots();
                if (activeSlots.size() == 0) {
                    return;
                }

                if (Config.getConfig().getBoolean("attack.force-weapon")) {
                    for (Slot activeSlot : activeSlots) {
                        if (!InventoryManager.isQuickEmptySlot(player.getInventory().getItem(activeSlot.getQuickSlot()))) {
                            player.getInventory().setHeldItemSlot(activeSlot.getQuickSlot());
                            break;
                        }
                    }
                }

                if (Config.getConfig().getBoolean("attack.require-weapon")) {
                    event.setCancelled(true);
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
            player.sendMessage(String.format(StringUtils.coloredLine(RPGInventory.getLanguage().getCaption("error.mount.owner")), vehicle.getOwner().getName()));
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

        PlayerWrapper playerWrapper = InventoryManager.get(player);
        if (playerWrapper.hasPet() && event.getInventory().getHolder() == playerWrapper.getPet()) {
            playerWrapper.openInventory();
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!InventoryManager.playerIsLoaded(player)) {
            return;
        }

        PlayerWrapper playerWrapper = InventoryManager.get(player);
        if (playerWrapper.hasPet() && playerWrapper.getPet().getPassenger() != player) {
            PetType pet = PetManager.getPetFromEntity((Tameable) playerWrapper.getPet());
            if (pet != null && pet.getRole() != PetType.Role.COMPANION) {
                EntityUtils.goToPlayer(player, playerWrapper.getPet());
            }
        }
    }
}
