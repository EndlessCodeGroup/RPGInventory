package ru.endlesscode.inspector.api.report

import kotlinx.coroutines.experimental.launch
import ru.endlesscode.inspector.api.dsl.markdown
import ru.endlesscode.inspector.api.service.HastebinStorage
import ru.endlesscode.inspector.api.service.TextStorage
import ru.endlesscode.inspector.util.getFocusedStackTrace
import ru.endlesscode.inspector.util.stackTraceText


/**
 * Reporter that uses Discord Webhook to write reports.
 *
 * @param id webhook id
 * @param token webhook token
 */
class DiscordReporter @JvmOverloads constructor(
        private val focus: ReporterFocus,
        id: String,
        token: String,
        private val textStorage: TextStorage = HastebinStorage(),
        private val username: String = "Inspector",
        private val avatarUrl: String = "https://gitlab.com/endlesscodegroup/inspector/raw/master/images/inspector_icon_256.png"
) : Reporter {
    private val url = "https://discordapp.com/api/webhooks/$id/$token"

    override fun report(env: Environment, exceptionData: ExceptionData, onError: (Throwable) -> Unit) {
        val title = "${env.title} [x${exceptionData.times}]"
        val exception = exceptionData.throwable

        launch {
            try {
                val stackTraceUrl = textStorage.storeText(exception.stackTraceText)
                val message = buildMessage(
                        title = title,
                        fields = env.fields,
                        shortStackTrace = exception.getFocusedStackTrace(focus.focusedPackage),
                        fullStackTraceUrl = stackTraceUrl
                )

                sendMessage(message, onError)
            } catch (e: Exception) {
                onError(e)
            }
        }
    }

    private fun buildMessage(
            title: String,
            fields: List<Pair<String, String>>,
            shortStackTrace: String,
            fullStackTraceUrl: String
    ): String {
        return markdown {
            +b(title)
            +""
            for ((name, value) in fields) {
                +"${b("$name:")} $value"
            }
            +b("Short stacktrace:")
            code("java") {
                +shortStackTrace
            }
            +"${b("Full stacktrace:")} $fullStackTraceUrl"
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
