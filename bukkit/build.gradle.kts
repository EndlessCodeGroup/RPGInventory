// Bukkit implementation build config
import java.util.Properties

plugins {
    id("com.github.johnrengelman.shadow") version "2.0.4"
    id("ru.endlesscode.bukkitgradle") version "0.8.0"
    id("com.github.kukuhyoniatmoko.buildconfigkotlin") version "1.0.1"
}

val apiProject: Project by extra
val minorVersion = 0
version = "${apiProject.version}.$minorVersion"

buildConfigKotlin {
    val secretProps = Properties()
    secretProps.load(file("secret.properties").inputStream())

    sourceSet("main") {
        packageName = "$group.${project.name}"
        buildConfig(name = "DISCORD_ID", value = secretProps.getProperty("discord.id", "SPECIFY_ID"))
        buildConfig(name = "DISCORD_TOKEN", value = secretProps.getProperty("discord.token", "SPECIFY_TOKEN"))
    }
}

// TODO: Port it to Kotlin DSL
apply(from = "groovy.gradle")
