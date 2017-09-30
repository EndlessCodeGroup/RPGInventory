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

package ru.endlesscode.rpginventory.misc.updater;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import ru.endlesscode.rpginventory.RPGInventory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

/**
 * Check for updates on EndlessCode for RPGInventory.
 * <p>
 * <b>VERY, VERY IMPORTANT</b>: Because there are no standards for adding auto-update toggles in your plugin's config, this system provides NO CHECK WITH YOUR CONFIG to make sure the user has allowed auto-updating.
 * <br>
 * It is a <b>BUKKIT POLICY</b> that you include a boolean value in your config that prevents the auto-updater from running <b>AT ALL</b>.
 * <br>
 * If you fail to include this option in your config, your plugin will be <b>REJECTED</b> when you attempt to submit it to dev.bukkit.org.
 * </p>
 * An example of a good configuration option would be something similar to 'auto-update: true' - if this value is set to false you may NOT run the auto-updater.
 * <br>
 * If you are unsure about these rules, please read the plugin submission guidelines: http://goo.gl/8iU5l
 *
 * @author Gravity
 * @version 2.3
 */

@SuppressWarnings("ALL")
public class Updater {

    /* Constants */

    // Remote file's title
    private static final String TITLE_VALUE = "name";
    // Remote file's download link
    private static final String LINK_VALUE = "fileUrl";
    // Remote file's release type
    private static final String TYPE_VALUE = "releaseType";
    // Remote file's build version
    private static final String VERSION_VALUE = "gameVersion";
    // Remote description
    private static final String DESCRIPTION_VALUE = "description";
    // Path to GET
    private static final String QUERY = "/updates/versions.json";
    // Slugs will be appended to this to get to the project's RSS feed
    private static final String HOST = "http://rpginventory.endlesscode.ru";
    // User-agent when querying Curse
    private static final String USER_AGENT = "RPGInventory";
    // Used for locating version numbers in file names
    private static final String DELIMITER = "^v|[\\s_-]v";
    // If the version number contains one of these, don't update.
    private static final String[] NO_UPDATE_TAG = {"-DEV", "-PRE", "-SNAPSHOT"};
    // Used for downloading files
    private static final int BYTE_SIZE = 1024;
    // Config key for api key
    private static final String API_KEY_CONFIG_KEY = "api-key";
    // Config key for disabling Updater
    private static final String DISABLE_CONFIG_KEY = "disable";
    // Default api key value in config
    private static final String API_KEY_DEFAULT = "PUT_API_KEY_HERE";
    // Default disable value in config
    private static final boolean DISABLE_DEFAULT = false;
    // Secret key
    private static final String SECRET_KEY = "BA8F94F2D6TF3ETR";

    /* User-provided variables */

    // Plugin running Updater
    private final Plugin plugin;
    // Type of update check to run
    private final UpdateType type;
    // Whether to announce file downloads
    private final boolean announce;
    // The plugin file (jar)
    private final File file;
    // The folder that downloads will be placed in
    private final File updateFolder;
    // The provided callback (if any)
    private final UpdateCallback callback;

    /* Collected from Curse API */

    private String versionName;
    private String versionType;
    private String infoLink;
    private String downloadLink;
    private String hashSum;
    private String description;

    /* Update process variables */
    // Connection to RSS
    private URL url;
    // Updater thread
    private Thread thread;
    // Used for determining the outcome of the update process
    private Updater.UpdateResult result = Updater.UpdateResult.SUCCESS;

    /**
     * Initialize the updater.
     *
     * @param plugin   The plugin that is checking for an update.
     * @param file     The file that the plugin is running from, get this by doing this.getFile() from within your main class.
     * @param type     Specify the type of update this will be. See {@link UpdateType}
     * @param announce True if the program should announce the progress of new updates in console.
     */
    public Updater(Plugin plugin, File file, UpdateType type, boolean announce) {
        this(plugin, file, type, null, announce);
    }

    /**
     * Initialize the updater with the provided callback.
     *
     * @param plugin   The plugin that is checking for an update.
     * @param file     The file that the plugin is running from, get this by doing this.getFile() from within your main class.
     * @param type     Specify the type of update this will be. See {@link UpdateType}
     * @param callback The callback instance to notify when the Updater has finished
     */
    public Updater(Plugin plugin, File file, UpdateType type, UpdateCallback callback) {
        this(plugin, file, type, callback, false);
    }

