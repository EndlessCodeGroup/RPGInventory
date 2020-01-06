// Bukkit implementation build config

plugins {
    id("ru.endlesscode.bukkitgradle") version "0.8.2"
    `maven-publish`
}

// TODO: Port it to Kotlin DSL
apply(from = "groovy.gradle")
