package ru.endlesscode.rpginventory;

import com.comphenix.protocol.ProtocolLibrary;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import ru.endlesscode.rpginventory.event.listener.*;
import ru.endlesscode.rpginventory.inventory.InventoryLocker;
import ru.endlesscode.rpginventory.inventory.InventoryManager;
import ru.endlesscode.rpginventory.inventory.backpack.BackpackManager;
import ru.endlesscode.rpginventory.inventory.craft.CraftManager;
import ru.endlesscode.rpginventory.inventory.slot.SlotManager;
import ru.endlesscode.rpginventory.item.ItemManager;
import ru.endlesscode.rpginventory.misc.Config;
import ru.endlesscode.rpginventory.misc.FileLanguage;
import ru.endlesscode.rpginventory.misc.metrics.Metrics;
import ru.endlesscode.rpginventory.misc.updater.ConfigUpdater;
import ru.endlesscode.rpginventory.misc.updater.Updater;
import ru.endlesscode.rpginventory.nms.VersionHandler;
import ru.endlesscode.rpginventory.pet.PetManager;
import ru.endlesscode.rpginventory.pet.mypet.MyPetManager;
import ru.endlesscode.rpginventory.utils.PlayerUtils;
import ru.endlesscode.rpginventory.utils.StringUtils;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Created by OsipXD on 18.08.2015.
 * It is a part of the RPGInventory.
 * Copyright © 2015 «EndlessCode Group»
 */
public class RPGInventory extends JavaPlugin {
    private static RPGInventory instance;
    private static FileLanguage language;

    private static Permission perms;
    private static Economy economy;

    private static PlayerUtils.LevelSystem levelSystem;
    private static PlayerUtils.ClassSystem classSystem;
    private static boolean pApiHooked;

    public static RPGInventory getInstance() {
        return instance;
    }

    public static FileLanguage getLanguage() {
        return language;
    }

    public static Permission getPermissions() {
        return perms;
    }

    public static Economy getEconomy() {
        return economy;
    }

    @Contract(pure = true)
    public static Logger getPluginLogger() {
        return instance.getLogger();
    }

    @Contract(pure = true)
    public static boolean economyConnected() {
        return economy != null;
    }

    @Contract(pure = true)
    public static boolean placeholderApiHooked() {
        return pApiHooked;
    }

    public static PlayerUtils.LevelSystem getLevelSystem() {
        return levelSystem;
    }

    public static PlayerUtils.ClassSystem getClassSystem() {
        return classSystem;
    }

    @Override
    public void onEnable() {
        instance = this;

        Config.loadConfig(this);
        this.updateConfig();
        language = new FileLanguage(this);

        if (!this.checkRequirements()) {
            this.getPluginLoader().disablePlugin(this);
            return;
        }

        // Hook Placeholder API
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new StringUtils.Placeholders().hook();
            pApiHooked = true;
            this.getLogger().info("Placeholder API hooked!");
        } else {
            pApiHooked = false;
        }

        // Load modules
        this.getLogger().info(CraftManager.init(this) ? "Craft extensions is enabled." : "Craft extensions isn't loaded.");
        this.getLogger().info(InventoryLocker.init(this) ? "Inventory lock system is enabled." : "Inventory lock system isn't loaded.");
        this.getLogger().info(ItemManager.init(this) ? "Item system is enabled." : "Item system isn't loaded.");
        this.getLogger().info(PetManager.init(this) ? "Pet system is enabled." : "Pet system isn't loaded.");
        this.getLogger().info(BackpackManager.init(this) ? "Backpack system is enabled." : "Backpack system isn't loaded.");

        // Hook MyPet
        if (Bukkit.getPluginManager().isPluginEnabled("MyPet") && MyPetManager.init(this)) {
            this.getLogger().info("MyPet hooked!");
        }

        // Registering other listeners
        PluginManager pm = this.getServer().getPluginManager();
        pm.registerEvents(new ArmorEquipListener(), this);
        pm.registerEvents(new HandSwitchListener(), this);
        pm.registerEvents(new PlayerListener(), this);
        pm.registerEvents(new WorldListener(), this);

        if (SlotManager.getSlotManager().getElytraSlot() != null) {
            pm.registerEvents(new ElytraListener(), this);
        }

        ProtocolLibrary.getProtocolManager().addPacketListener(new PlayerLoader(this));

        this.loadPlayers();
        this.startMetrics();

        // Enable commands
        this.getCommand("rpginventory").setExecutor(new RPGInventoryCommandExecutor());

