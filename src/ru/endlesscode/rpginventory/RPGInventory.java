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
import ru.endlesscode.rpginventory.inventory.ResourcePackManager;
import ru.endlesscode.rpginventory.inventory.backpack.BackpackManager;
import ru.endlesscode.rpginventory.inventory.chest.ChestManager;
import ru.endlesscode.rpginventory.inventory.mypet.MyPetManager;
import ru.endlesscode.rpginventory.inventory.slot.SlotManager;
import ru.endlesscode.rpginventory.item.ItemManager;
import ru.endlesscode.rpginventory.misc.Config;
import ru.endlesscode.rpginventory.misc.FileLanguage;
import ru.endlesscode.rpginventory.misc.metrics.Metrics;
import ru.endlesscode.rpginventory.misc.updater.ConfigUpdater;
import ru.endlesscode.rpginventory.misc.updater.Updater;
import ru.endlesscode.rpginventory.nms.VersionHandler;
import ru.endlesscode.rpginventory.pet.PetManager;
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

        // Check flag `enabled`
        if (!Config.getConfig().getBoolean("enabled")) {
            this.onFirstStart();
            this.getLogger().warning("Plugin is not enabled in config!");
            this.setEnabled(false);
            return;
        }

        if (!VersionHandler.checkVersion()) {
            this.getLogger().warning("This version of RPG Inventory is not compatible with your version of Bukkit!");
            this.getPluginLoader().disablePlugin(this);
            return;
        }

        if (!this.checkDependencies()) {
            this.getPluginLoader().disablePlugin(this);
            return;
        }

        // Registering of listeners
        PluginManager pm = this.getServer().getPluginManager();
        pm.registerEvents(new ArmorEquipListener(), this);
        pm.registerEvents(new InventoryListener(), this);
        pm.registerEvents(new BackpackListener(), this);
        pm.registerEvents(new LockerListener(), this);
        pm.registerEvents(new WorldListener(), this);
        pm.registerEvents(new ItemListener(), this);

        if (VersionHandler.is1_9()) {
            pm.registerEvents(new HandSwitchListener(), this);
            if (SlotManager.getSlotManager().getElytraSlot() != null) {
                pm.registerEvents(new ElytraListener(), this);
            }
        }

        if (!VersionHandler.is1_7_R4()) {
            ProtocolLibrary.getProtocolManager().addPacketListener(new ResourcePackListener(this));
        }

        // Initialization
        ResourcePackManager.init();
        InventoryManager.init();
        InventoryLocker.init();
        BackpackManager.init();
        ChestManager.init();
        ItemManager.init();
        SlotManager.init();

        // Enabling pets
        if (PetManager.isEnabled()) {
            pm.registerEvents(new PetListener(), this);
            PetManager.init();
        }

        // Check resource-pack settings
        if (ResourcePackManager.getMode() != ResourcePackManager.Mode.DISABLED) {
            if (Config.getConfig().getString("resource-pack.url").equals("PUT_YOUR_URL_HERE")) {
                this.getLogger().warning("Set resource-pack's url in config or set resource-pack.mode to DISABLED!");
                this.getPluginLoader().disablePlugin(this);
                return;
            }

            if (Config.getConfig().getString("resource-pack.hash").equals("PUT_YOUR_HASH_HERE")) {
                this.getLogger().warning("Your resource pack hash incorrect!");
            }
        }

        if (ResourcePackManager.getMode() == ResourcePackManager.Mode.EXPERIMENTAL && !VersionHandler.isHigher1_9()) {
            this.getLogger().warning("EXPERIMENTAL resource pack allowed only on minecraft 1.9+!");
            this.getPluginLoader().disablePlugin(this);
            return;
        }

        if (ResourcePackManager.getMode() != ResourcePackManager.Mode.FORCE && ResourcePackManager.getMode() != ResourcePackManager.Mode.EXPERIMENTAL
                && Config.getConfig().getBoolean("alternate-view.use-item")) {
            pm.registerEvents(new InventoryOpenItemListener(), this);
        }

        if (ResourcePackManager.getMode() != ResourcePackManager.Mode.EXPERIMENTAL && ResourcePackManager.getMode() != ResourcePackManager.Mode.DISABLED) {
            pm.registerEvents(new ChestListener(), this);
        }

        if (Bukkit.getPluginManager().isPluginEnabled("MyPet")) {
            pm.registerEvents(new MyPetManager(), this);
            MyPetManager.init();
            this.getLogger().info("MyPet hooked!");
        }

        this.loadPlayers();
        this.startMetrics();

        // Enable commands
        this.getCommand("rpginventory").setExecutor(new RPGInventoryCommandExecutor());

        this.checkUpdates(null);
    }

    private boolean checkDependencies() {
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

        return true;
    }

    @Override
    public void onDisable() {
        ResourcePackManager.save();
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
                            player.sendMessage(line);
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
