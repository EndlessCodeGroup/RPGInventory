package ru.endlesscode.rpginventory.event.listener;

import org.bukkit.*;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;
import ru.endlesscode.rpginventory.RPGInventory;
import ru.endlesscode.rpginventory.api.InventoryAPI;
import ru.endlesscode.rpginventory.event.PetUnequipEvent;
import ru.endlesscode.rpginventory.inventory.ActionType;
import ru.endlesscode.rpginventory.inventory.InventoryLocker;
import ru.endlesscode.rpginventory.inventory.InventoryManager;
import ru.endlesscode.rpginventory.inventory.PlayerWrapper;
import ru.endlesscode.rpginventory.inventory.backpack.BackpackManager;
import ru.endlesscode.rpginventory.pet.mypet.MyPetManager;
import ru.endlesscode.rpginventory.inventory.slot.ActionSlot;
import ru.endlesscode.rpginventory.inventory.slot.Slot;
import ru.endlesscode.rpginventory.inventory.slot.SlotManager;
import ru.endlesscode.rpginventory.item.CustomItem;
import ru.endlesscode.rpginventory.item.ItemManager;
import ru.endlesscode.rpginventory.misc.Config;
import ru.endlesscode.rpginventory.pet.PetManager;
import ru.endlesscode.rpginventory.pet.PetType;
import ru.endlesscode.rpginventory.utils.InventoryUtils;
import ru.endlesscode.rpginventory.utils.ItemUtils;
import ru.endlesscode.rpginventory.utils.PlayerUtils;

