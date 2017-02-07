package ru.endlesscode.rpginventory.inventory;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.endlesscode.rpginventory.RPGInventory;
import ru.endlesscode.rpginventory.api.InventoryAPI;
import ru.endlesscode.rpginventory.event.PetEquipEvent;
import ru.endlesscode.rpginventory.event.PetUnequipEvent;
import ru.endlesscode.rpginventory.event.PlayerInventoryLoadEvent;
import ru.endlesscode.rpginventory.event.PlayerInventoryUnloadEvent;
import ru.endlesscode.rpginventory.event.listener.InventoryListener;
import ru.endlesscode.rpginventory.inventory.slot.Slot;
import ru.endlesscode.rpginventory.inventory.slot.SlotManager;
import ru.endlesscode.rpginventory.item.ItemManager;
import ru.endlesscode.rpginventory.misc.Config;
import ru.endlesscode.rpginventory.pet.PetManager;
import ru.endlesscode.rpginventory.pet.PetType;
import ru.endlesscode.rpginventory.utils.*;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by OsipXD on 18.08.2015.
 * It is part of the RpgInventory.
 * Copyright © 2015 «EndlessCode Group»
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
public class InventoryManager {
    static final String TITLE = RPGInventory.getLanguage().getCaption("title");
    private static final Map<UUID, PlayerWrapper> INVENTORIES = new HashMap<>();

    private static ItemStack fillSlot = null;

    private InventoryManager() {
    }

