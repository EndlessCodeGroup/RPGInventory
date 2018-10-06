package ru.endlesscode.inspector.api.service

class HastebinStorage : TextStorage {

    companion object {
        private const val HOST = "https://hastebin.com"
    }

    override suspend fun storeText(text: String): String {
        return try {
            val response = khttp.post(
                url = "$HOST/documents",
                data = text
            )
            val key = response.jsonObject["key"]

            "$HOST/$key.txt"
        } catch (e: Exception) {
            e.printStackTrace()

            "<loading to Hastebin failed>"
        }
    }
}