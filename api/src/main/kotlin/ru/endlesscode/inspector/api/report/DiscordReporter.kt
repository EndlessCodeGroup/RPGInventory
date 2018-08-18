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

    /**
     * Builder that should be used to build [DiscordReporter].
     *
     * You should specify Webhook data with method [hook].
     * Also here you can configure reporter username and avatar.
     */
    class Builder : Reporter.Builder() {

        private val textStorage: TextStorage = defaultTextStorage
        private var id: String = ""
        private var token: String = ""
        private var username: String = "Inspector"
        private var avatarUrl: String = DEFAULT_AVATAR_URL

        /**
         * Build configured [DiscordReporter].
         */
        override fun build(): Reporter {
            if (id.isBlank() || token.isBlank()) {
                error("You should specify Discord id and token with method `hook(...)` and it shouldn't be blank.")
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

        /**
         * Assign Discord Webhook data.
         *
         * @param id Webhook id (it contains only digits).
         * @param token Token for webhook (contains digits and small latin letters).
         */
        fun hook(id: String, token: String) {
            this.id = id
            this.token = token
        }

        /**
         * Set username that will be used as reporter username in Discord.
         *
         * @param username The username.
         */
        fun setUsername(username: String) {
            this.username = username
        }

        /**
         * Set URL of avatar that will be used as reporter avatar in Discord.
         *
         * @param avatarUrl The avatar url. Starting with protocol and including all slashes.
         */
        fun setAvatar(avatarUrl: String) {
            this.avatarUrl = avatarUrl
        }
    }
}
