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

@file:Suppress("unused")

package ru.endlesscode.rpginventory.configuration

import ninja.leaping.configurate.objectmapping.Setting
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable

class Configuration {
    @Setting(comment = "Make sure that you have correctly configured the \"resourcePack\" section before enabling the plugin.")
    val isEnabled = false

    @Setting(value = "updates")
    val updatesConfiguration = UpdatesConfiguration()

    @Setting(value = "resourcePack")
    val resourcePackConfiguration = ResourcePackConfiguration()

    @Setting(comment = "Default locale for use")
    val locale = "en_us"
}

@ConfigSerializable
class UpdatesConfiguration {
    @Setting
    var isCheckUpdates = false

    @Setting
    var isDownloadUpdates = false
}

@ConfigSerializable
class ResourcePackConfiguration {
    @Setting(comment = "TODO: Write useful comment")
    var sha = "unknown"

    @Setting(comment = "TODO: Write useless comment")
    var url = "unknown"
}