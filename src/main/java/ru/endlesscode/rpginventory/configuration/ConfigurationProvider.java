/*
 * This file is part of RPGInventory.
 * Copyright (C) 2017 EndlessCode Group and contributors
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

package ru.endlesscode.rpginventory.configuration;

import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMapper;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ru.endlesscode.rpginventory.RPGInventory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConfigurationProvider {

    private static final String HEADER = "This is RPGInventory configuration blah-blah-blah enjoy new config blah-blah-blah";
    private final Logger log;
    private final HoconConfigurationLoader loader;
    private final ObjectMapper.BoundInstance configMapper;
    private CommentedConfigurationNode root;
    private Configuration configBase;

    public ConfigurationProvider(RPGInventory instance) {
        this.log = instance.getLogger();
        try {
            if (!instance.getDataFolder().exists()) {
                //noinspection ResultOfMethodCallIgnored
                instance.getDataFolder().mkdir();
            }
            final Path path = Paths.get(instance.getDataFolder().getAbsolutePath(), "config.conf");
            this.loader = HoconConfigurationLoader.builder().setPath(path).build();
            this.configMapper = ObjectMapper.forClass(Configuration.class).bindToNew();
            this.reload();
            this.save();
        } catch (ObjectMappingException e) {
            throw new RuntimeException("Failed to initialize configuration!", e);
        }
    }

    public Configuration getConfig() {
        return configBase;
    }

    public void reload() {
        try {
            this.root = this.loader.load(ConfigurationOptions.defaults().setHeader(HEADER));
            this.configBase = (Configuration) this.configMapper.populate(this.root.getNode("RPGInventory"));
        } catch (ObjectMappingException | IOException e) {
            this.log.log(Level.SEVERE, "Failed to reload configuration!", e);
        }
    }

    public void save() {
        try {
            this.configMapper.serialize(this.root.getNode("RPGInventory"));
            this.loader.save(this.root);
        } catch (ObjectMappingException | IOException e) {
            this.log.log(Level.SEVERE, "Failed to save configuration!", e);
        }
    }

}