    /**
     * Initialize the updater with the provided callback.
     *
     * @param plugin   The plugin that is checking for an update.
     * @param file     The file that the plugin is running from, get this by doing this.getFile() from within your main class.
     * @param type     Specify the type of update this will be. See {@link UpdateType}
     * @param callback The callback instance to notify when the Updater has finished
     * @param announce True if the program should announce the progress of new updates in console.
     */
    public Updater(Plugin plugin, File file, UpdateType type, UpdateCallback callback, boolean announce) {
        this.plugin = plugin;
        this.type = type;
        this.announce = announce;
        this.file = file;
        this.updateFolder = this.plugin.getServer().getUpdateFolderFile();
        this.callback = callback;

        final File pluginFile = this.plugin.getDataFolder().getParentFile();
        final File updaterFile = new File(pluginFile, "Updater");
        final File updaterConfigFile = new File(updaterFile, "config.yml");

        YamlConfiguration config = new YamlConfiguration();
        config.options().header("This configuration file affects all plugins using the Updater system (version 2+ - http://forums.bukkit.org/threads/96681/ )" + '\n'
                + "If you wish to use your API key, read http://wiki.bukkit.org/ServerMods_API and place it below." + '\n'
                + "Some updating systems will not adhere to the disabled value, but these may be turned off in their plugin's configuration.");
        config.addDefault(API_KEY_CONFIG_KEY, API_KEY_DEFAULT);
        config.addDefault(DISABLE_CONFIG_KEY, DISABLE_DEFAULT);

        if (!updaterFile.exists()) {
            this.fileIOOrError(updaterFile, updaterFile.mkdir(), true);
        }

        boolean createFile = !updaterConfigFile.exists();
        try {
            if (createFile) {
                this.fileIOOrError(updaterConfigFile, updaterConfigFile.createNewFile(), true);
                config.options().copyDefaults(true);
                config.save(updaterConfigFile);
            } else {
                config.load(updaterConfigFile);
            }
        } catch (final Exception e) {
            final String message;
            message = createFile ? "The updater could not create configuration at " + updaterFile.getAbsolutePath()
                    : "The updater could not load configuration at " + updaterFile.getAbsolutePath();
            this.plugin.getLogger().log(Level.SEVERE, message, e);
        }

        if (config.getBoolean(DISABLE_CONFIG_KEY)) {
            this.result = UpdateResult.DISABLED;
            return;
        }

        try {
            this.url = new URL(Updater.HOST + Updater.QUERY);
            this.thread = new Thread(new UpdateRunnable());
            this.thread.start();
        } catch (final MalformedURLException e) {
            runUpdater();
        }
    }

    /**
     * Get the result of the update process.
     *
     * @return result of the update process.
     * @see UpdateResult
     */
    public Updater.UpdateResult getResult() {
        this.waitForThread();
        return this.result;
    }

    /**
     * Get the latest version's release type.
     *
     * @return latest version's release type.
     * @see ReleaseType
     */
    public ReleaseType getLatestType() {
        this.waitForThread();
        if (this.versionType != null) {
            for (ReleaseType type : ReleaseType.values()) {
                if (this.versionType.equalsIgnoreCase(type.name())) {
                    return type;
                }
            }
        }
        return null;
    }

    /**
     * Get the latest version's name (such as "Project v1.0").
     *
     * @return latest version's name.
     */
    public String getLatestName() {
        this.waitForThread();
        return this.versionName;
    }

    /**
     * Get the latest version's direct file link.
     *
     * @return latest version's file link.
     */
    public String getDownloadLink() {
        this.waitForThread();
        return this.downloadLink;
    }

    /**
     * Get the version info link.
     *
     * @return version info link.
     */
    public String getInfoLink() {
        return this.infoLink;
    }

    /**
     * As the result of Updater output depends on the thread's completion, it is necessary to wait for the thread to finish
     * before allowing anyone to check the result.
     */
    private void waitForThread() {
        if ((this.thread != null) && this.thread.isAlive()) {
            try {
                this.thread.join();
            } catch (final InterruptedException e) {
                this.plugin.getLogger().log(Level.SEVERE, null, e);
            }
        }
    }

