import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("ru.endlesscode.bukkitgradle") version "0.6.9"
    id("com.github.johnrengelman.shadow") version "2.0.1"
}

applyFrom("bukkit.gradle")

// Runtime dependencies will be added into jar
// Here we can remove not needed dependencies
configurations {
    "runtime" {
        // Guava already in Bukkit
        exclude(module = "guava")
        // Better to use something other plugin that provides kotlin-runtime
        exclude(module = "kotlin-stdlib-jre8")
    }
}

val base: Project by extra
tasks {
    val shadowJar = "shadowJar" (ShadowJar::class) {
        // Understandable filename
        baseName = "${base.name}-${project.name}"
        classifier = null

        // Avoid conflicts with others
        relocate("com", "ru.endlesscode.rpginventory.shaded.com")
        relocate("ninja", "ru.endlesscode.rpginventory.shaded.ninja")
    }

    "jar"().enabled = false
    "assemble"().dependsOn(shadowJar)
}