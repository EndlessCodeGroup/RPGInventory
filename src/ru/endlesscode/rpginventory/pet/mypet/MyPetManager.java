package ru.endlesscode.rpginventory.pet.mypet;

import com.google.common.base.Optional;
import de.Keyle.MyPet.MyPetApi;
import de.Keyle.MyPet.api.WorldGroup;
import de.Keyle.MyPet.api.entity.MyPet;
import de.Keyle.MyPet.api.entity.StoredMyPet;
import de.Keyle.MyPet.api.event.MyPetCallEvent;
import de.Keyle.MyPet.api.event.MyPetCreateEvent;
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
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.endlesscode.rpginventory.RPGInventory;
import ru.endlesscode.rpginventory.event.PetEquipEvent;
import ru.endlesscode.rpginventory.event.PetUnequipEvent;
import ru.endlesscode.rpginventory.inventory.ActionType;
import ru.endlesscode.rpginventory.inventory.InventoryManager;
import ru.endlesscode.rpginventory.inventory.slot.Slot;
import ru.endlesscode.rpginventory.inventory.slot.SlotManager;
import ru.endlesscode.rpginventory.utils.ItemUtils;

import java.util.UUID;

/**
 * Created by Keyle on 21.05.2016
 * It is part of the RPGInventory.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */

public class MyPetManager implements Listener {
    private static final String MYPET_TAG = "mypet.uuid";

    public static boolean validatePet(Player player, InventoryAction action, @Nullable ItemStack currentItem, @NotNull ItemStack cursor) {
        ActionType actionType = ActionType.getTypeOfAction(action);

        return !(!ItemUtils.isEmpty(currentItem) && (actionType == ActionType.GET || action == InventoryAction.SWAP_WITH_CURSOR || actionType == ActionType.DROP))
                || swapMyPets(player, isMyPetItem(currentItem), cursor);
    }

    public static Slot getMyPetSlot() {
        for (Slot slot : SlotManager.getSlotManager().getSlots()) {
            if (slot.getSlotType() == Slot.SlotType.MYPET) {
                return slot;
            }
        }

        return null;
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
                    player.sendMessage("This pet doesn't belong into this world.");
                    return;
                }

                boolean savePet = storedMyPet.getOwner().getInternalUUID().equals(user.getInternalUUID());
                storedMyPet.setOwner(user);
                user.setMyPetForWorldGroup(WorldGroup.getGroupByWorld(player.getWorld().getName()).getName(), storedMyPet.getUUID());
                Optional<MyPet> pet = MyPetApi.getMyPetManager().activateMyPet(storedMyPet);
                if (pet.isPresent() && pet.get().wantsToRespawn()) {
                    pet.get().createEntity();
                }

                if (savePet) {
                    MyPetApi.getRepository().updateMyPet(storedMyPet, null);
                }

                MyPetApi.getRepository().updateMyPetPlayer(user, null);
            }
        });
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

    public static void init() {
        for (MyPetPlayer mpPlayer : MyPetApi.getPlayerManager().getMyPetPlayers()) {
            syncPlayer(mpPlayer);
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

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();

        new BukkitRunnable() {
            @Override
            public void run() {
                PlayerManager playerManager = MyPetApi.getPlayerManager();
                if (playerManager.isMyPetPlayer(player)) {
                    syncPlayer(playerManager.getMyPetPlayer(player));
                }
            }
        }.runTaskLater(RPGInventory.getInstance(), 2);
    }

    @EventHandler
    public void onMyPetCreate(MyPetCreateEvent event) {
        Player player = event.getOwner().getPlayer();
        if (!InventoryManager.playerIsLoaded(player)) {
            return;
        }

        ItemStack petItem = new ItemStack(Material.MONSTER_EGG);
        ItemMeta meta = petItem.getItemMeta();
        meta.setDisplayName("MyPet Egg: " + event.getMyPet().getPetName());
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

    @EventHandler
    public void onMyPetCall(MyPetCallEvent event) {
        Player player = event.getOwner().getPlayer();
        if (!InventoryManager.playerIsLoaded(player)) {
            return;
        }

        Inventory inventory = InventoryManager.get(player).getInventory();
        Slot petSlot = getMyPetSlot();

        ItemStack currentPet = inventory.getItem(petSlot.getSlotId());
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

    @EventHandler
    public void onMyPetItemUse(PlayerInteractEvent event) {
        if (event.getItem() != null) {
            Player player = event.getPlayer();

            if (!InventoryManager.playerIsLoaded(player)) {
                return;
            }

            Inventory inventory = InventoryManager.get(player).getInventory();

            if (isMyPetItem(event.getItem()) && (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR)) {
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
}