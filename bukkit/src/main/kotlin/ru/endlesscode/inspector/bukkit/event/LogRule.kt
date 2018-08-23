package ru.endlesscode.inspector.bukkit.event

/**
 * Rule of logging
 *
 */
internal class LogRule(
        val event: String,
        log: Boolean = true,
        private val frequency: Int = 1
) {

    companion object {

        /**
         * Creates [LogRule] from string representation.
         * Examples:
         *      "-Event" (event: Event, log: false)
         *      "PlayerEvent:10" (event: PlayerEvent, log: true, frequency: 10)
         */
        fun fromString(value: String): LogRule {
            val trimmedValue = value.trim()
            val parts = trimmedValue.trimStart('-').split(':', limit = 2)

            val event = parts[0]
            val log = !trimmedValue.startsWith('-')
            val frequency = parts.getOrNull(1)?.toIntOrNull() ?: 1

            return LogRule(event, log, frequency)
        }
    }

    val log = log
        get() {
            return field && isNeedToLog()
        }

    var count: Int = 0
        private set

    fun onEvent() {
        count++
    }

    fun afterLog() {
        count = 0
    }

    private fun isNeedToLog(): Boolean {
        return count >= frequency
    }
}
