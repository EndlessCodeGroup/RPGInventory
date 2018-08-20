// Bukkit implementation build config
import java.util.Properties

plugins {
    id("com.github.johnrengelman.shadow") version "2.0.4"
    id("ru.endlesscode.bukkitgradle") version "0.8.0"
    id("maven-publish")
}

val apiProject: Project by extra
val minorVersion = 1
version = "${apiProject.version}.$minorVersion"

// TODO: Port it to Kotlin DSL
apply(from = "groovy.gradle")
