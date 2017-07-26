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

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface Configuration {

    public <T> T get(Class<T> type, String path);
    public <T> T get(Class<T> type, String path, T defaultValue);

    public Object get(String path);
    public Object get(String path, Object defaultValue);

    public String getString(String path);
    public String getString(String path, String defaultValue);

    public int getInt(String path);
    public int getInt(String path, int defaultValue);

    public double getDouble(String path);
    public double getDouble(String path, double defaultValue);

    public <T> List<T> getList(String path);
    public <T> List<T> getList(String path, List<T> defaultValue);

    public <K, V> Map<K, V> getMap(String path);
    public <K, V> Map<K, V> getMap(String path, Map<K, V> defaultValue);

    public void load(String path) throws IOException;
    public void loadFromString(String input);

    public void save() throws IOException;
    public void saveToString();
}
