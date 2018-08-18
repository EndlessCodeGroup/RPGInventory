package ru.endlesscode.inspector.api.report

import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch

/**
 * Reporter that reports nothing.
 * Used to disable reporting.
 */
object SilentReporter : Reporter {

    override val focus: ReporterFocus = ReporterFocus.NO_FOCUS

    override fun addHandler(handler: ReportHandler) {
        // Do nothing
    }

    override fun report(message: String, exception: Exception): Job {
        return launch {
            // Do nothing
        }
    }
}
