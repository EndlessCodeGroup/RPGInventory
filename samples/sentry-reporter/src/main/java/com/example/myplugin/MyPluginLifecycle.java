package com.example.myplugin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import ru.endlesscode.inspector.bukkit.plugin.PluginLifecycle;

public class MyPluginLifecycle extends PluginLifecycle {

    @Override
    public void onEnable() {
        getCommand("report").setExecutor(track(new ReportCommandExecutor()));
    }

    public class ReportCommandExecutor implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
            throw new RuntimeException("Exceptions from commands should be reported to Sentry.");
        }
    }
}
