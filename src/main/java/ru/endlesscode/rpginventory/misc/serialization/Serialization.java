package ru.endlesscode.rpginventory.misc.serialization;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.endlesscode.rpginventory.inventory.PlayerWrapper;
import ru.endlesscode.rpginventory.utils.FileUtils;
import ru.endlesscode.rpginventory.utils.Log;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class Serialization {

    private static final String INV = "inventory";

    public static void registerTypes() {
        ConfigurationSerialization.registerClass(InventorySnapshot.class);
        ConfigurationSerialization.registerClass(SlotSnapshot.class);
    }

    public static void savePlayer(@NotNull PlayerWrapper playerWrapper, @NotNull Path file) throws IOException {
        final YamlConfiguration serializedInventory = new YamlConfiguration();
        serializedInventory.set(INV, InventorySnapshot.create(playerWrapper));

        Path tempFile = Files.createTempFile(file.getParent(), file.getFileName().toString(), null);
        try (OutputStream stream = Files.newOutputStream(tempFile)) {
            stream.write(serializedInventory.saveToString().getBytes());
        }
        Files.move(tempFile, file, StandardCopyOption.REPLACE_EXISTING);
    }

    @Nullable
    public static PlayerWrapper loadPlayerOrNull(Player player, @NotNull Path file) {
        try {
            try {
                return loadPlayer(player, file);
            } catch (InvalidConfigurationException e) {
                Log.w("Can''t load {0}''s inventory. Trying to use legacy loader.", player.getName());
                return LegacySerialization.loadPlayer(player, file);
            }
        } catch (IOException e) {
            Log.d(e);
            FileUtils.resolveException(file);
            return null;
        } catch (Exception e) {
            Log.d(e);
            e.printStackTrace();
            return null;
        }
    }

    @NotNull
    private static PlayerWrapper loadPlayer(Player player, @NotNull Path file) throws IOException, InvalidConfigurationException {
        final YamlConfiguration serializedInventory = new YamlConfiguration();
        try (InputStreamReader reader = new InputStreamReader(Files.newInputStream(file))) {
            serializedInventory.load(reader);
        }

        InventorySnapshot inventorySnapshot = (InventorySnapshot) serializedInventory.get(INV);
        return inventorySnapshot.restore(player);
    }

}
