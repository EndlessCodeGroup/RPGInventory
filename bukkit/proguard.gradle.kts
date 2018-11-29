buildscript {
    val proguardVersion = "6.0.1"
    dependencies {
        classpath("net.sf.proguard:proguard-gradle:$proguardVersion")
    }

    repositories {
        mavenCentral()
    }
}

task("proguard", proguard.gradle.ProGuardTask::class) {
    // Specify the input jars, output jars, and library jars.
    val shadowJar = (tasks["shadowJar"] as Jar)
    val jarFile = shadowJar.archivePath
    val minifiedJar: File by project.extra

    injars(jarFile.path)
    outjars(minifiedJar)

    val providedLibs = project.configurations["compileOnly"]
    for (lib in providedLibs) {
        libraryjars(lib.path)
    }

    // Import static configurations
    configuration("proguard/proguard.pro")
}
tasks["shadowJar"].finalizedBy("proguard")
