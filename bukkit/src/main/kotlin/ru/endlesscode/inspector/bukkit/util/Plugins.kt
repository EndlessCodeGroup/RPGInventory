package ru.endlesscode.inspector.bukkit.util

import org.bukkit.plugin.Plugin
import ru.endlesscode.inspector.bukkit.plugin.PluginLifecycle

internal val Plugin.realPlugin: Plugin
    get() = (this as? PluginLifecycle)?.holder ?: this