        this.checkUpdates(null);
    }

    private boolean checkRequirements() {
        // Check if plugin is enabled
        if (!Config.getConfig().getBoolean("enabled")) {
            this.onFirstStart();
            this.getLogger().warning("Plugin is not enabled in config!");
            this.setEnabled(false);
            return false;
        }

        // Check version compatibility
        if (!VersionHandler.checkVersion()) {
            this.getLogger().warning("[RPGInventory] This version of RPG Inventory is not tested with \"" + Bukkit.getBukkitVersion() + "\"!");
        }

        // Check resource-pack settings
        if (Config.getConfig().getString("resource-pack.url").equals("PUT_YOUR_URL_HERE")) {
            this.getLogger().warning("Set resource-pack's url in config!");
            this.getPluginLoader().disablePlugin(this);
            return false;
        }

        if (Config.getConfig().getString("resource-pack.hash").equals("PUT_YOUR_HASH_HERE")) {
            this.getLogger().warning("Your resource pack hash incorrect!");
        }

        // Check dependencies
        if (this.setupPermissions()) {
            this.getLogger().info("Permissions hooked: " + perms.getName());
        } else {
            this.getLogger().warning("Permissions not found!");
            return false;
        }

        if (this.setupEconomy()) {
            this.getLogger().info("Economy hooked: " + economy.getName());
        } else {
            this.getLogger().warning("Economy not found!");
        }

        levelSystem = PlayerUtils.LevelSystem.valueOf(Config.getConfig().getString("level-system"));
        classSystem = PlayerUtils.ClassSystem.valueOf(Config.getConfig().getString("class-system"));

        PluginManager pm = this.getServer().getPluginManager();
        if (levelSystem != PlayerUtils.LevelSystem.EXP && !pm.isPluginEnabled(levelSystem.getPluginName())) {
            this.getLogger().warning("Level-system " + levelSystem.getPluginName() + " is not enabled!");
            return false;
        }

        if (classSystem != PlayerUtils.ClassSystem.PERMISSIONS && !pm.isPluginEnabled(classSystem.getPluginName())) {
            this.getLogger().warning("Class-system " + classSystem.getPluginName() + " is not enabled!");
            return false;
        }

        return InventoryManager.init(this) && SlotManager.init();
    }

    @Override
    public void onDisable() {
        this.savePlayers();
    }

    private void startMetrics() {
        try {
            Metrics metrics = new Metrics(this);
            metrics.start();
        } catch (IOException ignored) {
        }
    }

    private void savePlayers() {
        if (this.getServer().getOnlinePlayers().size() == 0) {
            return;
        }

        this.getLogger().info("Saving players inventories...");
        for (Player player : this.getServer().getOnlinePlayers()) {
            InventoryManager.unloadPlayerInventory(player);
        }
    }

    private void loadPlayers() {
        if (this.getServer().getOnlinePlayers().size() == 0) {
            return;
        }

        this.getLogger().info("Loading players inventories...");
        for (Player player : this.getServer().getOnlinePlayers()) {
            InventoryManager.loadPlayerInventory(player);
        }
    }

    private void onFirstStart() {
        StringUtils.coloredConsole(RPGInventory.getLanguage().getCaption("firststart"));
    }

    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(Permission.class);

        if (permissionProvider == null) {
            return perms != null;
        }

        perms = permissionProvider.getProvider();
        return perms != null;
    }

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> rsp = this.getServer().getServicesManager().getRegistration(Economy.class);

        if (rsp == null) {
            return false;
        }

        economy = rsp.getProvider();
        return economy != null;
    }

    public void checkUpdates(@Nullable final Player player) {
        if (!Config.getConfig().getBoolean("auto-update")) {
            return;
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                Updater updater = new Updater(RPGInventory.instance, RPGInventory.instance.getFile(), Updater.UpdateType.NO_DOWNLOAD, false);
                if (updater.getResult() == Updater.UpdateResult.UPDATE_AVAILABLE) {
                    String[] lines = {
                            StringUtils.coloredLine("&3=================&b[&eRPGInventory&b]&3==================="),
                            StringUtils.coloredLine("&6New version available: &a" + updater.getLatestName() + "&6!"),
                            StringUtils.coloredLine(updater.getDescription()),
                            StringUtils.coloredLine("&6Changelog: &e" + updater.getInfoLink()),
                            StringUtils.coloredLine("&6Download it on &eSpigotMC&6!"),
                            StringUtils.coloredLine("&3==================================================")
                    };

                    for (String line : lines) {
                        if (player == null) {
                            StringUtils.coloredConsole(line);
                        } else {
                            PlayerUtils.sendMessage(player, line);
                        }
                    }
                }
            }
        }.runTaskAsynchronously(RPGInventory.getInstance());
    }

    private void updateConfig() {
        if (!Config.getConfig().contains("version")) {
            Config.getConfig().set("version", this.getDescription().getVersion());
            Config.save();
            return;
        }

        Double currentVersion = Double.parseDouble(this.getDescription().getVersion().replaceFirst("\\.", ""));
        Double configVersion = Double.parseDouble(Config.getConfig().getString("version").replaceFirst("\\.", ""));

        if (configVersion < currentVersion) {
            ConfigUpdater.update(configVersion);
            Config.getConfig().set("version", null);
            Config.getConfig().set("version", this.getDescription().getVersion());
            Config.save();
            Config.reload();
        }
    }
}
