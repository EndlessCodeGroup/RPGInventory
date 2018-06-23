// Bukkit implementation build config
import java.util.Properties

plugins {
    id("com.github.johnrengelman.shadow") version "2.0.4"
    id("ru.endlesscode.bukkitgradle") version "0.8.0"
    id("com.github.kukuhyoniatmoko.buildconfigkotlin") version "1.0.1"
    id("maven-publish")
}

val apiProject: Project by extra
val minorVersion = 0
version = "${apiProject.version}.$minorVersion"

buildConfigKotlin {
    val discordId: String
    val discordToken: String

    if (System.getenv("CI") == "true") {
        discordId = System.getenv("DISCORD_ID")
        discordToken = System.getenv("DISCORD_TOKEN")
    } else {
        val secretProps = Properties()
        secretProps.load(file("secret.properties").inputStream())
        discordId = secretProps.getProperty("discord.id", "SPECIFY_DISCORD_ID")
        discordToken = secretProps.getProperty("discord.token", "SPECIFY_DISCORD_TOKEN")
    }

    sourceSet("main") {
        packageName = "$group.${project.name}"
        buildConfig(name = "DISCORD_ID", value = discordId)
        buildConfig(name = "DISCORD_TOKEN", value = discordToken)
    }
}

// TODO: Port it to Kotlin DSL
apply(from = "groovy.gradle")
