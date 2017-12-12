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

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;

import java.util.List;

import ru.endlesscode.rpginventory.api.InventoryAPI;
import ru.endlesscode.rpginventory.inventory.InventoryManager;
import ru.endlesscode.rpginventory.inventory.backpack.BackpackManager;
import ru.endlesscode.rpginventory.item.ItemManager;
import ru.endlesscode.rpginventory.pet.PetManager;
import ru.endlesscode.rpginventory.utils.StringUtils;

/**
 * Created by OsipXD on 28.08.2015
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
@SuppressWarnings("deprecation")
class RPGInventoryCommandExecutor implements CommandExecutor {
    private static void givePet(CommandSender sender, String playerName, String petId) {
        if (validatePlayer(sender, playerName)) {
            Player player = RPGInventory.getInstance().getServer().getPlayer(playerName);
            ItemStack petItem = PetManager.getPetItem(petId);

            if (petItem != null) {
                player.getInventory().addItem(petItem);
                return;
            } else {
                sender.sendMessage(StringUtils.coloredLine("&cPet '" + petId + "' not found!"));
            }
        }

        sender.sendMessage(StringUtils.coloredLine("&3Use &6/rpginv pet [&eplayer&6] [&epetId&6]"));
    }

    private static void giveFood(CommandSender sender, String playerName, String foodId, String stringAmount) {
        if (validatePlayer(sender, playerName)) {
            Player player = RPGInventory.getInstance().getServer().getPlayer(playerName);
            ItemStack foodItem = PetManager.getFoodItem(foodId);

            if (foodItem != null) {
                try {
                    int amount = Integer.parseInt(stringAmount);
                    foodItem.setAmount(amount);
                    player.getInventory().addItem(foodItem);
                    return;
                } catch (NumberFormatException e) {
                    sender.sendMessage(StringUtils.coloredLine("&cThe amount must be a number!"));
                }
            } else {
                sender.sendMessage(StringUtils.coloredLine("&cFood '" + foodId + "' not found!"));
            }
        }

        sender.sendMessage(StringUtils.coloredLine("&3Use &6/rpginv food [&eplayer&6] [&efoodId&6] (&eamount&6)"));
    }

    private static void giveItem(CommandSender sender, String playerName, String itemId) {
        if (validatePlayer(sender, playerName)) {
            Player player = RPGInventory.getInstance().getServer().getPlayer(playerName);
            ItemStack petItem = ItemManager.getItem(itemId);

            if (petItem != null) {
                player.getInventory().addItem(petItem);
                return;
            } else {
                sender.sendMessage(StringUtils.coloredLine("&cItem '" + itemId + "' not found!"));
            }
        }

        sender.sendMessage(StringUtils.coloredLine("&3Use &6/rpginv item [&eplayer&6] [&eitemId&6]"));
    }

    private static void giveBackpack(CommandSender sender, String playerName, String id) {
        if (validatePlayer(sender, playerName)) {
            Player player = RPGInventory.getInstance().getServer().getPlayer(playerName);
            ItemStack bpItem = BackpackManager.getItem(id);

            if (bpItem != null) {
                player.getInventory().addItem(bpItem);
                return;
            } else {
                sender.sendMessage(StringUtils.coloredLine("&cBackpack '" + id + "' not found!"));
            }
        }

        sender.sendMessage(StringUtils.coloredLine("&3Use &6/rpginv bp [&eplayer&6] [&eitemId&6]"));
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
            sender.sendMessage(StringUtils.coloredLine("&6rpginv list [&etype&6] &7- show list of pets, food or items"));
            sender.sendMessage(StringUtils.coloredLine("&6rpginv food [&eplayer&6] [&efoodId&6] (&eamount&6) &7- gives food to player"));
            sender.sendMessage(StringUtils.coloredLine("&6rpginv pet [&eplayer&6] [&epetId&6] &7- gives pet to player"));
            sender.sendMessage(StringUtils.coloredLine("&6rpginv item [&eplayer&6] [&eitemId&6] &7- gives item to player"));
            sender.sendMessage(StringUtils.coloredLine("&6rpginv bp [&eplayer&6] [&ebackpackId&6] &7- gives backpack to player"));
        }

        sender.sendMessage(StringUtils.coloredLine("&3====================================================="));
    }

    private static void printList(CommandSender sender, String type) {
        switch (type) {
            case "pet":
            case "pets":
                List<String> petList = PetManager.getPetList();
                sender.sendMessage(StringUtils.coloredLine(petList.size() == 0 ? "&cPets not found..." : "&3Pets list: &6" + petList));
                break;
            case "food":
                List<String> foodList = PetManager.getFoodList();
                sender.sendMessage(StringUtils.coloredLine(foodList.size() == 0 ? "&cFood not found..." : "&3Food list: &6" + foodList));
                break;
            case "item":
            case "items":
                List<String> itemList = ItemManager.getItemList();
                sender.sendMessage(StringUtils.coloredLine(itemList.size() == 0 ? "&cItems not found..." : "&3Items list: &6" + itemList));
                break;
            case "bp":
            case "backpack":
            case "backpacks":
                List<String> bpList = BackpackManager.getBackpackList();
                sender.sendMessage(StringUtils.coloredLine(bpList.size() == 0 ? "&cBackpacks not found..." : "&3Backpacks list: &6" + bpList));
                break;
            default:
                sender.sendMessage(StringUtils.coloredLine("&3Use &6/rpginv list [&epets&6|&efood&6|&eitems&6|&ebackpacks&6]"));
                break;
        }
    }

    private static void reloadPlugin(CommandSender sender) {
        PluginManager pm = RPGInventory.getInstance().getServer().getPluginManager();
        pm.disablePlugin(RPGInventory.getInstance());
        pm.enablePlugin(RPGInventory.getInstance());
        sender.sendMessage(StringUtils.coloredLine("&e[RPGInventory] Plugin successfully reloaded!"));
    }

    private static void openInventory(CommandSender sender) {
        if (!validatePlayer(sender)) {
            return;
        }

        Player player = ((Player) sender).getPlayer();
        if (InventoryAPI.isRPGInventory(player.getOpenInventory().getTopInventory())) {
            return;
        }

        InventoryManager.get(player).openInventory();
    }

    private static void openInventory(CommandSender sender, String playerName) {
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

    private static boolean validatePlayer(CommandSender sender, String playerName) {
        Player player = RPGInventory.getInstance().getServer().getPlayer(playerName);
        if (player == null) {
            sender.sendMessage(StringUtils.coloredLine("&cPlayer '" + playerName + "' not found!"));
        }

        return validatePlayer(sender, player);
    }

    private static boolean validatePlayer(CommandSender sender, Player player) {
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

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Permission perms = RPGInventory.getPermissions();

        if (args.length > 0) {
            String subCommand = args[0];

            if (perms.has(sender, "rpginventory.admin")) {
                if (subCommand.equals("pet") && args.length >= 3) {
                    RPGInventoryCommandExecutor.givePet(sender, args[1], args[2]);
                    return true;
                } else if (subCommand.equals("food") && args.length >= 3) {
                    RPGInventoryCommandExecutor.giveFood(sender, args[1], args[2], args.length > 3 ? args[3] : "1");
                    return true;
                } else if (subCommand.equals("item") && args.length >= 3) {
                    RPGInventoryCommandExecutor.giveItem(sender, args[1], args[2]);
                    return true;
                } else if (subCommand.equals("bp") && args.length >= 3) {
                    RPGInventoryCommandExecutor.giveBackpack(sender, args[1], args[2]);
                    return true;
                } else if (subCommand.equals("list") && args.length >= 2) {
                    RPGInventoryCommandExecutor.printList(sender, args[1]);
                    return true;
                } else if (subCommand.equals("reload")) {
                    RPGInventoryCommandExecutor.reloadPlugin(sender);
                    return true;
                }
            }

            switch (subCommand) {
                case "open":
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
}
