package ru.endlesscode.inspector.bukkit.event

/**
 * Rule of logging
 *
 */
internal class LogRule(
        val event: String,
        private val log: Boolean = true,
        private val skip: Int = 0
) {

    companion object {
        private val LOG = "LOG"
        private const val SKIP = "SKIP"

        /**
         * Creates [LogRule] from string representation.
         * Examples:
         *      "-Event" (event: Event, log: false)
         *      "PlayerEvent:10" (event: PlayerEvent, log: true, skip: 10)
         */
        fun fromString(value: String): LogRule {
            val trimmedValue = value.trim()
            val parts = trimmedValue.trimStart('-').split(':', limit = 2)

            val event = parts[0]
            val log = !trimmedValue.startsWith('-')
            val skip = parts.getOrNull(1)?.toIntOrNull() ?: 0

            return LogRule(event, log, skip)
        }
    }

    private val states = mapOf(
            LOG to LogState(),
            SKIP to SkipState()
    )

    var skipped = 0
        private set

    private var state: State = states.getValue(LOG)

    fun onEvent(block: () -> Unit) {
        state.onEvent(block)?.let {
            state = states.getValue(it)
        }
    }


    @FunctionalInterface
    private interface State {
        fun onEvent(wantedAction: () -> Unit): String?
    }

    private inner class LogState : State {
        override fun onEvent(wantedAction: () -> Unit): String? {
            if (log) {
                wantedAction()
            }

            return if (skip == 0) {
                null
            } else {
                skipped = 0
                return SKIP
            }
        }
    }

    private inner class SkipState: State {
        override fun onEvent(wantedAction: () -> Unit): String? {
            skipped++
            return if (skipped >= skip) LOG else null
        }
    }
}
