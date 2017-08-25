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

package ru.endlesscode.rpginventory.configuration

import ninja.leaping.configurate.ConfigurationOptions
import ninja.leaping.configurate.commented.CommentedConfigurationNode
import ninja.leaping.configurate.hocon.HoconConfigurationLoader
import ninja.leaping.configurate.objectmapping.ObjectMapper
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

class ConfigurationProvider(configFolder: Path) {

    companion object {
        private val HEADER = "This is RPGInventory configuration blah-blah-blah enjoy new config blah-blah-blah"
    }

    private val loader: HoconConfigurationLoader
    private val configMapper: ObjectMapper<Configuration>.BoundInstance
    private lateinit var root: CommentedConfigurationNode

    lateinit var config: Configuration
        private set

    constructor(configFolder: File) : this(configFolder.toPath())

    init {
        try {
            Files.createDirectory(configFolder)

            val path = configFolder.resolve("config.conf")
            this.loader = HoconConfigurationLoader.builder().setPath(path).build()
            this.configMapper = ObjectMapper.forClass(Configuration::class.java).bindToNew()
            this.reload()
            this.save()
        } catch (e: Exception) {
            throw ConfigurationException("Failed to initialize configuration!", e)
        }
    }

    fun reload() {
        try {
            this.root = this.loader.load(ConfigurationOptions.defaults().setHeader(HEADER))
            this.config = this.configMapper.populate(this.root.getNode("RPGInventory"))
        } catch (e: Exception) {
            throw ConfigurationException("Failed to reload configuration!", e)
        }
    }

    fun save() {
        try {
            this.configMapper.serialize(this.root.getNode("RPGInventory"))
            this.loader.save(this.root)
        } catch (e: Exception) {
            throw ConfigurationException("Failed to save configuration!", e)
        }

    }
}

@Suppress("unused")
class ConfigurationException : RuntimeException {
    internal constructor() : super()
    internal constructor(message: String) : super(message)
    internal constructor(message: String, cause: Throwable) : super(message, cause)
    internal constructor(cause: Throwable) : super(cause)
}