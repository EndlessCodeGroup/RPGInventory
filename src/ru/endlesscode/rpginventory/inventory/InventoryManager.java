package ru.endlesscode.rpginventory.inventory;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.endlesscode.rpginventory.RPGInventory;
import ru.endlesscode.rpginventory.api.InventoryAPI;
import ru.endlesscode.rpginventory.event.PetEquipEvent;
import ru.endlesscode.rpginventory.event.PetUnequipEvent;
import ru.endlesscode.rpginventory.event.PlayerInventoryLoadEvent;
import ru.endlesscode.rpginventory.event.PlayerInventoryUnloadEvent;
import ru.endlesscode.rpginventory.inventory.slot.Slot;
import ru.endlesscode.rpginventory.inventory.slot.SlotManager;
import ru.endlesscode.rpginventory.item.ItemManager;
import ru.endlesscode.rpginventory.misc.Config;
import ru.endlesscode.rpginventory.pet.PetManager;
import ru.endlesscode.rpginventory.pet.PetType;
import ru.endlesscode.rpginventory.utils.InventoryUtils;
import ru.endlesscode.rpginventory.utils.ItemUtils;
import ru.endlesscode.rpginventory.utils.PlayerUtils;
import ru.endlesscode.rpginventory.utils.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

/**
 * Created by OsipXD on 18.08.2015.
 * It is part of the RpgInventory.
 * Copyright © 2015 «EndlessCode Group»
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
public class InventoryManager {
    public static final String TITLE = RPGInventory.getLanguage().getCaption("title");
    public static final int OPEN_ITEM_SLOT = Config.getConfig().getInt("alternate-view.slot") % 9;

    private static final Map<UUID, InventoryWrapper> INVENTORIES = new HashMap<>();

    private static ItemStack fillSlot = null;
    private static ItemStack inventoryOpenItem = null;

    private InventoryManager() {
    }

    public static void init() {
        // Setup alternate view
        fillSlot = ItemUtils.getTexturedItem(Config.getConfig().getString("alternate-view.fill"));
        ItemMeta meta = fillSlot.getItemMeta();
        meta.setDisplayName(" ");
        fillSlot.setItemMeta(meta);

        inventoryOpenItem = ItemUtils.getTexturedItem(Config.getConfig().getString("alternate-view.item"));
        meta.setDisplayName(StringUtils.coloredLine(Config.getConfig().getString("alternate-view.name")));
        meta.setLore(Collections.singletonList(StringUtils.coloredLine(Config.getConfig().getString("alternate-view.lore"))));
        inventoryOpenItem.setItemMeta(meta);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean validateUpdate(Player player, InventoryUtils.ActionType actionType, @NotNull Slot slot, ItemStack item) {
        return actionType == InventoryUtils.ActionType.GET || actionType == InventoryUtils.ActionType.DROP
                || actionType == InventoryUtils.ActionType.SET && ItemManager.allowedForPlayer(player, item, true) && slot.isValidItem(item);
    }

    public static boolean validatePet(Player player, InventoryAction action, @Nullable ItemStack currentItem, @NotNull ItemStack cursor) {
        InventoryUtils.ActionType actionType = InventoryUtils.getTypeOfAction(action);

        if (currentItem != null && currentItem.getType() != Material.AIR
                && (actionType == InventoryUtils.ActionType.GET || action == InventoryAction.SWAP_WITH_CURSOR || actionType == InventoryUtils.ActionType.DROP)
                && PetManager.getCooldown(currentItem) > 0) {
            return false;
        }

        if (actionType == InventoryUtils.ActionType.SET) {
            if (PetType.isPetItem(cursor)) {
                PetEquipEvent event = new PetEquipEvent(player, cursor);
                RPGInventory.getInstance().getServer().getPluginManager().callEvent(event);

                if (event.isCancelled()) {
                    return false;
                }

                PetManager.spawnPet(event.getPlayer(), event.getPetItem());
                return true;
            }
        } else if (actionType == InventoryUtils.ActionType.GET || actionType == InventoryUtils.ActionType.DROP) {
            PetUnequipEvent event = new PetUnequipEvent(player);
            RPGInventory.getInstance().getServer().getPluginManager().callEvent(event);
            PetManager.despawnPet(event.getPlayer());
            return true;
        }

        return false;
    }

    public static boolean validateArmor(InventoryAction action, @NotNull Slot slot, ItemStack cursor) {
        InventoryUtils.ActionType actionType = InventoryUtils.getTypeOfAction(action);

        return !(actionType == InventoryUtils.ActionType.SET || action == InventoryAction.UNKNOWN) || slot.isValidItem(cursor);
    }

    public static void updateQuickSlot(@NotNull Player player, @NotNull Inventory inventory, @NotNull Slot slot, int slotId, InventoryType.SlotType slotType, InventoryAction action, ItemStack currentItem, ItemStack cursor) {
        InventoryUtils.ActionType actionType = InventoryUtils.getTypeOfAction(action);
        if (actionType == InventoryUtils.ActionType.GET) {
            if (slot.isCup(currentItem)) {
                return;
            }

            if (player.getInventory().getHeldItemSlot() == slot.getQuickSlot()) {
                InventoryUtils.heldFreeSlot(player, slot.getQuickSlot(), InventoryUtils.SearchType.NEXT);
            }

            if (slotType == InventoryType.SlotType.QUICKBAR && InventoryAPI.isRPGInventory(inventory)) {
                inventory.setItem(slot.getSlotId(), slot.getCup());
            } else {
                player.getInventory().setItem(slot.getQuickSlot(), slot.getCup());
            }

            action = InventoryAction.SWAP_WITH_CURSOR;
            cursor = slot.getCup();
        } else if (actionType == InventoryUtils.ActionType.SET) {
            if (slot.isCup(currentItem)) {
                currentItem = null;
                action = InventoryAction.PLACE_ALL;
            }

            if (slotType == InventoryType.SlotType.QUICKBAR && InventoryAPI.isRPGInventory(inventory)) {
                inventory.setItem(slot.getSlotId(), cursor);
            } else {
                player.getInventory().setItem(slot.getQuickSlot(), cursor);
            }
        }

        InventoryManager.updateInventory(player, inventory, slotId, slotType, action, currentItem, cursor);
    }

    public static void updateArmor(@NotNull Player player, @NotNull Inventory inventory, @NotNull Slot slot, int slotId, InventoryAction action, ItemStack currentItem, @NotNull ItemStack cursor) {
        InventoryUtils.ActionType actionType = InventoryUtils.getTypeOfAction(action);

        // Equip armor
        if (actionType == InventoryUtils.ActionType.SET || action == InventoryAction.UNKNOWN) {
            switch (slot.getName()) {
                case "helmet":
                    InventoryManager.updateInventory(player, inventory, slotId, action, currentItem, cursor);
                    player.getEquipment().setHelmet(inventory.getItem(slotId));
                    break;
                case "chestplate":
                    InventoryManager.updateInventory(player, inventory, slotId, action, currentItem, cursor);
                    player.getEquipment().setChestplate(inventory.getItem(slotId));
                    break;
                case "leggings":
                    InventoryManager.updateInventory(player, inventory, slotId, action, currentItem, cursor);
                    player.getEquipment().setLeggings(inventory.getItem(slotId));
                    break;
                case "boots":
                    InventoryManager.updateInventory(player, inventory, slotId, action, currentItem, cursor);
                    player.getEquipment().setBoots(inventory.getItem(slotId));
                    break;
            }
        } else if (actionType == InventoryUtils.ActionType.GET || actionType == InventoryUtils.ActionType.DROP) { // Unequip armor
            InventoryManager.updateInventory(player, inventory, slotId, action, currentItem, cursor);

            switch (slot.getName()) {
                case "helmet":
                    player.getEquipment().setHelmet(null);
                    break;
                case "chestplate":
                    player.getEquipment().setChestplate(null);
                    break;
                case "leggings":
                    player.getEquipment().setLeggings(null);
                    break;
                case "boots":
                    player.getEquipment().setBoots(null);
                    break;
            }
        }
    }

    public static void syncArmor(@NotNull HumanEntity player, @NotNull Inventory inventory) {
        SlotManager sm = SlotManager.getSlotManager();
        if (sm.getHelmetSlotId() != -1) {
            ItemStack helmet = player.getEquipment().getHelmet();
            Slot helmetSlot = sm.getSlot(sm.getHelmetSlotId(), InventoryType.SlotType.CONTAINER);
            inventory.setItem(sm.getHelmetSlotId(), (helmet == null || helmet.getType() == Material.AIR) && helmetSlot != null ? helmetSlot.getCup() : helmet);
        }

        if (sm.getChestplateSlotId() != -1) {
            ItemStack chestplate = player.getEquipment().getChestplate();
            Slot chestplateSlot = sm.getSlot(sm.getChestplateSlotId(), InventoryType.SlotType.CONTAINER);
            inventory.setItem(sm.getChestplateSlotId(), (chestplate == null || chestplate.getType() == Material.AIR) && chestplateSlot != null ? chestplateSlot.getCup() : chestplate);
        }

        if (sm.getLeggingsSlotId() != -1) {
            ItemStack leggings = player.getEquipment().getLeggings();
            Slot leggingsSlot = sm.getSlot(sm.getLeggingsSlotId(), InventoryType.SlotType.CONTAINER);
            inventory.setItem(sm.getLeggingsSlotId(), (leggings == null || leggings.getType() == Material.AIR) && leggingsSlot != null ? leggingsSlot.getCup() : leggings);
        }

        if (sm.getBootsSlotId() != -1) {
            ItemStack boots = player.getEquipment().getBoots();
            Slot bootsSlot = sm.getSlot(sm.getBootsSlotId(), InventoryType.SlotType.CONTAINER);
            inventory.setItem(sm.getBootsSlotId(), (boots == null || boots.getType() == Material.AIR) && bootsSlot != null ? bootsSlot.getCup() : boots);
        }
    }

    public static void syncQuickSlots(@NotNull HumanEntity player, @NotNull Inventory inventory) {
        for (Slot quickSlot : SlotManager.getSlotManager().getQuickSlots()) {
            inventory.setItem(quickSlot.getSlotId(), player.getInventory().getItem(quickSlot.getQuickSlot()));
        }
    }

    private static void updateInventory(@NotNull Player player, @NotNull Inventory inventory, int slot, InventoryAction action, ItemStack currentItem, @NotNull ItemStack cursor) {
        InventoryManager.updateInventory(player, inventory, slot, InventoryType.SlotType.CONTAINER, action, currentItem, cursor);
    }

    @SuppressWarnings("deprecation")
    private static void updateInventory(@NotNull Player player, @NotNull Inventory inventory, int slot, InventoryType.SlotType slotType, InventoryAction action, ItemStack currentItem, ItemStack cursorItem) {
        if (InventoryUtils.getTypeOfAction(action) == InventoryUtils.ActionType.DROP) {
            return;
        }

        if (action == InventoryAction.PLACE_ALL) {
            if (currentItem == null || currentItem.getType() == Material.AIR) {
                currentItem = cursorItem.clone();
            } else {
                currentItem.setAmount(currentItem.getAmount() + cursorItem.getAmount());
            }

            cursorItem = null;
        } else if (action == InventoryAction.PLACE_ONE) {
            if (currentItem == null || currentItem.getType() == Material.AIR) {
                currentItem = cursorItem.clone();
                currentItem.setAmount(1);
                cursorItem.setAmount(cursorItem.getAmount() - 1);
            } else if (currentItem.getMaxStackSize() < currentItem.getAmount() + 1) {
                currentItem.setAmount(currentItem.getAmount() + 1);
                cursorItem.setAmount(cursorItem.getAmount() - 1);
            }
        } else if (action == InventoryAction.PLACE_SOME) {
            cursorItem.setAmount(currentItem.getMaxStackSize() - currentItem.getAmount());
            currentItem.setAmount(currentItem.getMaxStackSize());
        } else if (action == InventoryAction.SWAP_WITH_CURSOR) {
            ItemStack tempItem = cursorItem.clone();
            cursorItem = currentItem.clone();
            currentItem = tempItem;
        } else if (action == InventoryAction.PICKUP_ALL) {
            cursorItem = currentItem.clone();
            currentItem = null;
        } else if (action == InventoryAction.PICKUP_HALF) {
            ItemStack item = currentItem.clone();
            if (currentItem.getAmount() % 2 == 0) {
                item.setAmount(item.getAmount() / 2);
                currentItem = item.clone();
                cursorItem = item.clone();
            } else {
                currentItem = item.clone();
                currentItem.setAmount(item.getAmount() / 2);
                cursorItem = item.clone();
                cursorItem.setAmount(item.getAmount() / 2 + 1);
            }
        } else if (action == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
            player.getInventory().addItem(currentItem);
            currentItem = null;
        }

        if (slotType == InventoryType.SlotType.QUICKBAR) {
            player.getInventory().setItem(slot, currentItem);
        } else {
            inventory.setItem(slot, currentItem);
        }

        player.setItemOnCursor(cursorItem);
        player.updateInventory();
    }

    public static void lockEmptySlots(@NotNull Player player) {
        lockEmptySlots(player, INVENTORIES.get(player.getUniqueId()).getInventory());
    }

    @SuppressWarnings("WeakerAccess")
    public static void lockEmptySlots(@NotNull Player player, @NotNull Inventory inventory) {
        for (int i = 0; i < inventory.getSize(); i++) {
            Slot slot = SlotManager.getSlotManager().getSlot(i, InventoryType.SlotType.CONTAINER);
            if (slot == null) {
                if (!ResourcePackManager.isLoadedResourcePack(player)) {
                    inventory.setItem(i, fillSlot);
                }
            } else if (inventory.getItem(i) == null || inventory.getItem(i).getType() == Material.AIR) {
                inventory.setItem(i, slot.getCup());
            }
        }
    }

    public static void unlockEmptySlots(@NotNull Player player) {
        Inventory inventory = INVENTORIES.get(player.getUniqueId()).getInventory();

        for (int i = 0; i < inventory.getSize(); i++) {
            Slot slot = SlotManager.getSlotManager().getSlot(i, InventoryType.SlotType.CONTAINER);
            if (slot == null || slot.isCup(inventory.getItem(i))) {
                inventory.setItem(i, null);
            }
        }
    }

    public static boolean isQuickSlot(int slot) {
        return getQuickSlot(slot) != null;
    }

    @Nullable
    public static Slot getQuickSlot(int slot) {
        for (Slot quickSlot : SlotManager.getSlotManager().getQuickSlots()) {
            if (slot == quickSlot.getQuickSlot()) {
                return quickSlot;
            }
        }

        return null;
    }

    public static void lockQuickSlots(@NotNull Player player) {
        for (Slot quickSlot : SlotManager.getSlotManager().getQuickSlots()) {
            int slotId = quickSlot.getQuickSlot();
            if (player.getInventory().getItem(slotId) == null || player.getInventory().getItem(slotId).getType() == Material.AIR) {
                player.getInventory().setItem(slotId, quickSlot.getCup());
            }

            if (player.getInventory().getHeldItemSlot() == slotId) {
                if (quickSlot.isCup(player.getInventory().getItem(slotId))) {
                    InventoryUtils.heldFreeSlot(player, slotId, InventoryUtils.SearchType.NEXT);
                }
            }
        }
    }

    public static void unlockQuickSlots(@NotNull Player player) {
        for (Slot quickSlot : SlotManager.getSlotManager().getQuickSlots()) {
            int slotId = quickSlot.getQuickSlot();
            if (quickSlot.isCup(player.getInventory().getItem(slotId))) {
                player.getInventory().setItem(slotId, null);
            }
        }
    }

    public static void loadPlayerInventory(@NotNull Player player) {
        if (!InventoryManager.isAllowedWorld(player.getWorld())) {
            INVENTORIES.remove(player.getUniqueId());
            return;
        }

        try {
            File folder = new File(RPGInventory.getInstance().getDataFolder(), "inventories");
            if (!folder.exists()) {
                folder.mkdir();
            }

            // Load inventory from file
            File file = new File(folder, player.getUniqueId() + ".inv");
            if (new File(folder, player.getName() + ".inv").exists()) {
                Files.move(new File(folder, player.getName() + ".inv").toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

            InventoryWrapper inventoryWrapper;
            if (file.exists()) {
                inventoryWrapper = InventorySerializer.loadPlayer(player, file);
            } else {
                inventoryWrapper = new InventoryWrapper(player);
                inventoryWrapper.setBuyedSlots(0);
                ResourcePackManager.wontResourcePack(player, ResourcePackManager.getMode() != ResourcePackManager.Mode.DISABLED);
            }

            PlayerInventoryLoadEvent.Pre event = new PlayerInventoryLoadEvent.Pre(player);
            RPGInventory.getInstance().getServer().getPluginManager().callEvent(event);

            if (event.isCancelled()) {
                return;
            }

            lockEmptySlots(player, inventoryWrapper.getInventory());
            INVENTORIES.put(player.getUniqueId(), inventoryWrapper);
        } catch (IOException e) {
            e.printStackTrace();
        }

        InventoryLocker.lockSlots(player);
        PetManager.initPlayer(player);

        RPGInventory.getInstance().getServer().getPluginManager().callEvent(new PlayerInventoryLoadEvent.Post(player));
    }

    public static void unloadPlayerInventory(@NotNull Player player) {
        if (!InventoryManager.playerIsLoaded(player)) {
            return;
        }

        player.closeInventory();
        savePlayerInventory(player);

        InventoryWrapper inventoryWrapper = INVENTORIES.get(player.getUniqueId());
        InventoryLocker.unlockSlots(player);
        inventoryWrapper.clearStats();

        if (PetManager.isEnabled()) {
            PetManager.despawnPet(player);
            Inventory inventory = inventoryWrapper.getInventory();
            ItemStack petItem = inventory.getItem(PetManager.getPetSlotId());
            if (petItem != null) {
                inventory.setItem(PetManager.getPetSlotId(), PetType.clone(petItem));
            }
        }

        ResourcePackManager.removePlayer(player);
        INVENTORIES.remove(player.getUniqueId());

        RPGInventory.getInstance().getServer().getPluginManager().callEvent(new PlayerInventoryUnloadEvent.Post(player));
    }

    public static void savePlayerInventory(@NotNull Player player) {
        if (!InventoryManager.playerIsLoaded(player)) {
            return;
        }

        InventoryWrapper inventoryWrapper = INVENTORIES.get(player.getUniqueId());
        try {
            File folder = new File(RPGInventory.getInstance().getDataFolder(), "inventories");
            if (!folder.exists() && !folder.mkdir()) {
                throw new IOException("Failed to create directory: " + folder.getName());
            }

            File file = new File(folder, player.getUniqueId() + ".inv");
            if (file.exists() && !file.delete()) {
                throw new IOException("Failed to delete file: " + file.getName());
            }

            InventorySerializer.savePlayer(player, inventoryWrapper, file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Nullable
    public static InventoryWrapper get(@NotNull OfflinePlayer player) {
        return INVENTORIES.get(player.getUniqueId());
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isQuickEmptySlot(ItemStack item) {
        for (Slot quickSlot : SlotManager.getSlotManager().getQuickSlots()) {
            if (quickSlot.isCup(item)) {
                return true;
            }
        }

        return false;
    }

    public static boolean isEmptySlot(ItemStack item) {
        for (Slot slot : SlotManager.getSlotManager().getSlots()) {
            if (slot.isCup(item)) {
                return true;
            }
        }

        return false;
    }

    public static boolean isInventoryOpenItem(ItemStack item) {
        return inventoryOpenItem != null && item != null && item.getType() != Material.AIR && inventoryOpenItem.equals(item);
    }

    public static ItemStack getInventoryOpenItem() {
        return inventoryOpenItem;
    }

    @Contract("null -> false")
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean playerIsLoaded(AnimalTamer player) {
        return player != null && INVENTORIES.containsKey(player.getUniqueId());
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isAllowedWorld(World world) {
        List<String> list = Config.getConfig().getStringList("worlds.list");

        switch (ListType.valueOf(Config.getConfig().getString("worlds.mode"))) {
            case BLACKLIST:
                return !list.contains(world.getName());
            case WHITELIST:
                return list.contains(world.getName());
        }

        return false;
    }

    public static boolean buySlot(Player player, InventoryWrapper inventoryWrapper, Slot slot) {
        double cost = slot.getCost();

        if (!inventoryWrapper.isPreparedToBuy()) {
            player.sendMessage(String.format(RPGInventory.getLanguage().getCaption("error.buyable"), slot.getCost()));
            inventoryWrapper.prepareToBuy();
            return false;
        }

        if (!PlayerUtils.checkMoney(player, cost) || !RPGInventory.getEconomy().withdrawPlayer(player, cost).transactionSuccess()) {
            return false;
        }

        inventoryWrapper.setBuyedSlots(slot.getName());
        player.sendMessage(RPGInventory.getLanguage().getCaption("message.buyed"));

        return true;
    }

    enum ListType {
        BLACKLIST, WHITELIST
    }
}