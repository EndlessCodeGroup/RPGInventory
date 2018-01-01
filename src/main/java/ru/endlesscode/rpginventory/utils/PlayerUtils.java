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

import com.herocraftonline.heroes.Heroes;
import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.player.PlayerData;
import de.tobiyas.racesandclasses.APIs.ClassAPI;
import de.tobiyas.racesandclasses.APIs.LevelAPI;
import de.tobiyas.racesandclasses.datacontainer.traitholdercontainer.classes.ClassContainer;
import me.baks.rpl.api.API;
import me.leothepro555.skills.Skills;
import me.robin.battlelevels.api.BattleLevelsAPI;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import ru.endlesscode.rpginventory.RPGInventory;
import ru.endlesscode.rpginventory.inventory.InventoryManager;
import ru.endlesscode.rpginventory.inventory.PlayerWrapper;

import java.util.List;

/**
 * Created by OsipXD on 09.11.2015
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
public class PlayerUtils {
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean checkLevel(Player player, int required) {
        int level = 0;
        switch (RPGInventory.getLevelSystem()) {
            case EXP:
                level = player.getLevel();
                break;
            case SKILLAPI:
                PlayerData playerData = SkillAPI.getPlayerData(player);
                level = playerData.hasClass() ? playerData.getMainClass().getLevel() : 0;
                break;
            case BATTLELEVELS:
                level = BattleLevelsAPI.getLevel(player.getUniqueId());
                break;
            case SKILLS:
                level = Skills.getLevel(player);
                break;
            case HEROES:
                level = Heroes.getInstance().getCharacterManager().getHero(player).getLevel();
                break;
            case RAC:
                level = LevelAPI.getCurrentLevel(player);
                break;
            case RPGPL:
                level = new API().getPlayerLevel(player);
                break;
        }

        return level >= required;
    }

    public static boolean checkClass(Player player, List<String> classes) {
        String playerClass = "";
        switch (RPGInventory.getClassSystem()) {
            case PERMISSIONS:
                for (String theClass : classes) {
                    if (RPGInventory.getPermissions().has(player, "rpginventory.class." + theClass)) {
                        playerClass = theClass;
                        break;
                    }
                }
                break;
            case SKILLAPI:
                PlayerData data = SkillAPI.getPlayerData(player);
                if (data.hasClass()) {
                    playerClass = data.getMainClass().getData().getName();
                }
                break;
            //class check from skills pro
            case SKILLS:
                playerClass = Skills.getSkill(player).toString();
                break;
            case HEROES:
                playerClass = Heroes.getInstance().getCharacterManager().getHero(player).getHeroClass().getName();
                break;
            case RAC:
                ClassContainer classContainer = ClassAPI.getClassOfPlayer(player);
                playerClass = classContainer == null ? null : classContainer.getDisplayName();
        }

        return classes.contains(playerClass);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean checkMoney(Player player, double cost) {
        double balance = (RPGInventory.economyConnected() ? RPGInventory.getEconomy().getBalance(player) : 0);
        if (balance < cost) {
            PlayerUtils.sendMessage(player, RPGInventory.getLanguage().getMessage("error.money", StringUtils.doubleToString(cost - balance)));
            return false;
        }

        return true;
    }

    public static void updateInventory(final Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                player.updateInventory();
            }
        }.runTaskLater(RPGInventory.getInstance(), 1);
    }

    public static void sendMessage(Player player, String message) {
        if (InventoryManager.playerIsLoaded(player)) {
            PlayerWrapper wrapper = InventoryManager.get(player);

            if (wrapper.getLastMessage().equals(message)) {
                return;
            }

            wrapper.setLastMessage(message);
        }

        player.sendMessage(message);
    }

    public enum LevelSystem {
        EXP("NONE"),
        SKILLAPI("SkillAPI"),
        BATTLELEVELS("BattleLevels"),
        SKILLS("Skills"),
        HEROES("Heroes"),
        RAC("RacesAndClasses"),
        RPGPL("RPGPlayerLeveling");

        private final String pluginName;

        LevelSystem(String pluginName) {
            this.pluginName = pluginName;
        }

        public String getPluginName() {
            return pluginName;
        }
    }

    public enum ClassSystem {
        //check skills pro class system
        PERMISSIONS("NONE"), SKILLAPI("SkillAPI"), SKILLS("Skills"), HEROES("Heroes"), RAC("RacesAndClasses");

        private final String pluginName;

        ClassSystem(String pluginName) {
            this.pluginName = pluginName;
        }

        public String getPluginName() {
            return pluginName;
        }
    }
}
