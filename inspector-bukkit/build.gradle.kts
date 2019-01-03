import org.gradle.jvm.tasks.Jar

// Bukkit implementation build config

plugins {
    id("com.github.johnrengelman.shadow") version "4.0.1"
    id("ru.endlesscode.bukkitgradle") version "0.8.1"
    id("maven-publish")
}

// TODO: Port it to Kotlin DSL
apply(from = "groovy.gradle")

apply(from = "proguard.gradle.kts")
