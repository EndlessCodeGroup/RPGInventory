package ru.endlesscode.rpginventory.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.endlesscode.rpginventory.event.updater.HealthUpdater;
import ru.endlesscode.rpginventory.inventory.InventoryManager;
import ru.endlesscode.rpginventory.inventory.PlayerWrapper;
import ru.endlesscode.rpginventory.item.ItemManager;
import ru.endlesscode.rpginventory.item.ItemStat;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by OsipXD on 29.08.2015
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
public class StringUtils {
    @NotNull
    public static String coloredLine(@NotNull String line) {
        return ChatColor.translateAlternateColorCodes('&', line);
    }

    public static List<String> coloredLines(List<String> lines) {
        List<String> coloredLines = new ArrayList<>(lines.size());
        for (String line : lines) {
            coloredLines.add(StringUtils.coloredLine(line));
        }

        return coloredLines;
    }

    public static String roundDouble(double value) {
        return value == (long) value ? String.format("%d", (long) value) : String.format("%s", value);
    }

    public static void coloredConsole(String message) {
        Bukkit.getServer().getConsoleSender().sendMessage(message);
    }

    public static String applyPlaceHolders(String line, Player player) {
        PlayerWrapper playerWrapper = InventoryManager.get(player);
        if (playerWrapper == null) {
            return "[Not allowed here]";
        }

        // Player
        line = line.replaceAll("%WORLD%", player.getWorld().getName());
        line = line.replaceAll("%PLAYER%", player.getName());
        line = line.replaceAll("%HP%", Utils.round(player.getHealth(), 1) + "");
        line = line.replaceAll("%MAX_HP%", player.getMaxHealth() + "");

        // Health Updater
        HealthUpdater hu = playerWrapper.getHealthUpdater();
        line = line.replaceAll("%PLUGIN_HP%", hu.getModifiedHealth() + "");
        line = line.replaceAll("%OTHER_HP%", hu.getOtherPluginsBonus() + hu.getAttributesBonus() + "");

        // Modifiers
        line = line.replaceAll("%DAMAGE%", ItemManager.getModifier(player, ItemStat.StatType.DAMAGE).toString());
        line = line.replaceAll("%BOW_DAMAGE%", ItemManager.getModifier(player, ItemStat.StatType.BOW_DAMAGE).toString());
        line = line.replaceAll("%HAND_DAMAGE%", ItemManager.getModifier(player, ItemStat.StatType.HAND_DAMAGE).toString());
        line = line.replaceAll("%CRIT_DAMAGE%", ItemManager.getModifier(player, ItemStat.StatType.CRIT_DAMAGE).toString());
        line = line.replaceAll("%CRIT_CHANCE%", ItemManager.getModifier(player, ItemStat.StatType.CRIT_CHANCE).toString());
        line = line.replaceAll("%ARMOR%", ItemManager.getModifier(player, ItemStat.StatType.ARMOR).toString());
        line = line.replaceAll("%SPEED%", ItemManager.getModifier(player, ItemStat.StatType.SPEED).toString());
        line = line.replaceAll("%JUMP%", ItemManager.getModifier(player, ItemStat.StatType.JUMP).toString());

        return line;
    }
}
