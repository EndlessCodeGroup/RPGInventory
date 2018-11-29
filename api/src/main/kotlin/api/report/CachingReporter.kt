package ru.endlesscode.inspector.api.report

import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import ru.endlesscode.inspector.util.rootCause
import ru.endlesscode.inspector.util.similarTo

/**
 * Reporter that filters if he already reported similar exception.
 */
abstract class CachingReporter : Reporter {

    private val reportedCauses = mutableListOf<Throwable>()
    private val handlers = mutableListOf<ReportHandler>()

    override var enabled: Boolean = true

    override fun addHandler(handler: ReportHandler) {
        handlers.add(handler)
    }

    final override fun report(message: String, exception: Exception, async: Boolean) {
        if (!enabled) return

        val cause = exception.rootCause
        if (isReported(cause)) return

        rememberCause(cause)

        val exceptionData = ExceptionData(exception)
        beforeReport(message, exceptionData)

        val reportJob = launch { report(message, exceptionData, ::onSuccess, ::onError) }
        if (!async) {
            runBlocking { reportJob.join() }
        }
    }

    private fun isReported(cause: Throwable): Boolean {
        return reportedCauses.find { it.similarTo(cause) } != null
    }

    private fun rememberCause(cause: Throwable) {
        reportedCauses.add(cause)
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
     * Called if exception not reported yet and reporter not disabled.
     * It's similar to [report] but has additional parameters [onSuccess] and [onError].
     *
     * @param onSuccess Will be called on successful report
     * @param onError Will be called on error during report
     */
    abstract suspend fun report(
            title: String,
            exceptionData: ExceptionData,
            onSuccess: (String, ExceptionData) -> Unit,
            onError: (Throwable) -> Unit
    )
}