    /**
     * Check if the name of a jar is one of the plugins currently installed, used for extracting the correct files out of a zip.
     *
     * @param name a name to check for inside the plugins folder.
     * @return true if a file inside the plugins folder is named this.
     */
    private boolean pluginExists(String name) {
        File[] plugins = listFilesOrError(new File("plugins"));
        for (final File file : plugins) {
            if (file.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check to see if the program should continue by evaluating whether the plugin is already updated, or shouldn't be updated.
     *
     * @return true if the version was located and is not the same as the remote's newest.
     */
    private boolean versionCheck() {
        final String title = this.versionName;
        if (this.type != UpdateType.NO_VERSION_CHECK) {
            final String localVersion = this.plugin.getDescription().getVersion();
            if (title.split(DELIMITER).length == 2) {
                // Get the newest file's version number
                final String remoteVersion = title.split(DELIMITER)[1].split(" ")[0];

                if (this.hasTag(localVersion) || !this.shouldUpdate(localVersion, remoteVersion)) {
                    // We already have the latest version, or this build is tagged for no-update
                    this.result = Updater.UpdateResult.NO_UPDATE;
                    return false;
                }
            } else {
                // The file's name did not contain the string 'vVersion'
                final String authorInfo = this.plugin.getDescription().getAuthors().isEmpty() ? "" : " (" + this.plugin.getDescription().getAuthors().get(0) + ")";
                this.plugin.getLogger().warning("The author of this plugin" + authorInfo + " has misconfigured their Auto Update system");
                this.plugin.getLogger().warning("File versions should follow the format 'PluginName vVERSION'");
                this.plugin.getLogger().warning("Please notify the author of this error.");
                this.result = Updater.UpdateResult.FAIL_NOVERSION;
                return false;
            }
        }
        return true;
    }

    /**
     * <b>If you wish to run mathematical versioning checks, edit this method.</b>
     * <p>
     * With default behavior, Updater will NOT verify that a remote version available on BukkitDev
     * which is not this version is indeed an "update".
     * If a version is present on BukkitDev that is not the version that is currently running,
     * Updater will assume that it is a newer version.
     * This is because there is no standard versioning scheme, and creating a calculation that can
     * determine whether a new update is actually an update is sometimes extremely complicated.
     * </p>
     * <p>
     * Updater will call this method from {@link #versionCheck()} before deciding whether
     * the remote version is actually an update.
     * If you have a specific versioning scheme with which a mathematical determination can
     * be reliably made to decide whether one version is higher than another, you may
     * revise this method, using the local and remote version parameters, to execute the
     * appropriate check.
     * </p>
     * <p>
     * Returning a value of <b>false</b> will tell the update process that this is NOT a new version.
     * Without revision, this method will always consider a remote version at all different from
     * that of the local version a new update.
     * </p>
     *
     * @param localVersion  the current version
     * @param remoteVersion the remote version
     * @return true if Updater should consider the remote version an update, false if not.
     */
    public boolean shouldUpdate(String localVersion, String remoteVersion) {
        return !localVersion.equalsIgnoreCase(remoteVersion);
    }

    /**
     * Evaluate whether the version number is marked showing that it should not be updated by this program.
     *
     * @param version a version number to check for tags in.
     * @return true if updating should be disabled.
     */
    private boolean hasTag(String version) {
        for (final String string : Updater.NO_UPDATE_TAG) {
            if (version.contains(string)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Make a connection to the BukkitDev API and request the newest file's details.
     *
     * @return true if successful.
     */
    private boolean read() {
        try {
            return tryToRead();
        } catch (final IOException | NullPointerException e) {
            RPGInventory.getPluginLogger().severe("The updater could not contact " + HOST + " for check updates.");
            RPGInventory.getPluginLogger().severe("The site experiencing temporary downtime.");
            this.result = UpdateResult.FAIL_DBO;
            return false;
        }
    }

    private boolean tryToRead() throws IOException {
        final URLConnection conn = this.url.openConnection();
        conn.setConnectTimeout(5000);

        conn.addRequestProperty("User-Agent", Updater.USER_AGENT);
        conn.setDoOutput(true);

        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
        StringBuilder responseBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            responseBuilder.append(line.trim());
        }

        JSONArray array = (JSONArray) JSONValue.parse(responseBuilder.toString());
        JSONObject latestUpdate = null;
        for (int i = array.size() - 1; i >= 0; i--) {
            JSONObject update = (JSONObject) array.get(i);
            String[] versions = ((String) update.get(Updater.VERSION_VALUE)).split(", ");

            for (String version : versions) {
                if (Bukkit.getBukkitVersion().startsWith(version)) {
                    latestUpdate = update;
                    break;
                }
            }
        }

        if (latestUpdate == null) {
            this.result = UpdateResult.NO_UPDATE;
            return false;
        }

        this.versionName = (String) latestUpdate.get(Updater.TITLE_VALUE);
        this.versionType = (String) latestUpdate.get(Updater.TYPE_VALUE);
        this.infoLink = (String) latestUpdate.get(Updater.LINK_VALUE);
        this.description = (String) latestUpdate.get(Updater.DESCRIPTION_VALUE);

        return true;
    }

    /**
     * Perform a file operation and log any errors if it fails.
     *
     * @param file   file operation is performed on.
     * @param result result of file operation.
     * @param create true if a file is being created, false if deleted.
     */
    private void fileIOOrError(File file, boolean result, boolean create) {
        if (!result) {
            this.plugin.getLogger().severe("The updater could not " + (create ? "create" : "delete") + " file at: " + file.getAbsolutePath());
        }
    }

    private File[] listFilesOrError(File folder) {
        File[] contents = folder.listFiles();
        if (contents == null) {
            this.plugin.getLogger().severe("The updater could not access files at: " + this.updateFolder.getAbsolutePath());
            return new File[0];
        } else {
            return contents;
        }
    }

    private void runUpdater() {
        if (this.url != null && (this.read() && this.versionCheck())) {
            // Obtain the results of the project's file feed
            this.result = UpdateResult.UPDATE_AVAILABLE;
        }

        if (this.callback != null) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    runCallback();
                }
            }.runTask(this.plugin);
        }
    }

    private void runCallback() {
        this.callback.onFinish(this);
    }

    public String getDescription() {
        return description;
    }

    /**
     * Gives the developer the result of the update process. Can be obtained by called {@link #getResult()}
     */
    public enum UpdateResult {
        /**
         * The updater found an update, and has readied it to be loaded the next time the server restarts/reloads.
         */
        SUCCESS,
        /**
         * The updater did not find an update, and nothing was downloaded.
         */
        NO_UPDATE,
        /**
         * The server administrator has disabled the updating system.
         */
        DISABLED,
        /**
         * The updater found an update, but was unable to download it.
         */
        FAIL_DOWNLOAD,
        /**
         * For some reason, the updater was unable to contact dev.bukkit.org to download the file.
         */
        FAIL_DBO,
        /**
         * When running the version check, the file on DBO did not contain a recognizable version.
         */
        FAIL_NOVERSION,
        /**
         * The updater found an update, but because of the UpdateType being set to NO_DOWNLOAD, it wasn't downloaded.
         */
        UPDATE_AVAILABLE
    }

    /**
     * Allows the developer to specify the type of update that will be run.
     */
    public enum UpdateType {
        /**
         * Run a version check, and then if the file is out of date, download the newest version.
         */
        DEFAULT,
        /**
         * Don't run a version check, just find the latest update and download it.
         */
        NO_VERSION_CHECK,
        /**
         * Get information about the version and the download size, but don't actually download anything.
         */
        NO_DOWNLOAD
    }

    /**
     * Represents the various release types of a file on BukkitDev.
     */
    public enum ReleaseType {
        /**
         * An "alpha" file.
         */
        ALPHA,
        /**
         * A "beta" file.
         */
        BETA,
        /**
         * A "release" file.
         */
        RELEASE
    }

    /**
     * Called on main thread when the Updater has finished working, regardless
     * of result.
     */
    public interface UpdateCallback {
        /**
         * Called when the updater has finished working.
         *
         * @param updater The updater instance
         */
        void onFinish(Updater updater);
    }

    private class UpdateRunnable implements Runnable {
        @Override
        public void run() {
            runUpdater();
        }
    }
}
