package ru.endlesscode.inspector.api.report

import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch
import ru.endlesscode.inspector.util.rootCause
import ru.endlesscode.inspector.util.similarTo

/**
 * Reporter that filters if he already reported similar exception.
 */
abstract class FilteringReporter : Reporter {
    private val reportedCauses = mutableListOf<Throwable>()
    private val handlers = mutableListOf<ReportHandler>()

    override fun addHandler(handler: ReportHandler) {
        handlers.add(handler)
    }

    final override fun report(message: String, exception: Exception): Job {
        val cause = exception.rootCause
        if (shouldBeFiltered(cause)) return launch { /* Return completed job */ }

        reportedCauses.add(cause)

        val exceptionData = ExceptionData(exception)
        beforeReport(message, exceptionData)
        return reportFiltered(message, exceptionData, ::onSuccess, ::onError)
    }

    private fun shouldBeFiltered(cause: Throwable): Boolean {
        return reportedCauses.find { it.similarTo(cause) } != null
    }

    private fun beforeReport(message: String, exceptionData: ExceptionData) {
        handlers.forEach { it.beforeReport(message, exceptionData) }
    }

    private fun onSuccess(message: String, exceptionData: ExceptionData) {
        handlers.forEach { it.onSuccess(message, exceptionData) }
    }

    private fun onError(throwable: Throwable) {
        handlers.forEach { it.onError(throwable) }
    }

    /**
     * Called if exception not filtered and should be reported.
     * It's similar to [report] but has additional parameters [onSuccess] and [onError].
     *
     * @param onSuccess Will be called on successful report
     * @param onError Will be called on error during report
     */
    abstract fun reportFiltered(
            title: String,
            exceptionData: ExceptionData,
            onSuccess: (String, ExceptionData) -> Unit,
            onError: (Throwable) -> Unit
    ): Job
}
