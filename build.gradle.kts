import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val kotlinVersion: String by extra
buildscript {
    var kotlinVersion: String by extra
    kotlinVersion = "1.1.3-2"
    repositories { jcenter() }
    dependencies { classpath(kotlinModule("gradle-plugin", kotlinVersion)) }
}


val base: Project = project
subprojects {
    apply { plugin("kotlin") }

    // Apply shared configuration to subprojects
    extra.set("base", base)
    description = base.description
    applyFrom(base.file("config/config.gradle"))

    dependencies {
        compile(kotlinModule("stdlib-jre8", "1.1.3-2"))
    }

    // Configure Kotlin
    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }
}
