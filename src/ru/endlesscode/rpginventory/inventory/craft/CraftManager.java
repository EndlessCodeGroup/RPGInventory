package ru.endlesscode.rpginventory.inventory.craft;

import com.comphenix.protocol.ProtocolLibrary;
import org.bukkit.configuration.MemorySection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.endlesscode.rpginventory.RPGInventory;
import ru.endlesscode.rpginventory.event.listener.CraftListener;
import ru.endlesscode.rpginventory.misc.Config;
import ru.endlesscode.rpginventory.utils.ItemUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by OsipXD on 29.08.2016
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
public class CraftManager {
    private static List<CraftExtension> EXTENSIONS = new ArrayList<>();
    private static ItemStack capItem;

    private CraftManager() {}

    public static void init() {
        MemorySection config = (MemorySection) Config.getConfig().get("craft");
        if (!config.getBoolean("enabled") || !config.contains("extensions")) {
            return;
        }

        capItem = ItemUtils.getTexturedItem(config.getString("extendable"));

        Set<String> extensionNames = config.getConfigurationSection("extensions").getKeys(false);
        for (String extensionName : extensionNames) {
            EXTENSIONS.add(new CraftExtension(extensionName, config.getConfigurationSection("extensions." + extensionName)));
        }

        // Register listeners
        ProtocolLibrary.getProtocolManager().addPacketListener(new CraftListener(RPGInventory.getInstance()));
    }

    public static List<CraftExtension> getExtensions(Player player) {
        List<CraftExtension> extensions = new ArrayList<>(EXTENSIONS);
        for (CraftExtension extension : EXTENSIONS) {
            if (extension.isUnlockedForPlayer(player)) {
                extension.registerExtension(extensions);
            }
        }

        return extensions;
    }

    static ItemStack getCapItem() {
        return capItem;
    }

    static CraftExtension getByName(String childName) {
        for (CraftExtension extension : EXTENSIONS) {
            if (extension.getName().equals(childName)) {
                return extension;
            }
        }

        return null;
    }
}