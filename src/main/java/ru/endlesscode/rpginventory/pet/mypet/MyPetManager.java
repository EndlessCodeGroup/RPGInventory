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

package ru.endlesscode.rpginventory.pet.mypet;

import com.google.common.base.Optional;
import de.Keyle.MyPet.MyPetApi;
import de.Keyle.MyPet.api.WorldGroup;
import de.Keyle.MyPet.api.entity.MyPet;
import de.Keyle.MyPet.api.entity.StoredMyPet;
import de.Keyle.MyPet.api.event.MyPetCallEvent;
import de.Keyle.MyPet.api.event.MyPetCreateEvent;
import de.Keyle.MyPet.api.event.MyPetRemoveEvent;
import de.Keyle.MyPet.api.player.MyPetPlayer;
import de.Keyle.MyPet.api.repository.PlayerManager;
import de.Keyle.MyPet.api.repository.RepositoryCallback;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.endlesscode.rpginventory.RPGInventory;
import ru.endlesscode.rpginventory.event.PetEquipEvent;
import ru.endlesscode.rpginventory.event.PetUnequipEvent;
import ru.endlesscode.rpginventory.event.PlayerInventoryLoadEvent;
import ru.endlesscode.rpginventory.inventory.ActionType;
import ru.endlesscode.rpginventory.inventory.InventoryManager;
import ru.endlesscode.rpginventory.inventory.slot.Slot;
import ru.endlesscode.rpginventory.inventory.slot.SlotManager;
import ru.endlesscode.rpginventory.utils.ItemUtils;
import ru.endlesscode.rpginventory.utils.PlayerUtils;

import java.util.UUID;

/**
 * Created by Keyle on 21.05.2016
 * It is part of the RPGInventory.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */

public class MyPetManager implements Listener {
    private static final String MYPET_TAG = "mypet.uuid";

    public static boolean init(RPGInventory instance) {
        if (MyPetManager.getMyPetSlot() == null) {
            instance.getLogger().warning("MyPet found, but slot for MyPet not configured!");
            return false;
        }

        for (MyPetPlayer mpPlayer : MyPetApi.getPlayerManager().getMyPetPlayers()) {
            syncPlayer(mpPlayer);
        }

        instance.getServer().getPluginManager().registerEvents(new MyPetManager(), instance);
        return true;
    }

    @EventHandler
    public void onPlayerInventoryLoaded(PlayerInventoryLoadEvent.Post event) {
        Player player = event.getPlayer();
        PlayerManager playerManager = MyPetApi.getPlayerManager();
        if (playerManager.isMyPetPlayer(player)) {
            syncPlayer(playerManager.getMyPetPlayer(player));
        }
    }

