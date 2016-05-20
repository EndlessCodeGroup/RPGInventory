package ru.endlesscode.rpginventory.inventory.backpack;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.endlesscode.rpginventory.RPGInventory;
import ru.endlesscode.rpginventory.misc.FileLanguage;
import ru.endlesscode.rpginventory.utils.ItemUtils;
import ru.endlesscode.rpginventory.utils.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by OsipXD on 05.10.2015
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2015 © «EndlessCode Group»
 */
public class BackpackType {
    private final String id;
    private final String name;
    private final List<String> lore;
    private final int size;
    private final String texture;

    private ItemStack item;

    BackpackType(ConfigurationSection config) {
        this.id = config.getName();
        this.name = StringUtils.coloredLine(config.getString("name"));
        this.lore = StringUtils.coloredLines(config.getStringList("lore"));
        this.size = config.getInt("size") < 56 ? config.getInt("size") : 56;
        this.texture = config.getString("item");

        this.createItem();
    }

    private void createItem() {
        ItemStack spawnItem = ItemUtils.getTexturedItem(this.texture);

        ItemMeta meta = spawnItem.getItemMeta();
        meta.setDisplayName(this.name);

        FileLanguage lang = RPGInventory.getLanguage();
        List<String> lore = new ArrayList<>();
        lore.addAll(Arrays.asList(lang.getCaption("backpack.desc").split("\n")));
        lore.addAll(this.lore);
        lore.add(String.format(lang.getCaption("backpack.size"), this.size));

        meta.setLore(lore);
        spawnItem.setItemMeta(meta);
        ItemUtils.setMaxStackSize(spawnItem, 1);
        this.item = ItemUtils.setTag(spawnItem, ItemUtils.BACKPACK_TAG, this.id);
    }

    Backpack createBackpack() {
        return new Backpack(this);
    }

    public int getSize() {
        return this.size;
    }

    String getTitle() {
        return this.name;
    }

    public ItemStack getItem() {
        return this.item;
    }

    public String getId() {
        return this.id;
    }
}
