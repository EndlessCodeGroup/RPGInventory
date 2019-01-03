package ru.endlesscode.inspector.api.service

import awaitString
import com.github.kittinunf.fuel.httpPost

class HastebinStorage : TextStorage {

    companion object {
        private const val HOST = "https://hastebin.com"

        private val keyRegex = Regex(""""key":"([\d|\w]+)"""")
    }

    override suspend fun storeText(text: String): String {
        return try {
            val json = "$HOST/documents".httpPost()
                .header("content-type" to "text/plain")
                .body(text)
                .awaitString()
                .replace(" ", "")

            val key = keyRegex.find(json)?.groupValues?.get(1) ?: error("Can't parse JSON: $json")

            "$HOST/$key.txt"
        } catch (e: Exception) {
            e.printStackTrace()

            "<loading to Hastebin failed>"
        }
    }
}
