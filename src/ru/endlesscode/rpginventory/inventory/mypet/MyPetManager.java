package ru.endlesscode.rpginventory.inventory.mypet;

import com.google.common.base.Optional;
import de.Keyle.MyPet.MyPetApi;
import de.Keyle.MyPet.api.WorldGroup;
import de.Keyle.MyPet.api.entity.MyPet;
import de.Keyle.MyPet.api.entity.StoredMyPet;
import de.Keyle.MyPet.api.event.MyPetCallEvent;
import de.Keyle.MyPet.api.event.MyPetCreateEvent;
import de.Keyle.MyPet.api.player.MyPetPlayer;
import de.Keyle.MyPet.api.repository.RepositoryCallback;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
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
import ru.endlesscode.rpginventory.inventory.InventoryManager;
import ru.endlesscode.rpginventory.inventory.slot.Slot;
import ru.endlesscode.rpginventory.inventory.slot.SlotManager;
import ru.endlesscode.rpginventory.nms.VersionHandler;
import ru.endlesscode.rpginventory.utils.ItemUtils;

import java.util.UUID;

/**
 * Created by Keyle on 21.05.2016
 * It is part of the RPGInventory.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */

public class MyPetManager implements Listener {
    private static final String MYPET_TAG = "mypet.uuid";

    public static boolean validatePet(Player player, @Nullable ItemStack currentItem, @NotNull ItemStack cursor) {
        return isMyPetItem(cursor) && swapMyPets(player, isMyPetItem(currentItem), cursor);
    }

    private static Slot getMyPetSlot() {
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

        if (newPet != null) {
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
        }

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
        Slot petSlot = getMyPetSlot();

        for (MyPetPlayer p : MyPetApi.getPlayerManager().getMyPetPlayers()) {
            if (p.isOnline() && p.hasMyPet()) {
                Player player = p.getPlayer();
                if (!InventoryManager.playerIsLoaded(player)) {
                    return;
                }
                Inventory inventory = InventoryManager.get(player).getInventory();
                ItemStack currentPet = inventory.getItem(petSlot.getSlotId());
                if (isMyPetItem(currentPet)) {
                    MyPet pet = p.getMyPet();
                    UUID petUUID = UUID.fromString(ItemUtils.getTag(currentPet, MYPET_TAG));
                    if (petUUID.equals(pet.getUUID())) {
                        continue;
                    }
                }
                MyPetApi.getMyPetManager().deactivateMyPet(p, false);
            }
        }
    }

    @EventHandler
    public void onMyPetLeash(MyPetCreateEvent event) {
        ItemStack petItem = new ItemStack(Material.MONSTER_EGG);
        ItemMeta meta = petItem.getItemMeta();
        meta.setDisplayName("MyPet Egg: " + event.getMyPet().getPetName());
        petItem.setItemMeta(meta);
        petItem = ItemUtils.setTag(petItem, MYPET_TAG, event.getMyPet().getUUID().toString());

        Player player = event.getOwner().getPlayer();
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

                if (VersionHandler.isHigher1_9()) {
                    player.getEquipment().setItemInMainHand(currentPet);
                } else {
                    //noinspection deprecation
                    player.setItemInHand(currentPet);
                }
                inventory.setItem(petSlot.getSlotId(), newPet);

                swapMyPets(player, hasPet, newPet);
            }
        }
    }
}