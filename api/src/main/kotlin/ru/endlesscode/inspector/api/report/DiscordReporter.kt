package ru.endlesscode.inspector.api.report

import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch
import ru.endlesscode.inspector.api.dsl.markdown
import ru.endlesscode.inspector.api.service.HastebinStorage
import ru.endlesscode.inspector.api.service.TextStorage
import ru.endlesscode.inspector.util.getFocusedRootStackTrace
import ru.endlesscode.inspector.util.stackTraceText


/**
 * Reporter that uses Discord Webhook to write reports.
 *
 * @param id webhook id
 * @param token webhook token
 */
class DiscordReporter private constructor(
        override val focus: ReporterFocus,
        id: String,
        token: String,
        private val textStorage: TextStorage,
        private val username: String,
        private val avatarUrl: String
) : FilteringReporter() {

    companion object {
        const val DEFAULT_AVATAR_URL = "https://gitlab.com/endlesscodegroup/inspector/raw/master/images/inspector_icon_256.png"

        val defaultTextStorage = HastebinStorage()
    }

    private val url = "https://discordapp.com/api/webhooks/$id/$token"

    override fun reportFiltered(
            title: String,
            exceptionData: ExceptionData,
            onSuccess: (String, ExceptionData) -> Unit,
            onError: (Throwable) -> Unit
    ): Job {
        val exception = exceptionData.exception

        return launch {
            try {
                val fullReport = buildFullMessage(title, focus.environment.fields, exception)
                val fullReportUrl = textStorage.storeText(fullReport)
                val message = buildShortMessage(
                        title = title,
                        fields = focus.environment.fields,
                        shortStackTrace = exception.getFocusedRootStackTrace(focus.focusedPackage),
                        fullReportUrl = fullReportUrl
                )

                sendMessage(message, onError)
                onSuccess(title, exceptionData)
            } catch (e: Exception) {
                onError(e)
            }
        }
    }

    private fun buildFullMessage(
            title: String,
            fields: Map<String, ReportField>,
            exception: Exception
    ): String {
        return buildString {
            append(title)
            append("\n\n")
            for ((name, field) in fields) {
                append(name)
                append(": ")
                append(field.value)
                append("\n")
            }
            append("\nStacktrace:\n")
            append(exception.stackTraceText)
            append("\n")
        }
    }

    private fun buildShortMessage(
            title: String,
            fields: Map<String, ReportField>,
            shortStackTrace: String,
            fullReportUrl: String
    ): String {
        return markdown {
            +b(title)
            +""
            for ((name, field) in fields) {
                +"${b("$name:")} ${field.shortValue}"
            }
            +b("Short stacktrace:")
            code("java") {
                +shortStackTrace
            }
            +"${b("Full report:")} $fullReportUrl"
            // Separator
            +st("                                                                                                 ")
        }.toString()
    }

    private fun sendMessage(content: String, onError: (Throwable) -> Unit) {
        khttp.async.post(
                url = url,
                json = mapOf("username" to username, "avatar_url" to avatarUrl, "content" to content),
                onError = onError
        )
    }

    class Builder : Reporter.Builder() {

        private val textStorage: TextStorage = defaultTextStorage
        private var id: String = ""
        private var token: String = ""
        private var username: String = "Inspector"
        private var avatarUrl: String = DEFAULT_AVATAR_URL

        override fun build(): Reporter {
            if (id.isBlank() || token.isBlank()) {
                error("You should specify Discord id and token with method `auth(...)` and it shouldn't be blank.")
            }

            return DiscordReporter(
                    focus = focus,
                    id = id,
                    textStorage = textStorage,
                    token = token,
                    username = username,
                    avatarUrl = avatarUrl
            )
        }

        fun auth(id: String, token: String) {
            this.id = id
            this.token = token
        }

        fun setUsername(username: String) {
            this.username = username
        }

        fun setAvatar(avatarUrl: String) {
            this.avatarUrl = avatarUrl
        }
    }
}
