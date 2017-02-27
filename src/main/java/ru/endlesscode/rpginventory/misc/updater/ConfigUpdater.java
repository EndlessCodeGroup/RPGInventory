package ru.endlesscode.rpginventory.misc.updater;

import org.bukkit.configuration.file.FileConfiguration;
import ru.endlesscode.rpginventory.inventory.slot.SlotManager;
import ru.endlesscode.rpginventory.misc.Config;

import java.util.Arrays;
import java.util.Collections;

/**
 * Created by OsipXD on 05.09.2015
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
public class ConfigUpdater {
    public static void update(double configVersion) {
        FileConfiguration config = Config.getConfig();
        int version = (int) (configVersion*10);

        switch (version) {
            case 91:
                config.set("ids", null);
                config.set("attack.auto-held", true);
            case 100:
                config.set("slots.pet", null);
                config.set("slots.crafting", null);
                config.set("slots.enabled", true);
                config.set("slots.level.spend", false);
                SlotManager.getSlotManager().saveDefaults();
            case 101:
            case 102:
            case 103:
            case 104:
            case 105:
            case 106:
                config.set("alternate-view.enabled", false);
                config.set("alternate-view.fill", "STAINED_GLASS_PANE:0");
            case 110:
            case 111:
            case 112:
            case 113:
                config.set("alternate-view.item", "ENCHANTED_BOOK");
                config.set("alternate-view.slot", 8);
                config.set("alternate-view.name", "&6Equipment");
                config.set("alternate-view.lore", "&7&o(Right click to open equipment)");
            case 114:
                if (config.getInt("alternate-view.slot") == 9) {
                    config.set("alternate-view.slot", 8);
                }
                config.set("resource-pack.mode", config.getBoolean("alternate-view.enabled") ? "DISABLED" : "AUTO");
                config.set("resource-pack.url", "PUT_YOUR_URL_HERE");
                config.set("resource-pack.hash", "PUT_YOUR_HASH_HERE");
                config.set("alternate-view.enabled", null);
                config.set("alternate-view.use-item", true);
            case 115:
            case 116:
            case 117:
                config.set("alternate-view.enable-craft", true);
                config.set("worlds.mode", "BLACKLIST");
                config.set("worlds.list", new String[]{"blocked_world"});
            case 118:
            case 119:
            case 120:
            case 121:
            case 122:
            case 123:
                config.set("backpacks.expiration-time", 30);
            case 124:
                config.set("level-system", "EXP");
                config.set("class-system", "PERMISSIONS");
                config.set("metrics", true);
            case 125:
            case 126:
                config.set("items.lore-pattern", new String[]{
                        "_UNBREAKABLE_",
                        "_DROP_",
                        "_LEVEL_",
                        "_CLASS_",
                        "_SEPARATOR_",
                        "_LORE_",
                        "_SEPARATOR_",
                        "_SKILLS_",
                        "_SEPARATOR_",
                        "_STATS_"
                });
                config.set("items.separator", "");
                config.set("auto-update", false);
            case 127:
            case 128:
            case 129:
                config.set("auto-update", true);
            case 130:
            case 131:
            case 132:
                config.set("health.base", 20);
                config.set("health.scale", false);
                config.set("health.hearts", 20);
                config.set("health.heart-capacity.min", 1);
                config.set("health.heart-capacity.max", 5);
            case 133:
            case 134:
            case 135:
                // Removed RP settings
                config.set("resource-pack.fill", "DIAMOND_HOE:1");
                config.set("resource-pack.mode", null);
                config.set("alternate-view", null);
                config.set("containers.block", false);
                config.set("slots.locked", "DIAMOND_HOE:19");
                config.set("slots.buyable", "DIAMOND_HOE:18");

                // Added craft extensions
                config.set("craft.enabled", true);
                config.set("craft.extendable", "DIAMOND_HOE:0");
                config.set("craft.extensions.journeyman.name", "&aJourneyman slots");
                config.set("craft.extensions.journeyman.lore", "&eYou must be a journeyman to use it");
                config.set("craft.extensions.journeyman.slots", Arrays.asList(8, 9));
                config.set("craft.extensions.master.name", "&3Master slots");
                config.set("craft.extensions.master.lore", "&eYou must be a master to use it");
                config.set("craft.extensions.master.includes", Collections.singletonList("journeyman"));
                config.set("craft.extensions.master.slots", Arrays.asList(1, 4, 7));

                // Added join-messages
                config.set("join-messages.enabled", true);
                config.set("join-messages.delay", 3);
                config.set("join-messages.default.title", "&l&2Welcome to server!");
                config.set("join-messages.default.text", Arrays.asList("&6Glad to see you, &3%PLAYER%", "&6This server using &9RPGInventory"));
                config.set("join-messages.rp-info.title", "&l&4It is important!");
                config.set("join-messages.rp-info.text", Arrays.asList(
                        "&6You should &callow &6resource pack to play on this server",
                        "&6This will allow you fully immerse in the RPG atmosphere",
                        "&6But if you declined downloading of RP you can fix it...",
                        "&6Select the server in list, click &e'Edit' &6 and set &e'Resource-Pack: Accept'"));
            case 200:
                if ("&l&2Welcome to server!".equals(config.get("join-messages.rp-info.title"))) {
                    config.set("join-messages.rp-info.title", "&l&4It is important!");
                    config.set("join-messages.default.title", "&l&2Welcome to server!");
                }
            case 201:
            case 202:
            case 203:
                // Fixing join messages disabling
                config.set("join-messages.enabled", null);
                config.set("join-messages.rp-info.enabled", true);
                config.set("join-messages.default.enabled", true);

                // Added backpacks limit
                config.set("backpacks.limit", 1);

                // Added ability to disable extension for workbench
                config.set("craft.workbench", true);
                if ("DIAMOND_HOE:0".equals(config.get("craft.extendable"))) {
                    config.set("craft.extendable", "DIAMOND_HOE:27");
                }
            case 204:
            case 205:
            case 206:
            case 207:
                config.set("fill", config.get("resource-pack.fill"));
                config.set("resource-pack.fill", null);
                config.set("resource-pack.enabled", true);
        }
    }
}
