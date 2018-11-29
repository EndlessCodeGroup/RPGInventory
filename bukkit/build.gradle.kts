import org.gradle.jvm.tasks.Jar

// Bukkit implementation build config

plugins {
    id("com.github.johnrengelman.shadow") version "4.0.2"
    id("ru.endlesscode.bukkitgradle") version "0.8.0"
    id("maven-publish")
}

val inspectorVersion: String by extra
val minorVersion = 1
version = "$inspectorVersion.$minorVersion"

// TODO: Port it to Kotlin DSL
apply(from = "groovy.gradle")

apply(from = "proguard.gradle.kts")
