package ru.endlesscode.rpginventory.misc;

import org.bukkit.ChatColor;
import ru.endlesscode.rpginventory.RPGInventory;
import ru.endlesscode.rpginventory.configuration.misc.I18N;

public class I18NBukkit extends I18N {

    public I18NBukkit(RPGInventory instance) {
        super(instance.getDataFolder(), instance.getConfiguration().getLocale(), instance.getLogger());
    }

    @Override
    protected String stripColor(String message) {
        return ChatColor.stripColor(message);
    }

    @Override
    protected String translateCodes(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
