dependencies {
    implementation(group = "ninja.leaping.configurate", name = "configurate-hocon", version = "3.3")
}

// Add core as dependency to all subprojects
var core: Project = project
subprojects {
    dependencies {
        compile(core)
    }
}
