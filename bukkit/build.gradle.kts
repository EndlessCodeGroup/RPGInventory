import org.gradle.jvm.tasks.Jar

// Bukkit implementation build config

plugins {
    id("com.github.johnrengelman.shadow") version "2.0.4"
    id("ru.endlesscode.bukkitgradle") version "0.8.0"
    id("maven-publish")
}

val apiProject: Project by extra
val minorVersion = 0
version = "${apiProject.version}.$minorVersion"

// TODO: Port it to Kotlin DSL
apply(from = "groovy.gradle")

if (System.getenv("CI") == null) {
    apply(from = "proguard.gradle.kts")
}
