import org.gradle.jvm.tasks.Jar

// Bukkit implementation build config

plugins {
    id("ru.endlesscode.bukkitgradle") version "0.8.1"
    `maven-publish`
}

// TODO: Port it to Kotlin DSL
apply(from = "groovy.gradle")
