import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("ru.endlesscode.bukkitgradle") version "0.6.9"
    id("com.github.johnrengelman.shadow") version "2.0.1"
}

applyFrom("bukkit.gradle")

val base: Project by extra
tasks {
    "shadowJar" (ShadowJar::class) {
        // Understandable filename
        baseName = "${base.name}-${project.name}"
        classifier = null

        // Avoid conflicts with others
        relocate("com", "ru.endlesscode.rpginventory.shaded.com")
        relocate("ninja", "ru.endlesscode.rpginventory.shaded.ninja")
    }
}

dependencies {
    // Runtime dependencies will be bundled into the output jar
    runtime(group = "ninja.leaping.configurate", name = "configurate-hocon", version = "3.3") {
        // Guava already in Bukkit
        exclude(group = "com.google.guava")
    }
}