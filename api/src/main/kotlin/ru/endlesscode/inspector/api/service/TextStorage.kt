package ru.endlesscode.inspector.api.service

interface TextStorage {

    /**
     * Asynchronously stores text
     *
     * @param text the text to store
     * @return replacement of stored text. For example it can be URL of remote host with stored text
     */
    suspend fun storeText(text: String): String
}
