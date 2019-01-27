package com.example.myplugin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import ru.endlesscode.inspector.bukkit.plugin.PluginLifecycle;
import ru.endlesscode.inspector.bukkit.scheduler.TrackedBukkitRunnable;

import java.util.logging.Logger;

public class MyPluginLifecycle extends PluginLifecycle {

    private Logger logger;

    @Override
    public void init() {
        // This is legal place to initialize plugin's fields
        this.logger = getLogger();
    }

    @Override
    public void onLoad() {
        logger.info("onLoad()");
    }

    @Override
    public void onEnable() {
        logger.info("onEnable()");

        // Let's register events
        getServer().getPluginManager().registerEvents(new BrokenListener(), this);

        // To track errors in commands we should wrap executor with TrackedCommandExecutor
        // You can use function track(...) for this.
        getCommand("crash").setExecutor(track(new BrokenCommandExecutor()));

        // You also can report anything manually
        getReporter().report("Manually reported exception.", new Exception());

        // And, finally, use TrackedBukkitRunnable instead of BukkitRunnable
        new TrackedBukkitRunnable() {
            @Override
            public void run() {
                throw new RuntimeException("Error from Runnable");
            }
        }.runTaskLater(this, 1L);
    }

    @Override
    public void onDisable() {
        logger.info("onDisable()");

        throw new RuntimeException("Ooops... The error should be reported!");
    }


    public class BrokenListener implements Listener {
        @EventHandler
        public void onPlayerDropItemThrowException(PlayerDropItemEvent event) {
            throw new RuntimeException("You should know about the exceptions.");
        }
    }


    public class BrokenCommandExecutor implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
            throw new RuntimeException("Exceptions from commands also should be reported.");
        }
    }
}
