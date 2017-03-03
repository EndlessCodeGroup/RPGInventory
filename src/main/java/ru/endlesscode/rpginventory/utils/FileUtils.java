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

package ru.endlesscode.rpginventory.utils;

import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import ru.endlesscode.rpginventory.RPGInventory;

import java.io.*;
import java.util.Map;
import java.util.UUID;

/**
 * Created by OsipXD on 07.12.2015
 * It is part of the RpgInventory.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
public class FileUtils {
    @SuppressWarnings({"unchecked", "ResultOfMethodCallIgnored"})
    public static void jsonToUuidBooleanMap(String jsonFile, Map<UUID, Boolean> uuidBooleanMap) {
        File file = new File(RPGInventory.getInstance().getDataFolder(), jsonFile);
        if (file.exists()) {
            try (Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "utf-8"))) {
                for (Map.Entry<String, Boolean> entry : ((Map<String, Boolean>) new JSONParser().parse(reader)).entrySet()) {
                    if (RPGInventory.getInstance().getServer().getPlayer(UUID.fromString(entry.getKey())) != null) {
                        uuidBooleanMap.put(UUID.fromString(entry.getKey()), entry.getValue());
                    }
                }
            } catch (IOException | ParseException e) {
                e.printStackTrace();
            }
        }
        file.delete();
    }

    public static void uuidBooleanMapToJson(Map<UUID, Boolean> uuidBooleanMap, String jsonFile) {
        File file = new File(RPGInventory.getInstance().getDataFolder(), jsonFile);
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "utf-8"))) {
            JSONObject.writeJSONString(uuidBooleanMap, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @NotNull
    public static String stripExtension(String fileName) {
        return fileName.substring(0, fileName.lastIndexOf('.'));
    }
}
