buildscript {
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

task("proguard", proguard.gradle.ProGuardTask::class) {
    // Specify the input jars, output jars, and library jars.
    val shadowJar = (tasks.get("shadowJar") as Jar)
    val jarFile = shadowJar.archivePath
    val outPath = jarFile.parentFile.resolve("Inspector-$version-min.jar")
    injars(jarFile.path)
    outjars(outPath)

    val bukkitLib = project.configurations["compileOnly"].first { it.name.startsWith("bukkit-") }
    libraryjars(bukkitLib.path)

    // Import static configurations
    configuration("proguard/proguard.pro")
}
tasks["shadowJar"].finalizedBy("proguard")