/**
 * Created by OsipXD on 18.09.2015
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
public class InventoryListener implements Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(final PlayerJoinEvent event) {
        final Player player = event.getPlayer();

        if (InventoryManager.isAllowedWorld(player.getWorld())) {
            InventoryManager.initPlayer(player, false);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        ResourcePackListener.removePlayer(player);
        InventoryManager.unloadPlayerInventory(player);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        if (!InventoryManager.playerIsLoaded(player)) {
            return;
        }

        if (!event.getKeepInventory()) {
            Inventory inventory = InventoryManager.get(player).getInventory();
            boolean dropForPlayer = !RPGInventory.getPermissions().has(player, "rpginventory.keep.all");

            int petSlotId = PetManager.getPetSlotId();
            if (PetManager.isEnabled() && inventory.getItem(petSlotId) != null) {
                Slot petSlot = SlotManager.getSlotManager().getPetSlot();
                ItemStack petItem = inventory.getItem(petSlotId);
                if (petSlot != null && petSlot.isDrop() && dropForPlayer && !petSlot.isCup(petItem)) {
                    event.getDrops().add(PetType.clone(petItem));
                    RPGInventory.getInstance().getServer().getPluginManager().callEvent(new PetUnequipEvent(player));
                    inventory.setItem(petSlotId, petSlot.getCup());
                }
            }

            for (Slot slot : SlotManager.getSlotManager().getPassiveSlots()) {
                for (int slotId : slot.getSlotIds()) {
                    ItemStack item = inventory.getItem(slotId);
                    if (dropForPlayer && !slot.isQuick() && !slot.isCup(item) && slot.isDrop()
                            && (!CustomItem.isCustomItem(item) || ItemManager.getCustomItem(item).isDrop())) {
                        event.getDrops().add(inventory.getItem(slotId));
                        inventory.setItem(slotId, slot.getCup());
                    }
                }
            }
        }

        PetManager.despawnPet(player);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void prePlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        if (!InventoryManager.playerIsLoaded(player)) {
            return;
        }

        InventoryLocker.lockSlots(player);
    }

    @EventHandler
    public void onQuickSlotHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();

        if (!InventoryManager.playerIsLoaded(player)) {
            return;
        }

        int slotId = event.getNewSlot();
        Slot slot = InventoryManager.getQuickSlot(slotId);
        if (slot != null && slot.isCup(player.getInventory().getItem(slotId))) {
            event.setCancelled(true);
            InventoryUtils.heldFreeSlot(player, slotId,
                    (event.getPreviousSlot() + 1) % 9 == slotId ? InventoryUtils.SearchType.NEXT : InventoryUtils.SearchType.PREV);
        }
    }

    @EventHandler
    public void onBreakItem(PlayerItemBreakEvent event) {
        this.onItemDisappeared(event, event.getBrokenItem());
    }

    @EventHandler
    public void onDropQuickSlot(PlayerDropItemEvent event) {
        this.onItemDisappeared(event, event.getItemDrop().getItemStack());
    }

    private void onItemDisappeared(PlayerEvent event, ItemStack item) {
        final Player player = event.getPlayer();
        final PlayerInventory inventory = player.getInventory();
        final int slotId = inventory.getHeldItemSlot();

        if (!InventoryManager.playerIsLoaded(player)) {
            return;
        }

        if (ItemUtils.isEmpty(inventory.getItemInMainHand()) || item.equals(inventory.getItemInMainHand())) {
            final Slot slot = InventoryManager.getQuickSlot(slotId);
            if (slot != null) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        InventoryUtils.heldFreeSlot(player, slotId, InventoryUtils.SearchType.NEXT);
                        inventory.setItem(slotId, slot.getCup());
                    }
                }.runTaskLater(RPGInventory.getInstance(), 1);
            }
        }
    }

    @EventHandler
    public void onPickupToQuickSlot(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();

        if (!InventoryManager.playerIsLoaded(player)) {
            return;
        }

        for (Slot quickSlot : SlotManager.getSlotManager().getQuickSlots()) {
            int slotId = quickSlot.getQuickSlot();
            if (quickSlot.isCup(player.getInventory().getItem(slotId)) && quickSlot.isValidItem(event.getItem().getItemStack())) {
                player.getInventory().setItem(slotId, event.getItem().getItemStack());
                event.getItem().remove();

                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, .3f, 1.7f);
                if (Config.getConfig().getBoolean("attack.auto-held")) {
                    player.getInventory().setHeldItemSlot(quickSlot.getQuickSlot());
                }

                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        Player player = (Player) event.getWhoClicked();

        if (!InventoryManager.playerIsLoaded(player)) {
            return;
        }

        for (Integer rawSlotId : event.getRawSlots()) {
            ItemStack cursor = event.getOldCursor();
            Inventory inventory = event.getInventory();

            if (PetManager.isPetItem(cursor)) {
                event.setCancelled(true);
                return;
            }

            if (inventory.getType() == InventoryType.CRAFTING) {
                if (InventoryManager.get(player).isOpened()) {
                    return;
                }

                if (rawSlotId >= 1 && rawSlotId <= 8) {
                    event.setCancelled(true);
                    return;
                }
            } else if (InventoryAPI.isRPGInventory(inventory)) {
                if (rawSlotId < 54) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(final InventoryClickEvent event) {
        final Player player = (Player) event.getWhoClicked();

        if (!InventoryManager.playerIsLoaded(player)) {
            return;
        }

        final int rawSlot = event.getRawSlot();
        InventoryType.SlotType slotType = InventoryUtils.getSlotType(event.getSlotType(), rawSlot);

        if (rawSlot > event.getView().getTopInventory().getSize() && event.getSlot() < 9) {
            slotType = InventoryType.SlotType.QUICKBAR;
        }

        final Slot slot = SlotManager.getSlotManager().getSlot(event.getSlot(), slotType);
        final Inventory inventory = event.getInventory();
        InventoryAction action = event.getAction();
        ActionType actionType = ActionType.getTypeOfAction(action);
        ItemStack currentItem = event.getCurrentItem();
        ItemStack cursor = event.getCursor();

        if (action == InventoryAction.HOTBAR_SWAP && SlotManager.getSlotManager().getSlot(event.getHotbarButton(), InventoryType.SlotType.QUICKBAR) != null) {
            event.setCancelled(true);
            return;
        }

        // Crafting area
        if (inventory.getType() == InventoryType.CRAFTING) {
            if (InventoryManager.get(player).isOpened()) {
                return;
            }

            switch (event.getSlotType()) {
                case RESULT:
                    InventoryManager.get(player).openInventory(true);
                case ARMOR:
                case CRAFTING:
                    event.setCancelled(true);
                    return;
            }
        }

        // In RPG Inventory or quick slot
        if (InventoryAPI.isRPGInventory(inventory)
                || slotType == InventoryType.SlotType.QUICKBAR && slot != null
                && (slot.isQuick() || slot.getSlotType() == Slot.SlotType.SHIELD) && player.getGameMode() != GameMode.CREATIVE) {
            if (rawSlot < 54 && slot == null || action == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                event.setCancelled(true);
                return;
            }

            if (rawSlot == -999 || rawSlot >= 54 && slotType != InventoryType.SlotType.QUICKBAR || slot == null) {
                return;
            }

            PlayerWrapper playerWrapper = null;
            if (InventoryAPI.isRPGInventory(inventory)) {
                playerWrapper = (PlayerWrapper) inventory.getHolder();

                // Check flying
                if (playerWrapper.isFlying()) {
                    player.sendMessage(RPGInventory.getLanguage().getCaption("error.fall"));
                    event.setCancelled(true);
                    return;
                }
            }

            if (!validateClick(player, playerWrapper, slot, actionType, currentItem, slotType) || slot.getSlotType() == Slot.SlotType.INFO) {
                event.setCancelled(true);
                return;
            }

            if (slot.getSlotType() == Slot.SlotType.ACTION) {
                //noinspection ConstantConditions
                ((ActionSlot) slot).preformAction(player);
                event.setCancelled(true);
                return;
            }

            if (playerWrapper != null && slot.getSlotType() == Slot.SlotType.ARMOR) {
                onArmorSlotClick(event, playerWrapper, slot, cursor, currentItem);
                return;
            }

            if (slot.getSlotType() == Slot.SlotType.ACTIVE || slot.getSlotType() == Slot.SlotType.PASSIVE
                    || slot.getSlotType() == Slot.SlotType.SHIELD || slot.getSlotType() == Slot.SlotType.ELYTRA) {
                event.setCancelled(!InventoryManager.validateUpdate(player, actionType, slot, cursor));

                if ((slot.getSlotType() == Slot.SlotType.SHIELD || slot.getSlotType() == Slot.SlotType.ELYTRA) &&
                        actionType == ActionType.DROP) {
                    event.setCancelled(true);
                }
            } else if (slot.getSlotType() == Slot.SlotType.PET) {
                event.setCancelled(!InventoryManager.validatePet(player, action, currentItem, cursor));
            } else if (slot.getSlotType() == Slot.SlotType.MYPET) {
                event.setCancelled(!MyPetManager.validatePet(player, action, currentItem, cursor));
            } else if (slot.getSlotType() == Slot.SlotType.BACKPACK) {
                if (event.getClick() == ClickType.RIGHT && BackpackManager.open(player, currentItem)) {
                    event.setCancelled(true);
                } else if (actionType != ActionType.GET) {
                    event.setCancelled(!BackpackManager.isBackpack(cursor));
                }
            }

            if (!event.isCancelled()) {
                BukkitRunnable cupPlacer = new BukkitRunnable() {
                    @Override
                    public void run() {
                        inventory.setItem(rawSlot, slot.getCup());
                        player.updateInventory();
                    }
                };

                if (slot.isQuick()) {
                    InventoryManager.updateQuickSlot(player, inventory, slot, event.getSlot(), slotType,
                            action, currentItem, cursor);
                    event.setCancelled(true);
                } else if (slot.getSlotType() == Slot.SlotType.SHIELD) {
                    InventoryManager.updateShieldSlot(player, inventory, slot, event.getSlot(), slotType,
                            action, currentItem, cursor);
                    event.setCancelled(true);

                    if (actionType == ActionType.GET || actionType == ActionType.DROP) {
                        cupPlacer.runTaskLater(RPGInventory.getInstance(), 1);
                    }
                } else if (actionType == ActionType.GET || actionType == ActionType.DROP) {
                    cupPlacer.runTaskLater(RPGInventory.getInstance(), 1);
                } else if (slot.isCup(currentItem)) {
                    event.setCurrentItem(null);
                }
            }
        }
    }

    /**
     * Check
     *
     * @return Click is valid
     */
    private boolean validateClick(Player player, PlayerWrapper playerWrapper, Slot slot,
                                  ActionType actionType, ItemStack currentItem, InventoryType.SlotType slotType) {
        if (playerWrapper != null) {
            if (player != playerWrapper.getPlayer()) {
                return false;
            }

            if (!PlayerUtils.checkLevel(player, slot.getRequiredLevel())) {
                player.sendMessage(String.format(RPGInventory.getLanguage().getCaption("error.level"), slot.getRequiredLevel()));
                return false;
            }

            if (!slot.isFree() && !playerWrapper.isBuyedSlot(slot.getName()) && !InventoryManager.buySlot(player, playerWrapper, slot)) {
                return false;
            }
        }

        return !((actionType == ActionType.GET && slot.getSlotType() != Slot.SlotType.ACTION
                || actionType == ActionType.DROP) && slot.isCup(currentItem) && slotType != InventoryType.SlotType.QUICKBAR);
    }

    /**
     * It happens when player click on armor slot
     */
    private void onArmorSlotClick(InventoryClickEvent event, PlayerWrapper playerWrapper, final Slot slot,
                                  ItemStack cursor, ItemStack currentItem) {
        final Player player = playerWrapper.getPlayer().getPlayer();
        final Inventory inventory = event.getInventory();
        final int rawSlot = event.getRawSlot();
        InventoryAction action = event.getAction();
        ActionType actionType = ActionType.getTypeOfAction(action);

        if (InventoryManager.validateArmor(action, slot, cursor)) {
            // Event of equip armor
            InventoryClickEvent fakeEvent = new InventoryClickEvent((playerWrapper.getInventoryView()),
                    InventoryType.SlotType.ARMOR, InventoryUtils.getArmorSlotId(slot), event.getClick(), action);
            Bukkit.getPluginManager().callEvent(fakeEvent);

            if (fakeEvent.isCancelled()) {
                event.setCancelled(true);
                return;
            }

            InventoryManager.updateArmor(player, inventory, slot, rawSlot, action, currentItem, cursor);

            if (actionType == ActionType.GET) {
                inventory.setItem(rawSlot, slot.getCup());
            } else if (slot.isCup(currentItem)) {
                player.setItemOnCursor(new ItemStack(Material.AIR));
            }

            player.updateInventory();
        }

        if (actionType == ActionType.DROP) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    inventory.setItem(rawSlot, slot.getCup());
                    player.updateInventory();
                }
            }.runTaskLater(RPGInventory.getInstance(), 1);
        } else {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onInventoryOpen(InventoryOpenEvent event) {
        Inventory inventory = event.getInventory();
        HumanEntity player = event.getPlayer();

        if (!InventoryManager.playerIsLoaded(player)) {
            return;
        }

        if (InventoryAPI.isRPGInventory(inventory)) {
            PlayerWrapper playerWrapper = (PlayerWrapper) inventory.getHolder();
            InventoryManager.syncQuickSlots(playerWrapper);
            InventoryManager.syncInfoSlots(playerWrapper);
            InventoryManager.syncShieldSlot(playerWrapper);
            InventoryManager.syncArmor(playerWrapper);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (InventoryAPI.isRPGInventory(event.getInventory())) {
            PlayerWrapper playerWrapper = (PlayerWrapper) event.getInventory().getHolder();
            if (event.getPlayer() != playerWrapper.getPlayer()) {
                return;
            }

            playerWrapper.onClose();
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onWorldChanged(PlayerChangedWorldEvent event) {
        InventoryManager.unloadPlayerInventory(event.getPlayer());
    }

    @EventHandler
    public void postWorldChanged(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        if (InventoryManager.isAllowedWorld(player.getWorld())) {
            InventoryManager.initPlayer(player, InventoryManager.isAllowedWorld(event.getFrom()));
        }
    }
}
