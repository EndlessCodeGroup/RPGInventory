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
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.endlesscode.inspector.bukkit.command.TrackedCommandExecutor;
import ru.endlesscode.inspector.bukkit.plugin.PluginLifecycle;
import ru.endlesscode.inspector.bukkit.scheduler.TrackedBukkitRunnable;
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
import ru.endlesscode.rpginventory.misc.config.Config;
import ru.endlesscode.rpginventory.misc.FileLanguage;
import ru.endlesscode.rpginventory.misc.Metrics;
import ru.endlesscode.rpginventory.misc.config.ConfigUpdater;
import ru.endlesscode.rpginventory.misc.Updater;
import ru.endlesscode.rpginventory.compat.VersionHandler;
import ru.endlesscode.rpginventory.pet.PetManager;
import ru.endlesscode.rpginventory.pet.mypet.MyPetManager;
import ru.endlesscode.rpginventory.utils.Log;
import ru.endlesscode.rpginventory.utils.PlayerUtils;
import ru.endlesscode.rpginventory.utils.ResourcePackUtils;
import ru.endlesscode.rpginventory.utils.StringUtils;
import ru.endlesscode.rpginventory.utils.Version;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;

public class RPGInventory extends PluginLifecycle {
    private static RPGInventory instance;

    private Permission perms;
    private Economy economy;

    private PlayerUtils.LevelSystem levelSystem;
    private PlayerUtils.ClassSystem classSystem;

    private FileLanguage language;
    private boolean placeholderApiHooked = false;
    private boolean myPetHooked = false;

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

    public static PlayerUtils.LevelSystem getLevelSystem() {
        return instance.levelSystem;
    }

    public static PlayerUtils.ClassSystem getClassSystem() {
        return instance.classSystem;
    }

    @Override
    public void init() {
        instance = this;
        Log.init(this.getLogger());
        Config.init(this);
    }

    @Override
    public void onEnable() {
        this.updateConfig();
        Config.reload();
        language = new FileLanguage(this);

        if (!this.checkRequirements()) {
            this.getPluginLoader().disablePlugin(this);
            return;
        }

        // Hook Placeholder API
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new StringUtils.Placeholders().hook();
            placeholderApiHooked = true;
            Log.i("Placeholder API hooked!");
        } else {
            placeholderApiHooked = false;
        }

        // Load modules
        Log.i(CraftManager.init(this) ? "Craft extensions is enabled." : "Craft extensions isn't loaded.");
        Log.i(InventoryLocker.init(this) ? "Inventory lock system is enabled." : "Inventory lock system isn't loaded.");
        Log.i(ItemManager.init(this) ? "Item system is enabled." : "Item system isn't loaded.");
        Log.i(PetManager.init(this) ? "Pet system is enabled." : "Pet system isn't loaded.");
        Log.i(BackpackManager.init(this) ? "Backpack system is enabled." : "Backpack system isn't loaded.");

        // Hook MyPet
        if (Bukkit.getPluginManager().isPluginEnabled("MyPet") && MyPetManager.init(this)) {
            myPetHooked = true;
            Log.i("MyPet hooked!");
        } else {
            myPetHooked = false;
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
            Log.i("Recipe book conflicts with RPGInventory and was disabled.");
        }

        protocolManager.addPacketListener(new PlayerLoader(this));

        this.loadPlayers();
        this.startMetrics();

        // Enable commands
        this.getCommand("rpginventory").setExecutor(
                new TrackedCommandExecutor(new RPGInventoryCommandExecutor(), getReporter())
        );

        this.checkUpdates(null);

        // Do this after all plugins loaded
        new TrackedBukkitRunnable() {
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
            Log.w("Plugin is not enabled in config!");
            return false;
        }

        // Check version compatibility
        if (!VersionHandler.checkVersion()) {
            Log.w("This version of RPG Inventory is not tested with \"{0}\"!", Bukkit.getBukkitVersion());
        } else if (VersionHandler.is1_13()) {
            Log.w("Support of {0} is experimental! Use RPGInventory with caution.", Bukkit.getBukkitVersion());
        }

        // Check resource-pack settings
        if (Config.getConfig().getBoolean("resource-pack.enabled", true)) {
            String rpUrl = Config.getConfig().getString("resource-pack.url");
            if (rpUrl.equals("PUT_YOUR_URL_HERE")) {
                Log.w("Set resource-pack's url in config!");
                this.getPluginLoader().disablePlugin(this);
                return false;
            }

            if (Config.getConfig().getString("resource-pack.hash").equals("PUT_YOUR_HASH_HERE")) {
                Log.w("Your resource pack hash incorrect!");
            }

            try {
                ResourcePackUtils.validateUrl(rpUrl);
            } catch (Exception e) {
                String[] messageLines = e.getLocalizedMessage().split("\n");
                Log.w("");
                Log.w("######### Something wrong with your RP link! #########");
                for (String line : messageLines) {
                    Log.w("# {0}", line);
                }
                Log.w("######################################################");
                Log.w("");
            }
        }

        // Check dependencies
        if (this.setupPermissions()) {
            Log.i("Permissions hooked: {0}", perms.getName());
        } else {
            Log.w("Permissions not found!");
            return false;
        }

        if (this.setupEconomy()) {
            Log.i("Economy hooked: {0}", economy.getName());
        } else {
            Log.w("Economy not found!");
        }

        initLevelSystem();
        initClassSystem();

        return InventoryManager.init(this) && SlotManager.init();
    }

    private void initLevelSystem() {
        String levelSystemName = Config.getConfig().getString("level-system");
        try {
            levelSystem = PlayerUtils.LevelSystem.valueOf(levelSystemName);
        } catch (IllegalArgumentException e) {
            Log.w("Unknown level system: {0}. Used EXP by default.", levelSystemName);
            Log.w("Available level systems: {0}", Arrays.toString(PlayerUtils.LevelSystem.values()));
            levelSystem = PlayerUtils.LevelSystem.EXP;
        }
    }

    private void initClassSystem() {
        String classSystemName = Config.getConfig().getString("class-system");
        try {
            classSystem = PlayerUtils.ClassSystem.valueOf(classSystemName);
        } catch (IllegalArgumentException e) {
            Log.w("Unknown class system: {0}. Used PERMISSIONS by default.", classSystemName);
            Log.w("Available class systems: {0}", Arrays.toString(PlayerUtils.LevelSystem.values()));
            classSystem = PlayerUtils.ClassSystem.PERMISSIONS;
        }
    }

    private void checkThatSystemsLoaded() {
        PluginManager pm = this.getServer().getPluginManager();
        if (levelSystem != PlayerUtils.LevelSystem.EXP && !pm.isPluginEnabled(levelSystem.getPluginName())) {
            Log.w("Level-system {0} is not enabled!", levelSystem.getPluginName());
            Log.w("Will be used EXP by default");
            levelSystem = PlayerUtils.LevelSystem.EXP;
        }

        if (classSystem != PlayerUtils.ClassSystem.PERMISSIONS && !pm.isPluginEnabled(classSystem.getPluginName())) {
            Log.w("Class-system {0} is not enabled!", classSystem.getPluginName());
            Log.w("Will be used PERMISSIONS by default");
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
