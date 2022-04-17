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

import com.comphenix.protocol.ProtocolLibrary;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.endlesscode.inspector.bukkit.command.TrackedCommandExecutor;
import ru.endlesscode.inspector.bukkit.plugin.PluginLifecycle;
import ru.endlesscode.inspector.bukkit.scheduler.TrackedBukkitRunnable;
import ru.endlesscode.mimic.Mimic;
import ru.endlesscode.mimic.MimicApiLevel;
import ru.endlesscode.mimic.classes.BukkitClassSystem;
import ru.endlesscode.mimic.level.BukkitLevelSystem;
import ru.endlesscode.rpginventory.compat.VersionHandler;
import ru.endlesscode.rpginventory.compat.mimic.RPGInventoryItemsRegistry;
import ru.endlesscode.rpginventory.compat.mypet.MyPetManager;
import ru.endlesscode.rpginventory.event.listener.*;
import ru.endlesscode.rpginventory.inventory.InventoryLocker;
import ru.endlesscode.rpginventory.inventory.InventoryManager;
import ru.endlesscode.rpginventory.inventory.backpack.BackpackManager;
import ru.endlesscode.rpginventory.inventory.craft.CraftManager;
import ru.endlesscode.rpginventory.inventory.slot.SlotManager;
import ru.endlesscode.rpginventory.item.ItemManager;
import ru.endlesscode.rpginventory.misc.FileLanguage;
import ru.endlesscode.rpginventory.misc.Updater;
import ru.endlesscode.rpginventory.misc.config.Config;
import ru.endlesscode.rpginventory.misc.config.ConfigUpdater;
import ru.endlesscode.rpginventory.misc.serialization.Serialization;
import ru.endlesscode.rpginventory.pet.PetManager;
import ru.endlesscode.rpginventory.resourcepack.ResourcePackModule;
import ru.endlesscode.rpginventory.utils.Log;
import ru.endlesscode.rpginventory.utils.PlayerUtils;
import ru.endlesscode.rpginventory.utils.StringUtils;
import ru.endlesscode.rpginventory.utils.Version;

import java.nio.file.Path;

public class RPGInventory extends PluginLifecycle {
    private static RPGInventory instance;

    private Permission perms;
    private Economy economy;

    private Mimic mimic;

    private FileLanguage language;
    private boolean placeholderApiHooked = false;
    private boolean myPetHooked = false;
    private ResourcePackModule resourcePackModule = null;

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
    public static boolean economyConnected() {
        return instance.economy != null;
    }

    @Contract(pure = true)
    public static boolean isPlaceholderApiHooked() {
        return instance.placeholderApiHooked;
    }

    @Contract(pure = true)
    public static boolean isMyPetHooked() {
        return instance.myPetHooked;
    }

    public static BukkitLevelSystem getLevelSystem(@NotNull Player player) {
        return instance.mimic.getLevelSystem(player);
    }

    public static BukkitClassSystem getClassSystem(@NotNull Player player) {
        return instance.mimic.getClassSystem(player);
    }

    @Nullable
    public static ResourcePackModule getResourcePackModule() {
        return instance.resourcePackModule;
    }

    @Override
    public void init() {
        instance = this;
        Log.init(this.getLogger());
        Config.init(this);
    }

    @Override
    public void onLoad() {
        if (checkMimic()) {
            mimic = Mimic.getInstance();
            mimic.registerItemsRegistry(new RPGInventoryItemsRegistry(), MimicApiLevel.VERSION_0_7, this, ServicePriority.High);
        }
    }

    @Override
    public void onEnable() {
        if (!initMimicSystems()) {
            return;
        }

        loadConfigs();
        Serialization.registerTypes();

        if (!this.checkRequirements()) {
            this.getPluginLoader().disablePlugin(this);
            return;
        }

        PluginManager pm = this.getServer().getPluginManager();

        // Hook Placeholder API
        if (pm.isPluginEnabled("PlaceholderAPI")) {
            new StringUtils.Placeholders().register();
            placeholderApiHooked = true;
            Log.i("Placeholder API hooked!");
        } else {
            placeholderApiHooked = false;
        }

        loadModules();

        this.loadPlayers();
        this.startMetrics();

        // Enable commands
        this.getCommand("rpginventory")
                .setExecutor(new TrackedCommandExecutor(new RPGInventoryCommandExecutor(), getReporter()));

        this.checkUpdates(null);
    }

    private boolean initMimicSystems() {
        boolean isMimicFound = checkMimic();
        if (isMimicFound) {
            BukkitLevelSystem.Provider levelSystemProvider = mimic.getLevelSystemProvider();
            Log.i("Level system ''{0}'' found.", levelSystemProvider.getId());
            BukkitClassSystem.Provider classSystemProvider = mimic.getClassSystemProvider();
            Log.i("Class system ''{0}'' found.", classSystemProvider.getId());
        } else {
            Log.s("Mimic is required for RPGInventory to use levels and classes from other RPG plugins!");
            Log.s("Download it from SpigotMC: https://www.spigotmc.org/resources/82515/");
            getServer().getPluginManager().disablePlugin(this);
        }
        return isMimicFound;
    }

