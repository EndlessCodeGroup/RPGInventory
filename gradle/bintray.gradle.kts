import java.util.*

var bintrayUser = "USERNAME_HERE"
var bintrayApiKey = "API_KEY_HERE"

val isRunningOnCI = System.getenv("CI") == "true"
if (isRunningOnCI) {
    bintrayUser = System.getenv("BINTRAY_USER") ?: bintrayUser
    bintrayApiKey = System.getenv("BINTRAY_API_KEY") ?: bintrayApiKey
} else {
    val secretProperties = Properties()
    val file = rootProject.file("secret.properties")

    if (!file.exists()) {
        project.logger.log(LogLevel.WARN,
            """
            secret.properties not found. Uploading to bintray is unavailable.
            To make it possible, create the file and fill it with bintray settings.
            """.trimIndent()
        )
    } else file.inputStream().use { stream ->
        secretProperties.load(stream)

        bintrayUser = secretProperties.getProperty("bintrayUser") ?: error("'bintrayUser' should be specified")
        bintrayApiKey = secretProperties.getProperty("bintrayApiKey") ?: error("'bintrayApiKey' should be specified")
    }
}

allprojects {
    extra["bintrayUser"] = bintrayUser
    extra["bintrayApiKey"] = bintrayApiKey
}
