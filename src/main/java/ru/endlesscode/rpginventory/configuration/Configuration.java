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

import ninja.leaping.configurate.objectmapping.Setting;
import ru.endlesscode.rpginventory.configuration.part.ResourcePackConfiguration;
import ru.endlesscode.rpginventory.configuration.part.UpdatesConfiguration;

@SuppressWarnings({"FieldCanBeLocal", "unused"})
public class Configuration {

    @Setting(comment = "Make sure that you have correctly configured the \"resourcePack\" section before enabling the plugin.")
    private boolean enabled = false;

    @Setting(value = "updates")
    private UpdatesConfiguration updatesConfiguration = new UpdatesConfiguration();

    @Setting(value = "resourcePack")
    private ResourcePackConfiguration resourcePackConfiguration = new ResourcePackConfiguration();

    @Setting(comment = "Default locale for use")
    private String locale = "en_us";

    public boolean isEnabled() {
        return enabled;
    }

    public UpdatesConfiguration getUpdatesConfiguration() {
        return updatesConfiguration;
    }

    public ResourcePackConfiguration getResourcePackConfiguration() {
        return resourcePackConfiguration;
    }

    public String getLocale() {
        return locale;
    }
}
