package ru.endlesscode.inspector.service

interface TextStorage {

    /**
     * Stores given [text] (synchronously).
     *
     * @return replacement of stored text. For example it can be URL of remote host with stored text
     */
    suspend fun storeText(text: String): String
}