    private static void syncPlayer(MyPetPlayer mpPlayer) {
        Slot petSlot = getMyPetSlot();
        if (mpPlayer.isOnline() && mpPlayer.hasMyPet()) {
            Player player = mpPlayer.getPlayer();

            if (!InventoryManager.playerIsLoaded(player)) {
                return;
            }

            Inventory inventory = InventoryManager.get(player).getInventory();
            ItemStack currentPet = inventory.getItem(petSlot.getSlotId());
            if (isMyPetItem(currentPet)) {
                MyPet pet = mpPlayer.getMyPet();
                UUID petUUID = UUID.fromString(ItemUtils.getTag(currentPet, MYPET_TAG));
                if (petUUID.equals(pet.getUUID())) {
                    return;
                }
            } else {
                inventory.setItem(petSlot.getSlotId(), petSlot.getCup());
            }

            MyPetApi.getMyPetManager().deactivateMyPet(mpPlayer, false);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onMyPetCreate(MyPetCreateEvent event) {
        Player player = event.getOwner().getPlayer();
        if (!InventoryManager.playerIsLoaded(player)) {
            return;
        }

        ItemStack petItem = new ItemStack(Material.MONSTER_EGG);
        ItemMeta meta = petItem.getItemMeta();
        meta.setDisplayName(RPGInventory.getLanguage().getMessage("mypet.egg", event.getMyPet().getPetName()));
        petItem.setItemMeta(meta);
        petItem = ItemUtils.setTag(petItem, MYPET_TAG, event.getMyPet().getUUID().toString());

        Inventory inventory = InventoryManager.get(player).getInventory();
        Slot petSlot = getMyPetSlot();

        ItemStack currentPet = inventory.getItem(petSlot.getSlotId());
        boolean hasPet = !petSlot.isCup(currentPet);
        inventory.setItem(petSlot.getSlotId(), petItem);

        if (hasPet) {
            player.getInventory().addItem(currentPet);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onMyPetCall(MyPetCallEvent event) {
        Player player = event.getOwner().getPlayer();
        if (!InventoryManager.playerIsLoaded(player)) {
            return;
        }

        Inventory inventory = InventoryManager.get(player).getInventory();
        ItemStack currentPet = inventory.getItem(getMyPetSlot().getSlotId());
        boolean keepPet = true;

        if (!isMyPetItem(currentPet)) {
            keepPet = false;
        } else {
            UUID petUUID = UUID.fromString(ItemUtils.getTag(currentPet, MYPET_TAG));
            if (!petUUID.equals(event.getMyPet().getUUID())) {
                keepPet = false;
            }
        }

        if (!keepPet) {
            event.setCancelled(true);
            MyPetApi.getMyPetManager().deactivateMyPet(event.getOwner(), true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onMyPetRemove(MyPetRemoveEvent event) {
        Player player = event.getOwner().getPlayer();
        if (!InventoryManager.playerIsLoaded(player)) {
            return;
        }

        Inventory inventory = InventoryManager.get(player).getInventory();
        Slot petSlot = getMyPetSlot();
        ItemStack currentPetItem = inventory.getItem(petSlot.getSlotId());

        if (isMyPetItem(currentPetItem)) {
            UUID petUUID = UUID.fromString(ItemUtils.getTag(currentPetItem, MYPET_TAG));
            if (petUUID.equals(event.getMyPet().getUUID())) {
                inventory.setItem(petSlot.getSlotId(), petSlot.getCup());
            }
        }
    }

    @EventHandler
    public void onMyPetItemUse(PlayerInteractEvent event) {
        if (event.getItem() != null) {
            Player player = event.getPlayer();

            if (!InventoryManager.playerIsLoaded(player)) {
                return;
            }

            Inventory inventory = InventoryManager.get(player).getInventory();

            if (isMyPetItem(event.getItem())
                    && (event.getAction() == Action.RIGHT_CLICK_BLOCK
                    || event.getAction() == Action.RIGHT_CLICK_AIR)) {
                Slot petSlot = getMyPetSlot();

                ItemStack currentPet = inventory.getItem(petSlot.getSlotId());
                boolean hasPet = !petSlot.isCup(currentPet);
                ItemStack newPet = event.getItem();

                if (!hasPet) {
                    currentPet = null;
                }

                player.getEquipment().setItemInMainHand(currentPet);
                inventory.setItem(petSlot.getSlotId(), newPet);

                swapMyPets(player, hasPet, newPet);
            }
        }
    }

    @Nullable
    private static Slot getMyPetSlot() {
        for (Slot slot : SlotManager.getSlotManager().getSlots()) {
            if (slot.getSlotType() == Slot.SlotType.MYPET) {
                return slot;
            }
        }

        return null;
    }

    public static boolean validatePet(Player player, InventoryAction action, @Nullable ItemStack currentItem, @NotNull ItemStack cursor) {
        ActionType actionType = ActionType.getTypeOfAction(action);
        boolean isAllowedAction = actionType == ActionType.GET ||
                action == InventoryAction.SWAP_WITH_CURSOR ||
                actionType == ActionType.DROP;

        return !(!ItemUtils.isEmpty(currentItem) && isAllowedAction)
                || swapMyPets(player, isMyPetItem(currentItem), cursor);
    }

    private static boolean swapMyPets(final Player player, boolean hasPet, @NotNull ItemStack newPet) {
        if (hasPet) {
            PetUnequipEvent event = new PetUnequipEvent(player);
            RPGInventory.getInstance().getServer().getPluginManager().callEvent(event);

            deactivateMyPet(player);
        }

        if (!isMyPetItem(newPet)) {
            return true;
        }

        PetEquipEvent event = new PetEquipEvent(player, newPet);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return false;
        }

        final UUID petUUID = UUID.fromString(ItemUtils.getTag(newPet, MYPET_TAG));
        new BukkitRunnable() {
            @Override
            public void run() {
                activateMyPet(player, petUUID);
            }
        }.runTaskLater(RPGInventory.getInstance(), 1);

        return true;
    }

    private static boolean isMyPetItem(ItemStack item) {
        return !ItemUtils.isEmpty(item) && ItemUtils.hasTag(item, MYPET_TAG);
    }

    private static void deactivateMyPet(final Player player) {
        if (!MyPetApi.getPlayerManager().isMyPetPlayer(player)) {
            return;
        }

        final MyPetPlayer user = MyPetApi.getPlayerManager().getMyPetPlayer(player);

        if (user.hasMyPet()) {
            MyPetApi.getMyPetManager().deactivateMyPet(user, true);
            user.setMyPetForWorldGroup(WorldGroup.getGroupByWorld(player.getWorld().getName()).getName(), null);
            MyPetApi.getRepository().updateMyPetPlayer(user, null);
        }

    }

    private static void activateMyPet(final Player player, UUID petUUID) {
        final MyPetPlayer user;
        if (MyPetApi.getPlayerManager().isMyPetPlayer(player)) {
            user = MyPetApi.getPlayerManager().getMyPetPlayer(player);
        } else {
            user = MyPetApi.getPlayerManager().createMyPetPlayer(player);
        }

        if (user.hasMyPet()) {
            MyPetApi.getMyPetManager().deactivateMyPet(user, true);
        }

        final WorldGroup wg = WorldGroup.getGroupByWorld(player.getWorld().getName());
        MyPetApi.getRepository().getMyPet(petUUID, new RepositoryCallback<StoredMyPet>() {
            @Override
            public void callback(StoredMyPet storedMyPet) {
                if (!storedMyPet.getWorldGroup().equals(wg.getName())) {
                    PlayerUtils.sendMessage(player, "This pet doesn't belong into this world.");
                    return;
                }

                boolean savePet = storedMyPet.getOwner().getInternalUUID().equals(user.getInternalUUID());
                storedMyPet.setOwner(user);
                String worldName = player.getWorld().getName();
                user.setMyPetForWorldGroup(WorldGroup.getGroupByWorld(worldName).getName(), storedMyPet.getUUID());
                Optional<MyPet> pet = MyPetApi.getMyPetManager().activateMyPet(storedMyPet);
                if (pet.isPresent()) {
                    MyPet myPet = pet.get();
                    if (myPet.getStatus() != MyPet.PetState.Dead) {
                        pet.get().setStatus(MyPet.PetState.Here);
                    }
                }

                if (savePet) {
                    MyPetApi.getRepository().updateMyPet(storedMyPet, null);
                }

                MyPetApi.getRepository().updateMyPetPlayer(user, null);
            }
        });
    }
}