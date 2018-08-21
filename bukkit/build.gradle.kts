import org.gradle.jvm.tasks.Jar

// Bukkit implementation build config
buildscript {
    if (System.getenv("CI") == null) {
        val localProps = java.util.Properties()
        localProps.load(file("local.properties").inputStream())
        val proguardPath = localProps.getProperty("proguard.dir") ?: "SPECIFY proguard.dir PROPERTY"

        repositories {
            flatDir {
                dirs(proguardPath)
            }
        }

        dependencies {
            classpath(":proguard")
        }
    }
}

plugins {
    id("com.github.johnrengelman.shadow") version "2.0.4"
    id("ru.endlesscode.bukkitgradle") version "0.8.0"
    id("maven-publish")
}

val apiProject: Project by extra
val minorVersion = 2
version = "${apiProject.version}.$minorVersion"

// TODO: Port it to Kotlin DSL
apply(from = "groovy.gradle")

if (System.getenv("CI") == null) {
    task("proguard", proguard.gradle.ProGuardTask::class) {
        // Specify the input jars, output jars, and library jars.
        val jarFile = (tasks.get("shadowJar") as Jar).archivePath
        val outPath = jarFile.parentFile.resolve("Inspector-$version-min.jar")
        injars(jarFile.path)
        outjars(outPath)

        val bukkitLib = project.configurations.compileOnly.first { it.name.startsWith("bukkit-") }
        libraryjars(bukkitLib.path)

        // Import static configurations
        configuration("proguard/proguard.pro")
    }
    tasks["shadowJar"].finalizedBy("proguard")
}