    void reload() {
        // Unload
        saveData();
        removeListeners();

        // Load
        loadConfigs();
        loadModules();
        loadPlayers();
    }

    private void loadConfigs() {
        this.updateConfig();
        Config.reload();
        language = new FileLanguage(this);
    }

    private void loadModules() {
        PluginManager pm = getServer().getPluginManager();

        // Hook MyPet
        if (pm.isPluginEnabled("MyPet") && MyPetManager.init(this)) {
            myPetHooked = true;
            Log.i("MyPet used as pet system");
        } else {
            myPetHooked = false;
            Log.i(PetManager.init(this) ? "Pet system is enabled" : "Pet system isn''t loaded");
        }

        // Load modules
        Log.i(CraftManager.init(this) ? "Craft extensions is enabled" : "Craft extensions isn''t loaded");
        Log.i(InventoryLocker.init(this) ? "Inventory lock system is enabled" : "Inventory lock system isn''t loaded");
        Log.i(ItemManager.init(this) ? "Item system is enabled" : "Item system isn''t loaded");
        Log.i(BackpackManager.init(this) ? "Backpack system is enabled" : "Backpack system isn''t loaded");

        // Registering other listeners
        pm.registerEvents(new ArmorEquipListener(), this);
        pm.registerEvents(new HandSwapListener(), this);
        pm.registerEvents(new PlayerListener(), this);
        pm.registerEvents(new WorldListener(), this);

        if (SlotManager.instance().getElytraSlot() != null) {
            pm.registerEvents(new ElytraListener(), this);
        }
        this.resourcePackModule = ResourcePackModule.init(this);
    }

    private void removeListeners() {
        ProtocolLibrary.getProtocolManager().removePacketListeners(this);
        HandlerList.unregisterAll(this);
    }

    private boolean checkMimic() {
        if (getServer().getPluginManager().getPlugin("Mimic") == null) {
            return false;
        } else if (MimicApiLevel.checkApiLevel(MimicApiLevel.VERSION_0_8)) {
            return true;
        } else {
            Log.w("At least Mimic 0.8 required for RPGInventory.");
            return false;
        }
    }

    private boolean checkRequirements() {
        // Check if plugin is enabled
        if (!Config.getConfig().getBoolean("enabled")) {
            Log.w("RPGInventory is disabled in the config!");
            return false;
        }

        // Check version compatibility
        if (VersionHandler.isNotSupportedVersion()) {
            Log.w("This version of RPG Inventory is not tested with \"{0}\"!", Bukkit.getBukkitVersion());
        } else if (VersionHandler.isExperimentalSupport()) {
            Log.w("Support of {0} is experimental! Use RPGInventory with caution.", Bukkit.getBukkitVersion());
        }

        // Check dependencies
        if (this.setupPermissions()) {
            Log.i("Permissions hooked: {0}", perms.getName());
        } else {
            Log.s("Permissions not found!");
            return false;
        }

        if (this.setupEconomy()) {
            Log.i("Economy hooked: {0}", economy.getName());
        } else {
            Log.w("Economy not found!");
        }

        return InventoryManager.init(this) && SlotManager.init();
    }

    @Override
    public void onDisable() {
        saveData();
    }

    private void saveData() {
        BackpackManager.saveBackpacks();
        this.savePlayers();
    }

    private void startMetrics() {
        new Metrics(holder, 4210);
    }

    private void savePlayers() {
        if (this.getServer().getOnlinePlayers().size() == 0) {
            return;
        }

        Log.i("Saving players inventories...");
        for (Player player : this.getServer().getOnlinePlayers()) {
            InventoryManager.unloadPlayerInventory(player);
        }
    }

    private void loadPlayers() {
        if (this.getServer().getOnlinePlayers().size() == 0) {
            return;
        }

        Log.i("Loading players inventories...");
        for (Player player : this.getServer().getOnlinePlayers()) {
            InventoryManager.loadPlayerInventory(player);
        }
    }

    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(Permission.class);
        if (permissionProvider != null) {
            perms = permissionProvider.getProvider();
        }

        return perms != null;
    }

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> rsp = this.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp != null) {
            economy = rsp.getProvider();
        }

        return economy != null;
    }

    public void checkUpdates(@Nullable final Player player) {
        if (!Config.getConfig().getBoolean("check-update")) {
            return;
        }

        new TrackedBukkitRunnable() {
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
        final Version version = Version.parseVersion(this.getDescription().getVersion());

        if (!Config.getConfig().contains("version")) {
            Config.getConfig().set("version", version.toString());
            Config.save();
            return;
        }

        final Version configVersion = Version.parseVersion(Config.getConfig().getString("version"));
        if (version.compareTo(configVersion) > 0) {
            ConfigUpdater.update(configVersion);
            Config.getConfig().set("version", null);
            Config.getConfig().set("version", version.toString());
            Config.save();
        }
    }

    @NotNull
    public Path getDataPath() {
        return getDataFolder().toPath();
    }
}
