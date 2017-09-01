import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins { kotlin("jvm", "1.1.4-3") }

val kotlinVersion = "1.1.4-3"
val root: Project = project
val mockitoVersion by project
subprojects {
    apply { plugin("kotlin") }

    // Apply shared configuration to subprojects
    extra.set("base", root)
    description = root.description
    applyFrom(root.file("config/config.gradle"))

    // Common dependencies
    dependencies {
        // I think Kotlin runtime must be "provided" by other plugin
        implementation(kotlin("stdlib-jre8", kotlinVersion))
        testImplementation(kotlin("test-junit", kotlinVersion))
        testImplementation(group = "org.mockito", name = "mockito-core", version = "$mockitoVersion")
        testImplementation(group = "com.nhaarman", name = "mockito-kotlin-kt1.1", version = "1.5.+")
    }

    // Configure Kotlin
    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }
}

// Gradle version used for generating the Gradle wrapper
tasks {
    "wrapper" (Wrapper::class) {
        gradleVersion = "${project.findProperty("gradleVersion")}"
    }
}