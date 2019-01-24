package ru.endlesscode.inspector.report

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import ru.endlesscode.inspector.util.rootCause
import ru.endlesscode.inspector.util.similarTo

/**
 * Reporter that filters if he already reported similar exception.
 */
abstract class CachingReporter : Reporter {

    private val reportedCauses = mutableListOf<Throwable>()
    private val handlers = CompoundReportHandler()

    override var enabled: Boolean = true

    override fun addHandler(handler: ReportHandler) {
        handlers.addHandler(handler)
    }

    final override fun report(message: String, exception: Exception, async: Boolean) {
        if (!enabled) return

        val cause = exception.rootCause
        if (isReported(cause)) return

        rememberCause(cause)

        val exceptionData = ExceptionData(exception)
        handlers.beforeReport(message, exceptionData)

        val reportJob = GlobalScope.launch { report(message, exceptionData, handlers::onSuccess, handlers::onError) }
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
