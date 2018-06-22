package ru.endlesscode.inspector.api.report

import ru.endlesscode.inspector.api.dsl.markdown


/**
 * Reporter that uses Discord Webhook to write reports.
 *
 * @param id webhook id
 * @param token webhook token
 */
class DiscordReporter @JvmOverloads constructor(
        id: String,
        token: String,
        private val username: String = "Inspector",
        private val avatarUrl: String = "https://gitlab.com/endlesscodegroup/inspector/raw/master/images/inspector_icon_256.png"
) : Reporter {
    private val url = "https://discordapp.com/api/webhooks/$id/$token"

    override fun report(env: Environment, exceptionData: ExceptionData, onError: (Throwable) -> Unit) {

    }

    private fun buildMessage(env: Environment): String {
        return markdown {
            +b(env.title)
            +""
            for ((name, value) in env.fields) {
                +"${b("$name:")} $value"
            }
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
