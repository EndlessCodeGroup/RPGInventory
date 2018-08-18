package ru.endlesscode.inspector.api.report

import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch
import ru.endlesscode.inspector.util.rootCause
import ru.endlesscode.inspector.util.similarTo

abstract class FilteringReporter : Reporter {
    private val reportedCauses = mutableListOf<Throwable>()
    private val handlers = mutableListOf<ReportHandler>()

    override fun addHandler(handler: ReportHandler) {
        handlers.add(handler)
    }

    final override fun report(message: String, exception: Exception): Job {
        val cause = exception.rootCause
        if (reportedCauses.find { it.similarTo(cause) } != null) return launch { /* Return completed job */ }

        reportedCauses.add(cause)

        val exceptionData = ExceptionData(exception)
        beforeReport(message, exceptionData)
        return reportFiltered(message, exceptionData, ::onSuccess, ::onError)
    }

    private fun beforeReport(message: String, exceptionData: ExceptionData) {
        handlers.forEach { it.beforeReport(message, exceptionData) }
    }

    private fun onSuccess(message: String, exceptionData: ExceptionData) {
        handlers.forEach { it.onSuccess(message, exceptionData) }
    }

    private fun onError(exception: Throwable) {
        handlers.forEach { it.onError(exception) }
    }

    abstract fun reportFiltered(
            title: String,
            exceptionData: ExceptionData,
            onSuccess: (String, ExceptionData) -> Unit,
            onError: (Throwable) -> Unit
    ): Job
}
