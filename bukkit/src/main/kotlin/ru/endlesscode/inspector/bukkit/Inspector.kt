package ru.endlesscode.inspector.bukkit

import ru.endlesscode.inspector.api.report.DiscordReporter
import ru.endlesscode.inspector.api.report.Reporter
import ru.endlesscode.inspector.bukkit.plugin.TrackedPlugin

class Inspector : TrackedPlugin(InspectorPlugin::class.java) {

    override fun createReporter(): Reporter {
        return DiscordReporter(
                focus = this,
                id = BuildConfig.DISCORD_ID,
                token = BuildConfig.DISCORD_TOKEN
        )
    }
}
