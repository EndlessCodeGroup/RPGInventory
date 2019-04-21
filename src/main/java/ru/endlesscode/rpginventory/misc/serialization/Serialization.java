package ru.endlesscode.rpginventory.misc.serialization;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.reader.ReaderException;
import ru.endlesscode.rpginventory.inventory.PlayerWrapper;
import ru.endlesscode.rpginventory.inventory.backpack.Backpack;
import ru.endlesscode.rpginventory.utils.FileUtils;
import ru.endlesscode.rpginventory.utils.Log;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Serialization {

    private static final String ROOT_TAG = "data";

    public static void registerTypes() {
        ConfigurationSerialization.registerClass(InventorySnapshot.class);
        ConfigurationSerialization.registerClass(SlotSnapshot.class);
        ConfigurationSerialization.registerClass(Backpack.class);
    }

    @Nullable
    public static PlayerWrapper loadPlayerOrNull(Player player, @NotNull Path file) {
        PlayerWrapper playerWrapper;
        try {
            playerWrapper = loadPlayer(player, file);
        } catch (IOException | InvalidConfigurationException e) {
            Log.d(e);
            FileUtils.resolveException(file);
            playerWrapper = null;
        }
        return playerWrapper;
    }

    @NotNull
    private static PlayerWrapper loadPlayer(Player player, @NotNull Path file)
            throws IOException, InvalidConfigurationException {
        PlayerWrapper playerWrapper;
        try {
            InventorySnapshot inventorySnapshot = (InventorySnapshot) load(file);
            playerWrapper = inventorySnapshot.restore(player);
        } catch (InvalidConfigurationException e) {
            if (e.getCause() instanceof ReaderException) {
                Log.w("Can''t load {0}''s inventory. Trying to use legacy loader...", player.getName());
                playerWrapper = LegacySerialization.loadPlayer(player, file);
            } else {
                throw e;
            }
        }
        return playerWrapper;
    }

    public static Backpack loadBackpack(@NotNull Path file) throws IOException, InvalidConfigurationException {
        Backpack backpack;
        try {
            backpack = (Backpack) load(file);
        } catch (InvalidConfigurationException e) {
            if (e.getCause() instanceof ReaderException) {
                Log.w("Can''t load backpack {0}. Trying to use legacy loader...", file.getFileName().toString());
                backpack = LegacySerialization.loadBackpack(file);
            } else {
                throw e;
            }
        }

        return backpack;
    }

    public static void save(@NotNull Object data, @NotNull Path file) throws IOException {
        final FileConfiguration serializedData = new YamlConfiguration();
        serializedData.set(ROOT_TAG, data);

        Path tempFile = Files.createTempFile(file.getParent(), file.getFileName().toString(), null);
        try (OutputStream stream = new GZIPOutputStream(Files.newOutputStream(tempFile))) {
            stream.write(serializedData.saveToString().getBytes(StandardCharsets.UTF_8));
        }
        Files.move(tempFile, file, StandardCopyOption.REPLACE_EXISTING);
    }

    @NotNull
    private static Object load(@NotNull Path file)
            throws IOException, InvalidConfigurationException {
        final FileConfiguration serializedData = new YamlConfiguration();
        try (InputStreamReader reader = new InputStreamReader(new GZIPInputStream(Files.newInputStream(file)))) {
            serializedData.load(reader);
        }

        return Objects.requireNonNull(serializedData.get(ROOT_TAG), "Serialized data not found");
    }
}
