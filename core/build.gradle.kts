dependencies {
    compileOnly(group = "ninja.leaping.configurate", name = "configurate-hocon", version = "3.3")
}

// Add core as dependency to all subprojects
var core: Project = project
subprojects {
    dependencies {
        compile(core)
    }
}

val mockitoVersion by project
// Add common dependencies
allprojects {
    dependencies {
        testCompile(group = "junit", name = "junit", version = "4.12")
        testCompile(group = "org.mockito", name = "mockito-core", version = "$mockitoVersion")
        testCompile(group = "com.nhaarman", name = "mockito-kotlin-kt1.1", version = "1.5.+")
    }
}