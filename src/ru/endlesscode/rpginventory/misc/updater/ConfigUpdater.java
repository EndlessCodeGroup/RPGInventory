package ru.endlesscode.rpginventory.misc.updater;

import org.bukkit.configuration.file.FileConfiguration;
import ru.endlesscode.rpginventory.inventory.slot.SlotManager;
import ru.endlesscode.rpginventory.misc.Config;

/**
 * Created by OsipXD on 05.09.2015
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2015 © «EndlessCode Group»
 */
public class ConfigUpdater {
    public static void update(double configVersion) {
        FileConfiguration config = Config.getConfig();
        int version = (int) (configVersion * 10);

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
        }
    }
}