    public static boolean init(RPGInventory instance) {
        try {
            // Setup alternate view
            fillSlot = ItemUtils.getTexturedItem(Config.getConfig().getString("resource-pack.fill"));
            ItemMeta meta = fillSlot.getItemMeta();
            meta.setDisplayName(" ");
            fillSlot.setItemMeta(meta);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        // Register events
        instance.getServer().getPluginManager().registerEvents(new InventoryListener(), instance);
        return true;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean validateUpdate(Player player, ActionType actionType, @NotNull Slot slot, ItemStack item) {
        return actionType == ActionType.GET || actionType == ActionType.DROP
                || actionType == ActionType.SET && slot.isValidItem(item) && ItemManager.allowedForPlayer(player, item, true);
    }

    public static ItemStack getFillSlot() {
        return fillSlot;
    }

    public static boolean validatePet(Player player, InventoryAction action, @Nullable ItemStack currentItem, @NotNull ItemStack cursor) {
        ActionType actionType = ActionType.getTypeOfAction(action);

        if (!ItemUtils.isEmpty(currentItem)
                && (actionType == ActionType.GET || action == InventoryAction.SWAP_WITH_CURSOR || actionType == ActionType.DROP)
                && PetManager.getCooldown(currentItem) > 0) {
            return false;
        }

        if (actionType == ActionType.SET) {
            if (PetType.isPetItem(cursor) && ItemManager.allowedForPlayer(player, cursor, true)) {
                PetEquipEvent event = new PetEquipEvent(player, cursor);
                RPGInventory.getInstance().getServer().getPluginManager().callEvent(event);

                if (event.isCancelled()) {
                    return false;
                }

                PetManager.spawnPet(event.getPlayer(), event.getPetItem());
                return true;
            }
        } else if (actionType == ActionType.GET || actionType == ActionType.DROP) {
            PetUnequipEvent event = new PetUnequipEvent(player);
            RPGInventory.getInstance().getServer().getPluginManager().callEvent(event);
            PetManager.despawnPet(event.getPlayer());
            return true;
        }

        return false;
    }

    public static boolean validateArmor(Player player, InventoryAction action, @NotNull Slot slot, ItemStack item) {
        ActionType actionType = ActionType.getTypeOfAction(action);
        return actionType != ActionType.OTHER && (actionType != ActionType.SET || slot.isValidItem(item))
                && ItemManager.allowedForPlayer(player, item, true);
    }

    public static void updateShieldSlot(@NotNull Player player, @NotNull Inventory inventory, @NotNull Slot slot, int slotId,
                                        InventoryType.SlotType slotType, InventoryAction action,
                                        ItemStack currentItem, ItemStack cursor) {
        ActionType actionType = ActionType.getTypeOfAction(action);
        if (actionType == ActionType.GET) {
            if (slot.isCup(currentItem)) {
                return;
            }

            if (slotType == InventoryType.SlotType.QUICKBAR && InventoryAPI.isRPGInventory(inventory)) {
                inventory.setItem(slot.getSlotId(), slot.getCup());
            } else {
                player.getEquipment().setItemInOffHand(new ItemStack(Material.AIR));
            }
        } else if (actionType == ActionType.SET) {
            if (slot.isCup(currentItem)) {
                currentItem = null;
                action = InventoryAction.PLACE_ALL;
            }

            if (slotType == InventoryType.SlotType.QUICKBAR && InventoryAPI.isRPGInventory(inventory)) {
                inventory.setItem(slot.getSlotId(), cursor);
            } else {
                player.getEquipment().setItemInOffHand(cursor);
            }
        }

        InventoryManager.updateInventory(player, inventory, slotId, slotType, action, currentItem, cursor);
    }

    public static void updateQuickSlot(@NotNull Player player, @NotNull Inventory inventory, @NotNull Slot slot, int slotId,
                                       InventoryType.SlotType slotType, InventoryAction action,
                                       ItemStack currentItem, ItemStack cursor) {
        ActionType actionType = ActionType.getTypeOfAction(action);
        if (actionType == ActionType.GET) {
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
        } else if (actionType == ActionType.SET) {
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
        ActionType actionType = ActionType.getTypeOfAction(action);

        // Equip armor
        if (actionType == ActionType.SET || action == InventoryAction.UNKNOWN) {
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
        } else if (actionType == ActionType.GET || actionType == ActionType.DROP) { // Unequip armor
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

    public static void syncArmor(PlayerWrapper playerWrapper) {
        Player player = (Player) playerWrapper.getPlayer();
        Inventory inventory = playerWrapper.getInventory();
        SlotManager sm = SlotManager.getSlotManager();
        if (ArmorType.HELMET.getSlot() != -1) {
            ItemStack helmet = player.getEquipment().getHelmet();
            Slot helmetSlot = sm.getSlot(ArmorType.HELMET.getSlot(), InventoryType.SlotType.CONTAINER);
            inventory.setItem(ArmorType.HELMET.getSlot(), (ItemUtils.isEmpty(helmet))
                    && helmetSlot != null ? helmetSlot.getCup() : helmet);
        }

        if (ArmorType.CHESTPLATE.getSlot() != -1) {
            ItemStack savedChestplate = InventoryManager.get(player).getSavedChestplate();
            ItemStack chestplate = savedChestplate == null ? player.getEquipment().getChestplate() : savedChestplate;
            Slot chestplateSlot = sm.getSlot(ArmorType.CHESTPLATE.getSlot(), InventoryType.SlotType.CONTAINER);
            inventory.setItem(ArmorType.CHESTPLATE.getSlot(), (ItemUtils.isEmpty(chestplate))
                    && chestplateSlot != null ? chestplateSlot.getCup() : chestplate);
        }

        if (ArmorType.LEGGINGS.getSlot() != -1) {
            ItemStack leggings = player.getEquipment().getLeggings();
            Slot leggingsSlot = sm.getSlot(ArmorType.LEGGINGS.getSlot(), InventoryType.SlotType.CONTAINER);
            inventory.setItem(ArmorType.LEGGINGS.getSlot(), (ItemUtils.isEmpty(leggings))
                    && leggingsSlot != null ? leggingsSlot.getCup() : leggings);
        }

        if (ArmorType.BOOTS.getSlot() != -1) {
            ItemStack boots = player.getEquipment().getBoots();
            Slot bootsSlot = sm.getSlot(ArmorType.BOOTS.getSlot(), InventoryType.SlotType.CONTAINER);
            inventory.setItem(ArmorType.BOOTS.getSlot(), (ItemUtils.isEmpty(boots))
                    && bootsSlot != null ? bootsSlot.getCup() : boots);
        }
    }

    public static void syncQuickSlots(PlayerWrapper playerWrapper) {
        Player player = (Player) playerWrapper.getPlayer();
        for (Slot quickSlot : SlotManager.getSlotManager().getQuickSlots()) {
            playerWrapper.getInventory().setItem(quickSlot.getSlotId(), player.getInventory().getItem(quickSlot.getQuickSlot()));
        }
    }

    public static void syncInfoSlots(PlayerWrapper playerWrapper) {
        final Player player = (Player) playerWrapper.getPlayer();
        for (Slot infoSlot : SlotManager.getSlotManager().getInfoSlots()) {
            ItemStack cup = infoSlot.getCup();
            ItemMeta meta = cup.getItemMeta();
            List<String> lore = meta.getLore();

            for (int i = 0; i < lore.size(); i++) {
                String line = lore.get(i);
                lore.set(i, StringUtils.setPlaceholders(player, line));
            }

            meta.setLore(lore);
            cup.setItemMeta(meta);
            playerWrapper.getInventory().setItem(infoSlot.getSlotId(), cup);
        }

        player.updateInventory();
    }

    public static void syncShieldSlot(PlayerWrapper playerWrapper) {
        Slot slot = SlotManager.getSlotManager().getShieldSlot();
        if (slot == null) {
            return;
        }

        Player player = (Player) playerWrapper.getPlayer();
        ItemStack itemInHand = player.getEquipment().getItemInOffHand();
        playerWrapper.getInventory().setItem(slot.getSlotId(), ItemUtils.isEmpty(itemInHand) ? slot.getCup() : itemInHand);
    }

    private static void updateInventory(@NotNull Player player, @NotNull Inventory inventory, int slot, InventoryAction action, ItemStack currentItem, @NotNull ItemStack cursor) {
        InventoryManager.updateInventory(player, inventory, slot, InventoryType.SlotType.CONTAINER, action, currentItem, cursor);
    }

    private static void updateInventory(@NotNull Player player, @NotNull Inventory inventory, int slot, InventoryType.SlotType slotType, InventoryAction action, ItemStack currentItem, ItemStack cursorItem) {
        if (ActionType.getTypeOfAction(action) == ActionType.DROP) {
            return;
        }

        if (action == InventoryAction.PLACE_ALL) {
            if (ItemUtils.isEmpty(currentItem)) {
                currentItem = cursorItem.clone();
            } else {
                currentItem.setAmount(currentItem.getAmount() + cursorItem.getAmount());
            }

            cursorItem = null;
        } else if (action == InventoryAction.PLACE_ONE) {
            if (ItemUtils.isEmpty(currentItem)) {
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
            if (slot < 9) { // Exclude shield
                player.getInventory().setItem(slot, currentItem);
            }
        } else {
            inventory.setItem(slot, currentItem);
        }

        player.setItemOnCursor(cursorItem);
        player.updateInventory();
    }

    static void lockEmptySlots(@NotNull Player player) {
        lockEmptySlots(INVENTORIES.get(player.getUniqueId()).getInventory());
    }

    @SuppressWarnings("WeakerAccess")
    public static void lockEmptySlots(@NotNull Inventory inventory) {
        for (int i = 0; i < inventory.getSize(); i++) {
            Slot slot = SlotManager.getSlotManager().getSlot(i, InventoryType.SlotType.CONTAINER);
            if (slot == null) {
                inventory.setItem(i, fillSlot);
            } else if (ItemUtils.isEmpty(inventory.getItem(i))) {
                inventory.setItem(i, slot.getCup());
            }
        }
    }

    static void unlockEmptySlots(@NotNull Player player) {
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

    static void lockQuickSlots(@NotNull Player player) {
        for (Slot quickSlot : SlotManager.getSlotManager().getQuickSlots()) {
            int slotId = quickSlot.getQuickSlot();

            if (ItemUtils.isEmpty(player.getInventory().getItem(slotId))) {
                player.getInventory().setItem(slotId, quickSlot.getCup());
            }

            if (player.getInventory().getHeldItemSlot() == slotId) {
                if (quickSlot.isCup(player.getInventory().getItem(slotId))) {
                    InventoryUtils.heldFreeSlot(player, slotId, InventoryUtils.SearchType.NEXT);
                }
            }
        }
    }

    static void unlockQuickSlots(@NotNull Player player) {
        for (Slot quickSlot : SlotManager.getSlotManager().getQuickSlots()) {
            int slotId = quickSlot.getQuickSlot();
            if (quickSlot.isCup(player.getInventory().getItem(slotId))) {
                player.getInventory().setItem(slotId, null);
            }
        }
    }

    private static void sendResourcePack(final Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                player.setResourcePack(Config.getConfig().getString("resource-pack.url"));
            }
        }.runTaskLater(RPGInventory.getInstance(), 20);
    }

    private static boolean isNewPlayer(Player player) {
        return !new File(RPGInventory.getInstance().getDataFolder(), "inventories/" + player.getUniqueId() + ".inv").exists();
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

            PlayerWrapper playerWrapper;
            if (file.exists()) {
                playerWrapper = InventorySerializer.loadPlayer(player, file);
            } else {
                playerWrapper = new PlayerWrapper(player);
                playerWrapper.setBuyedSlots(0);
            }

            PlayerInventoryLoadEvent.Pre event = new PlayerInventoryLoadEvent.Pre(player);
            RPGInventory.getInstance().getServer().getPluginManager().callEvent(event);

            if (event.isCancelled()) {
                return;
            }

            playerWrapper.startHealthUpdater();
            INVENTORIES.put(player.getUniqueId(), playerWrapper);
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

        INVENTORIES.get(player.getUniqueId()).onUnload();
        savePlayerInventory(player);
        InventoryLocker.unlockSlots(player);

        INVENTORIES.remove(player.getUniqueId());

        RPGInventory.getInstance().getServer().getPluginManager().callEvent(new PlayerInventoryUnloadEvent.Post(player));
    }

    public static void savePlayerInventory(@NotNull Player player) {
        if (!InventoryManager.playerIsLoaded(player)) {
            return;
        }

        PlayerWrapper playerWrapper = INVENTORIES.get(player.getUniqueId());
        try {
            File folder = new File(RPGInventory.getInstance().getDataFolder(), "inventories");
            if (!folder.exists() && !folder.mkdir()) {
                throw new IOException("Failed to create directory: " + folder.getName());
            }

            File file = new File(folder, player.getUniqueId() + ".inv");
            if (file.exists() && !file.delete()) {
                throw new IOException("Failed to delete file: " + file.getName());
            }

            InventorySerializer.savePlayer(player, playerWrapper, file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Nullable
    public static PlayerWrapper get(@NotNull OfflinePlayer player) {
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

    public static boolean isFilledSlot(ItemStack item) {
        return fillSlot.equals(item);
    }

    public static boolean isEmptySlot(ItemStack item) {
        for (Slot slot : SlotManager.getSlotManager().getSlots()) {
            if (slot.isCup(item)) {
                return true;
            }
        }

        return false;
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

    public static boolean buySlot(Player player, PlayerWrapper playerWrapper, Slot slot) {
        double cost = slot.getCost();

        if (!playerWrapper.isPreparedToBuy(slot)) {
            PlayerUtils.sendMessage(player, RPGInventory.getLanguage().getCaption("error.buyable", slot.getCost()));
            playerWrapper.prepareToBuy(slot);
            return false;
        }

        if (!PlayerUtils.checkMoney(player, cost) || !RPGInventory.getEconomy().withdrawPlayer(player, cost).transactionSuccess()) {
            return false;
        }

        playerWrapper.setBuyedSlots(slot.getName());
        PlayerUtils.sendMessage(player, RPGInventory.getLanguage().getCaption("message.buyed"));

        return true;
    }

    public static void initPlayer(final Player player, boolean skipJoinMessage) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 1));

        if (InventoryManager.isNewPlayer(player)) {
            if (Config.getConfig().getBoolean("join-messages.rp-info.enabled", true)) {
                Runnable callback = new Runnable() {
                    @Override
                    public void run() {
                        InventoryManager.sendResourcePack(player);
                    }
                };

                EffectUtils.sendTitle(player,
                        Config.getConfig().getInt("join-messages.delay"),
                        Config.getConfig().getString("join-messages.rp-info.title"),
                        Config.getConfig().getStringList("join-messages.rp-info.text"), callback);
            } else {
                InventoryManager.sendResourcePack(player);
            }
        } else {
            if (Config.getConfig().getBoolean("join-messages.default.enabled", true)) {
                if (!skipJoinMessage) {
                    EffectUtils.sendTitle(player,
                            Config.getConfig().getInt("join-messages.delay"),
                            Config.getConfig().getString("join-messages.default.title"),
                            Config.getConfig().getStringList("join-messages.default.text"), null);
                }
            }

            InventoryManager.sendResourcePack(player);
        }

        if (RPGInventory.getPermissions().has(player, "rpginventory.admin")) {
            RPGInventory.getInstance().checkUpdates(player);
        }
    }

    private enum ListType {
        BLACKLIST, WHITELIST
    }
}