// Apply shared configuration to subprojects
val base: Project = project

subprojects {
    extra.set("base", base)
    description = base.description
    applyFrom(base.file("config/config.gradle"))
}
