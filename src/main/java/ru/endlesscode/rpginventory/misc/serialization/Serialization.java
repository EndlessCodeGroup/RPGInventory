package ru.endlesscode.rpginventory.misc.serialization;

import org.bukkit.configuration.serialization.ConfigurationSerialization;

public class Serialization {

    public static void registerTypes() {
        ConfigurationSerialization.registerClass(InventorySnapshot.class);
        ConfigurationSerialization.registerClass(SlotSnapshot.class);
    }

}
