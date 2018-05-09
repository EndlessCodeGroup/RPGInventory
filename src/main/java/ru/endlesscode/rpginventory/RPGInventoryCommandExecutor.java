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

package ru.endlesscode.rpginventory;

import net.milkbowl.vault.permission.Permission;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;

import java.util.List;

import org.jetbrains.annotations.*;
import ru.endlesscode.rpginventory.api.InventoryAPI;
import ru.endlesscode.rpginventory.inventory.InventoryManager;
import ru.endlesscode.rpginventory.inventory.backpack.BackpackManager;
import ru.endlesscode.rpginventory.item.ItemManager;
import ru.endlesscode.rpginventory.pet.PetManager;
import ru.endlesscode.rpginventory.utils.*;

/**
 * Created by OsipXD on 28.08.2015
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
@SuppressWarnings("deprecation")
class RPGInventoryCommandExecutor implements CommandExecutor {

    private static void tryToGivePet(CommandSender sender, String[] args) {
        if (args.length == 1) {
            printPetsList(sender);
        } else if (args.length >= 3 && validatePlayer(sender, args[1])) {
            RPGInventoryCommandExecutor.givePet(sender, args[1], args[2]);
            return;
        }

        sender.sendMessage(StringUtils.coloredLine("&3Usage: &6/rpginv pet [&eplayer&6] [&epetId&6]"));
    }

    private static void tryToGiveFood(CommandSender sender, String[] args) {
        if (args.length == 1) {
            printFoodList(sender);
        } else if (args.length >= 3 && validatePlayer(sender, args[1])) {
            RPGInventoryCommandExecutor.giveFood(sender, args[1], args[2], args.length > 3 ? args[3] : "1");
            return;
        }

        sender.sendMessage(StringUtils.coloredLine("&3Usage: &6/rpginv food [&eplayer&6] [&efoodId&6] (&eamount&6)"));
    }

    private static void tryToGiveItem(CommandSender sender, String[] args) {
        if (args.length == 1) {
            printItemsList(sender);
        } else if (args.length >= 3 && validatePlayer(sender, args[1])) {
            RPGInventoryCommandExecutor.giveItem(sender, args[1], args[2]);
            return;
        }

        sender.sendMessage(StringUtils.coloredLine("&3Usage: &6/rpginv item [&eplayer&6] [&eitemId&6]"));
    }

    private static void tryToGiveBackpack(CommandSender sender, String[] args) {
        if (args.length == 1) {
            printBackpacksList(sender);
        } else if (args.length >= 3 && validatePlayer(sender, args[1])) {
            RPGInventoryCommandExecutor.giveBackpack(sender, args[1], args[2]);
            return;
        }

        sender.sendMessage(StringUtils.coloredLine("&3Usage: &6/rpginv bp [&eplayer&6] [&eitemId&6]"));
    }

    private static void givePet(@NotNull CommandSender sender, String playerName, String petId) {
        Player player = RPGInventory.getInstance().getServer().getPlayer(playerName);
        ItemStack petItem = PetManager.getPetItem(petId);
        String prefix = "Pet '" + petId + "'";

        if (ItemUtils.isEmpty(petItem)) {
            sender.sendMessage(StringUtils.coloredLine("&c" + prefix + " not found!"));
            printPetsList(sender);
        } else {
            giveItemToPlayer(sender, player, petItem, prefix);
        }
    }

    private static void giveFood(@NotNull CommandSender sender, String playerName, String foodId, @NotNull String stringAmount) {
        Player player = RPGInventory.getInstance().getServer().getPlayer(playerName);
        ItemStack foodItem = PetManager.getFoodItem(foodId);
        String prefix = "Food '" + foodId + "'";

        if (ItemUtils.isEmpty(foodItem)) {
            sender.sendMessage(StringUtils.coloredLine("&c" + prefix + " not found!"));
            printFoodList(sender);
        } else {
            try {
                int amount = Integer.parseInt(stringAmount);
                foodItem.setAmount(amount);
                giveItemToPlayer(sender, player, foodItem, prefix);
            } catch (NumberFormatException e) {
                sender.sendMessage(StringUtils.coloredLine("&cThe amount must be a number!"));
            }
        }
    }

    private static void giveItem(@NotNull CommandSender sender, String playerName, String itemId) {
        Player player = RPGInventory.getInstance().getServer().getPlayer(playerName);
        ItemStack item = ItemManager.getItem(itemId);
        String prefix = "Item '" + itemId + "'";

        if (ItemUtils.isEmpty(item)) {
            sender.sendMessage(StringUtils.coloredLine("&c" + prefix + " not found!"));
            printItemsList(sender);
        } else {
            giveItemToPlayer(sender, player, item, prefix);
        }
    }

    private static void giveBackpack(@NotNull CommandSender sender, String playerName, String id) {
        Player player = RPGInventory.getInstance().getServer().getPlayer(playerName);
        ItemStack bpItem = BackpackManager.getItem(id);
        String prefix = "Backpack '" + id + "'";

        if (ItemUtils.isEmpty(bpItem)) {
            sender.sendMessage(StringUtils.coloredLine("&c" + prefix + " not found!"));
            printBackpacksList(sender);
        } else {
            giveItemToPlayer(sender, player, bpItem, prefix);
        }
    }

    private static void printPetsList(@NotNull CommandSender sender) {
        printList(sender, PetManager.getPetList(), "Pets");
    }

    private static void giveItemToPlayer(@NotNull CommandSender sender, Player player, ItemStack item, String prefix) {
        String message;
        if (player.getInventory().addItem(item).isEmpty()) {
            message = "&3" + prefix + " has been given to " + player.getName();
        } else {
            message = "&c" + player.getName() + " has no empty slots in the inventory.";
        }
        sender.sendMessage(StringUtils.coloredLine(message));
    }

    private static void printFoodList(@NotNull CommandSender sender) {
        printList(sender, PetManager.getFoodList(), "Food");
    }

    private static void printItemsList(@NotNull CommandSender sender) {
        printList(sender, ItemManager.getItemList(), "Items");
    }

    private static void printBackpacksList(@NotNull CommandSender sender) {
        printList(sender, BackpackManager.getBackpackList(), "Backpacks");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, Command command, String label, @NotNull String[] args) {
        Permission perms = RPGInventory.getPermissions();

        if (args.length > 0) {
            String subCommand = args[0].toLowerCase();

            if (perms.has(sender, "rpginventory.admin")) {
                if (subCommand.startsWith("p")) {
                    // pets
                    RPGInventoryCommandExecutor.tryToGivePet(sender, args);
                    return true;
                } else if (subCommand.startsWith("f")) {
                    // food
                    RPGInventoryCommandExecutor.tryToGiveFood(sender, args);
                    return true;
                } else if (subCommand.startsWith("i")) {
                    // items
                    RPGInventoryCommandExecutor.tryToGiveItem(sender, args);
                    return true;
                } else if (subCommand.startsWith("b")) {
                    // backpacks
                    RPGInventoryCommandExecutor.tryToGiveBackpack(sender, args);
                    return true;
                } else if (subCommand.startsWith("l")) {
                    // list
                    RPGInventoryCommandExecutor.onCommandList(sender);
                    return true;
                } else if (subCommand.startsWith("r")) {
                    // reload
                    RPGInventoryCommandExecutor.reloadPlugin(sender);
                    return true;
                }
            }

            switch (subCommand.charAt(0)) {
                // open
                case 'o':
                    if (args.length == 1) {
                        if (perms.has(sender, "rpginventory.open")) {
                            RPGInventoryCommandExecutor.openInventory(sender);
                        } else {
                            missingRights(sender);
                        }
                    } else if (perms.has(sender, "rpginventory.open.others")) {
                        RPGInventoryCommandExecutor.openInventory(sender, args[1]);
                    } else {
                        missingRights(sender);
                    }
                    break;
                default:
                    RPGInventoryCommandExecutor.printHelp(sender);
                    break;
            }
        } else {
            RPGInventoryCommandExecutor.printHelp(sender);
        }

        return true;
    }

    private static void printList(@NotNull CommandSender sender, List<String> list, String prefix) {
        String message = String.format(list.isEmpty() ? "&c%s not found..." : "&3%s list: &6" + list, prefix);
        sender.sendMessage(StringUtils.coloredLine(message));
    }

    private static void onCommandList(@NotNull CommandSender sender) {
        sender.sendMessage(StringUtils.coloredLine("&cCommand &6/rpginv list [&etype&6]&c was removed."));
        sender.sendMessage(StringUtils.coloredLine("&3Use &6/rpginv [&epets&6|&efood&6|&eitems&6|&ebackpacks&6]&3 instead."));
    }

    private static void reloadPlugin(CommandSender sender) {
        PluginManager pm = RPGInventory.getInstance().getServer().getPluginManager();
        pm.disablePlugin(RPGInventory.getInstance());
        pm.enablePlugin(RPGInventory.getInstance());
        sender.sendMessage(StringUtils.coloredLine("&e[RPGInventory] Plugin successfully reloaded!"));
    }

    private static void printHelp(CommandSender sender) {
        sender.sendMessage(StringUtils.coloredLine("&3===================&b[&eRPGInventory&b]&3====================="));
        sender.sendMessage(StringUtils.coloredLine("&8[] &7Required, &8() &7Optional"));

        if (RPGInventory.getPermissions().has(sender, "rpginventory.open.others")) {
            sender.sendMessage(StringUtils.coloredLine("&6rpginv open (&eplayer&6) &7- open inventory"));
        } else if (RPGInventory.getPermissions().has(sender, "rpginventory.open")) {
            sender.sendMessage(StringUtils.coloredLine("&6rpginv open &7- open inventory"));
        }

        if (RPGInventory.getPermissions().has(sender, "rpginventory.admin")) {
            sender.sendMessage(StringUtils.coloredLine("&6rpginv reload &7- reload config"));
            sender.sendMessage(StringUtils.coloredLine("&6rpginv [&epets&6|&efood&6|&eitems&6|&ebackpacks&6] &7- show list of pets, items etc."));
            sender.sendMessage(StringUtils.coloredLine("&6rpginv food [&eplayer&6] [&efoodId&6] (&eamount&6) &7- gives food to player"));
            sender.sendMessage(StringUtils.coloredLine("&6rpginv pet [&eplayer&6] [&epetId&6] &7- gives pet to player"));
            sender.sendMessage(StringUtils.coloredLine("&6rpginv item [&eplayer&6] [&eitemId&6] &7- gives item to player"));
            sender.sendMessage(StringUtils.coloredLine("&6rpginv bp [&eplayer&6] [&ebackpackId&6] &7- gives backpack to player"));
        }

        sender.sendMessage(StringUtils.coloredLine("&3====================================================="));
    }

    private static void openInventory(@NotNull CommandSender sender) {
        if (!validatePlayer(sender)) {
            return;
        }

        Player player = ((Player) sender).getPlayer();
        if (InventoryAPI.isRPGInventory(player.getOpenInventory().getTopInventory())) {
            return;
        }

        InventoryManager.get(player).openInventory();
    }

    private static void openInventory(@NotNull CommandSender sender, String playerName) {
        if (!validatePlayer(sender) || !validatePlayer(sender, playerName)) {
            return;
        }

        Player player = RPGInventory.getInstance().getServer().getPlayer(playerName);
        ((Player) sender).openInventory(InventoryManager.get(player).getInventory());
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean validatePlayer(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(StringUtils.coloredLine("&cThis command not allowed from console."));
            return false;
        }

        return validatePlayer(sender, (Player) sender);
    }

    private static boolean validatePlayer(@NotNull CommandSender sender, String playerName) {
        Player player = RPGInventory.getInstance().getServer().getPlayer(playerName);
        if (player == null) {
            sender.sendMessage(StringUtils.coloredLine("&cPlayer '" + playerName + "' not found!"));
        }

        return validatePlayer(sender, player);
    }

    private static boolean validatePlayer(@NotNull CommandSender sender, Player player) {
        if (!InventoryManager.playerIsLoaded(player)) {
            sender.sendMessage(StringUtils.coloredLine("&cThis command not allowed here."));
            return false;
        }

        return true;
    }

    private static void missingRights(CommandSender sender) {
        sender.sendMessage(RPGInventory.getLanguage().getMessage("message.perms"));
        RPGInventoryCommandExecutor.printHelp(sender);
    }
}
