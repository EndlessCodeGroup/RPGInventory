package ru.endlesscode.rpginventory.item;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Created by OsipXD on 27.08.2016
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
public class ClassedItem {
    private final int level;
    private final List<String> classes;

    protected ClassedItem(@NotNull ConfigurationSection config) {
        this.level = config.getInt("level", -1);
        this.classes = config.contains("classes") ? config.getStringList("classes") : null;
    }

    public int getLevel() {
        return this.level;
    }

    @Nullable
    protected List<String> getClasses() {
        return this.classes;
    }

    protected String getClassesString() {
        String classesString = "";
        for (String theClass : this.classes) {
            if (!classesString.isEmpty()) {
                classesString += ", ";
            }

            classesString += theClass;
        }

        return classesString;
    }
}
