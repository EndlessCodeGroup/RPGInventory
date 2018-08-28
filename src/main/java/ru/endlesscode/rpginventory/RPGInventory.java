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

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.endlesscode.inspector.bukkit.plugin.PluginLifecycle;
import ru.endlesscode.rpginventory.event.listener.ArmorEquipListener;
import ru.endlesscode.rpginventory.event.listener.ElytraListener;
import ru.endlesscode.rpginventory.event.listener.HandSwapListener;
import ru.endlesscode.rpginventory.event.listener.PlayerListener;
import ru.endlesscode.rpginventory.event.listener.PlayerLoader;
import ru.endlesscode.rpginventory.event.listener.WorldListener;
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
import ru.endlesscode.rpginventory.utils.ResourcePackUtils;
import ru.endlesscode.rpginventory.utils.StringUtils;
import ru.endlesscode.rpginventory.utils.VersionUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Logger;

public class RPGInventory extends PluginLifecycle {
    private static RPGInventory instance;

    private Permission perms;
    private Economy economy;

    private PlayerUtils.LevelSystem levelSystem;
    private PlayerUtils.ClassSystem classSystem;

    private FileLanguage language;
    private boolean pApiHooked;

    public static RPGInventory getInstance() {
        return instance;
    }

    public static FileLanguage getLanguage() {
        return instance.language;
    }

    public static Permission getPermissions() {
        return instance.perms;
    }

    public static Economy getEconomy() {
        return instance.economy;
    }

    @Contract(pure = true)
    public static Logger getPluginLogger() {
        return instance.getLogger();
    }

    @Contract(pure = true)
    public static boolean economyConnected() {
        return instance.economy != null;
    }

    @Contract(pure = true)
    public static boolean placeholderApiHooked() {
        return instance.pApiHooked;
    }

    public static PlayerUtils.LevelSystem getLevelSystem() {
        return instance.levelSystem;
    }

    public static PlayerUtils.ClassSystem getClassSystem() {
        return instance.classSystem;
    }

    public RPGInventory() {
        super();

        instance = this;
    }

    @Override
    public void onEnable() {
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
        pm.registerEvents(new HandSwapListener(), this);
        pm.registerEvents(new PlayerListener(), this);
        pm.registerEvents(new WorldListener(), this);

        if (SlotManager.instance().getElytraSlot() != null) {
            pm.registerEvents(new ElytraListener(), this);
        }

        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        // Workaround for 1.12+
        if (VersionHandler.isUpper1_12()) {
            protocolManager.addPacketListener(
                    new PacketAdapter(this, PacketType.Play.Server.RECIPES) {
                        @Override
                        public void onPacketSending(@NotNull PacketEvent event) {
                            event.setCancelled(true);
                        }
                    });
            this.getLogger().info("Recipe book conflicts with RPGInventory and was disabled.");
        }

        protocolManager.addPacketListener(new PlayerLoader(this));

        this.loadPlayers();
        this.startMetrics();

        // Enable commands
        this.getCommand("rpginventory").setExecutor(new RPGInventoryCommandExecutor());

        this.checkUpdates(null);

        // Do this after all plugins loaded
        new BukkitRunnable() {
            @Override
            public void run() {
                checkThatSystemsLoaded();
            }
        }.runTask(this);
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
        if (Config.getConfig().getBoolean("resource-pack.enabled", true)) {
            String rpUrl = Config.getConfig().getString("resource-pack.url");
            if (rpUrl.equals("PUT_YOUR_URL_HERE")) {
                this.getLogger().warning("Set resource-pack's url in config!");
                this.getPluginLoader().disablePlugin(this);
                return false;
            }

            if (Config.getConfig().getString("resource-pack.hash").equals("PUT_YOUR_HASH_HERE")) {
                this.getLogger().warning("Your resource pack hash incorrect!");
            }

            try {
                ResourcePackUtils.validateUrl(rpUrl);
            } catch (Exception e) {
                String[] messageLines = e.getLocalizedMessage().split("\n");
                this.getLogger().warning("");
                this.getLogger().warning("######### Something wrong with your RP link! #########");
                for (String line : messageLines) {
                    this.getLogger().warning("# " + line);
                }
                this.getLogger().warning("######################################################");
                this.getLogger().warning("");
            }
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

        return InventoryManager.init(this) && SlotManager.init();
    }

    private void checkThatSystemsLoaded() {
        PluginManager pm = this.getServer().getPluginManager();
        if (levelSystem != PlayerUtils.LevelSystem.EXP && !pm.isPluginEnabled(levelSystem.getPluginName())) {
            this.getLogger().warning("Level-system " + levelSystem.getPluginName() + " is not enabled!");
            this.getLogger().warning("Will be used EXP by default");
            levelSystem = PlayerUtils.LevelSystem.EXP;
        }

        if (classSystem != PlayerUtils.ClassSystem.PERMISSIONS && !pm.isPluginEnabled(classSystem.getPluginName())) {
            this.getLogger().warning("Class-system " + classSystem.getPluginName() + " is not enabled!");
            this.getLogger().warning("Will be used PERMISSIONS by default");
            classSystem = PlayerUtils.ClassSystem.PERMISSIONS;
        }
    }

    @Override
    public void onDisable() {
        BackpackManager.saveBackpacks();
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
        StringUtils.coloredConsole(RPGInventory.getLanguage().getMessage("firststart"));
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
        if (!Config.getConfig().getBoolean("check-update")) {
            return;
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                Updater updater = new Updater(RPGInventory.instance, Updater.UpdateType.NO_DOWNLOAD);
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
        String fullVersion = this.getDescription().getVersion();
        String currentVersion = VersionUtils.trimQualifiers(fullVersion);

        if (!Config.getConfig().contains("version")) {
            Config.getConfig().set("version", currentVersion);
            Config.save();
            return;
        }

        int currentVersionCode = VersionUtils.versionToCode(currentVersion);
        int configVersionCode = VersionUtils.versionToCode(Config.getConfig().getString("version"));

        if (configVersionCode < currentVersionCode) {
            ConfigUpdater.update(configVersionCode);
            Config.getConfig().set("version", null);
            Config.getConfig().set("version", currentVersion);
            Config.save();
            Config.reload();
        }
    }

    @NotNull
    public Path getDataPath() {
        return getDataFolder().toPath();
    }
}
