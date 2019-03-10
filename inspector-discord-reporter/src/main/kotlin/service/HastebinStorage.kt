package ru.endlesscode.inspector.service

import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.coroutines.awaitStringResult
import com.github.kittinunf.fuel.httpPost

class HastebinStorage : TextStorage {

    companion object {
        private const val HOST = "https://hastebin.com"
        private const val ERROR_PLACEHOLDER = "<loading to Hastebin failed>"

        private val keyRegex = Regex(""""key":"([\d|\w]+)"""")
    }

    override suspend fun storeText(text: String): String {
        return "$HOST/documents".httpPost()
            .header(
                "content-type" to "text/plain",
                "user-agent" to "Inspector"
            )
            .body(text)
            .awaitStringResult()
            .fold(::onSuccess, ::onFailure)
    }

    private fun onSuccess(rawJson: String): String {
        val json = rawJson.replace(" ", "")
        val key = keyRegex.find(json)?.groupValues?.get(1)

        return if (key != null) "$HOST/$key.txt" else ERROR_PLACEHOLDER
    }

    private fun onFailure(error: FuelError): String {
        println(error.response)
        return ERROR_PLACEHOLDER
    }
}
