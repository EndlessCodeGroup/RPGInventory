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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@SuppressWarnings("WeakerAccess")
public class ConfigurationProvider {

    private static final String HEADER = "This is RPGInventory configuration blah-blah-blah enjoy new config blah-blah-blah";

    private final HoconConfigurationLoader loader;
    private final ObjectMapper.BoundInstance configMapper;
    private CommentedConfigurationNode root;
    private Configuration configBase;

    public ConfigurationProvider(File configFolder) {
        this(configFolder.toPath());
    }

    public ConfigurationProvider(Path configFolder) {
        try {
            Files.createDirectory(configFolder);

            final Path path = configFolder.resolve("config.conf");
            this.loader = HoconConfigurationLoader.builder().setPath(path).build();
            this.configMapper = ObjectMapper.forClass(Configuration.class).bindToNew();
            this.reload();
            this.save();
        } catch (ObjectMappingException | IOException e) {
            throw new ConfigurationException("Failed to initialize configuration!", e);
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
            throw new ConfigurationException("Failed to reload configuration!", e);
        }
    }

    public void save() {
        try {
            this.configMapper.serialize(this.root.getNode("RPGInventory"));
            this.loader.save(this.root);
        } catch (ObjectMappingException | IOException e) {
            throw new ConfigurationException("Failed to save configuration!", e);
        }
    }

}
