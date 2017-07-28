package ru.endlesscode.rpginventory.configuration;

import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMapper;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

import java.io.File;
import java.io.IOException;

public class ConfigurationProvider {

    private static final String HEADER = "This is RPGInventory configuration blah-blah-blah enjoy new config blah-blah-blah";
    private final HoconConfigurationLoader loader;
    private final ObjectMapper.BoundInstance configMapper;
    private CommentedConfigurationNode root;
    private Configuration configBase;

    public ConfigurationProvider(File configuration) {
        try {
            if (!configuration.getParentFile().exists()) {
                configuration.getParentFile().mkdir();
            }
            this.loader = HoconConfigurationLoader.builder().setFile(configuration).build();
            this.configMapper = ObjectMapper.forClass(Configuration.class).bindToNew();
        } catch (ObjectMappingException e) {
            //TODO: log exception
            throw new RuntimeException(e);
        }
        this.reload();
        this.save();
    }

    public Configuration getConfig() {
        return configBase;
    }

    public void reload() {
        try {
            this.root = this.loader.load(ConfigurationOptions.defaults().setHeader(HEADER));
            this.configBase = (Configuration) this.configMapper.populate(this.root.getNode("RPGInventory"));
        } catch (Exception e) {
            //TODO: log exception
            e.printStackTrace();
        }
    }

    public void save() {
        try {
            this.configMapper.serialize(this.root.getNode("RPGInventory"));
            this.loader.save(this.root);
        } catch (IOException | ObjectMappingException e) {
            //TODO: log exception
            e.printStackTrace();
        }
    }

}
