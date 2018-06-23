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
class DiscordReporter @JvmOverloads constructor(
        override val focus: ReporterFocus,
        id: String,
        token: String,
        private val textStorage: TextStorage = HastebinStorage(),
        private val username: String = "Inspector",
        private val avatarUrl: String = "https://gitlab.com/endlesscodegroup/inspector/raw/master/images/inspector_icon_256.png"
) : FilteringReporter() {

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
            fields: List<Pair<String, ReportField>>,
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
            fields: List<Pair<String, ReportField>>,
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
}
