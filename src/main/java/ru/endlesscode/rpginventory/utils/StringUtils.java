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

package ru.endlesscode.rpginventory.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.external.EZPlaceholderHook;
import ru.endlesscode.rpginventory.RPGInventory;
import ru.endlesscode.rpginventory.inventory.InventoryManager;
import ru.endlesscode.rpginventory.inventory.PlayerWrapper;
import ru.endlesscode.rpginventory.item.ItemManager;
import ru.endlesscode.rpginventory.item.ItemStat;

/**
 * Created by OsipXD on 29.08.2015
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
public class StringUtils {
    @NotNull
    public static String coloredLine(String line) {
        return ChatColor.translateAlternateColorCodes('&', line);
    }

    public static List<String> coloredLines(List<String> lines) {
        List<String> coloredLines = new ArrayList<>(lines.size());
        for (String line : lines) {
            coloredLines.add(StringUtils.coloredLine(line));
        }

        return coloredLines;
    }

    public static String doubleToString(double value) {
        return value == (long) value ? String.format("%d", (long) value) : String.format("%s", value);
    }

    public static void coloredConsole(String message) {
        Bukkit.getServer().getConsoleSender().sendMessage(message);
    }

    public static String setPlaceholders(Player player, String line) {
        // Using Placeholder API
        if (RPGInventory.placeholderApiHooked()) {
            return PlaceholderAPI.setPlaceholders(player, line);
        }

        // Player
        line = line.replaceAll("%WORLD%", player.getWorld().getName());
        line = line.replaceAll("%PLAYER%", player.getName());
        line = line.replaceAll("%HP%", Utils.round(player.getHealth(), 1) + "");
        line = line.replaceAll("%MAX_HP%", player.getMaxHealth() + "");

        PlayerWrapper playerWrapper = InventoryManager.get(player);
        if (playerWrapper != null) {

            // Modifiers
            line = line.replaceAll("%DAMAGE%", ItemManager.getModifier(player, ItemStat.StatType.DAMAGE).toString());
            line = line.replaceAll("%BOW_DAMAGE%", ItemManager.getModifier(player, ItemStat.StatType.BOW_DAMAGE).toString());
            line = line.replaceAll("%HAND_DAMAGE%", ItemManager.getModifier(player, ItemStat.StatType.HAND_DAMAGE).toString());
            line = line.replaceAll("%CRIT_DAMAGE%", ItemManager.getModifier(player, ItemStat.StatType.CRIT_DAMAGE).toString());
            line = line.replaceAll("%CRIT_CHANCE%", ItemManager.getModifier(player, ItemStat.StatType.CRIT_CHANCE).toString());
            line = line.replaceAll("%ARMOR%", ItemManager.getModifier(player, ItemStat.StatType.ARMOR).toString());
            line = line.replaceAll("%SPEED%", ItemManager.getModifier(player, ItemStat.StatType.SPEED).toString());
            line = line.replaceAll("%JUMP%", ItemManager.getModifier(player, ItemStat.StatType.JUMP).toString());
        }

        return line;
    }

    public static class Placeholders extends EZPlaceholderHook {
        public Placeholders() {
            super(RPGInventory.getInstance(), "rpginv");
        }

        @Override
        public String onPlaceholderRequest(Player player, String identifier) {
            if (player == null) {
                return "";
            }

            PlayerWrapper playerWrapper = InventoryManager.get(player);
            if (playerWrapper == null) {
                return "";
            }

            switch (identifier) {
                case "damage_bonus":
                    return ItemManager.getModifier(player, ItemStat.StatType.DAMAGE).toString();
                case "bow_damage_bonus":
                    return ItemManager.getModifier(player, ItemStat.StatType.BOW_DAMAGE).toString();
                case "hand_damage_bonus":
                    return ItemManager.getModifier(player, ItemStat.StatType.HAND_DAMAGE).toString();
                case "crit_damage_bonus":
                    return ItemManager.getModifier(player, ItemStat.StatType.CRIT_DAMAGE).toString();
                case "crit_chance":
                    return ItemManager.getModifier(player, ItemStat.StatType.CRIT_CHANCE).toString();
                case "armor_bonus":
                    return ItemManager.getModifier(player, ItemStat.StatType.ARMOR).toString();
                case "speed_bonus":
                    return ItemManager.getModifier(player, ItemStat.StatType.SPEED).toString();
                case "jump_bonus":
                    return ItemManager.getModifier(player, ItemStat.StatType.JUMP).toString();
            }

            return "";
        }
    }
}
