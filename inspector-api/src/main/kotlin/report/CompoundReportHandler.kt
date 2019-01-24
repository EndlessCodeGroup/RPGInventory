package ru.endlesscode.inspector.report

/**
 * Handler that dispatches events to many handlers.
 */
class CompoundReportHandler : ReportHandler {

    private val handlers = mutableListOf<ReportHandler>()

    override fun beforeReport(message: String, exceptionData: ExceptionData) {
        handlers.forEach { it.beforeReport(message, exceptionData) }
    }

    override fun onSuccess(message: String, exceptionData: ExceptionData) {
        handlers.forEach { it.onSuccess(message, exceptionData) }
    }

    override fun onError(throwable: Throwable) {
        handlers.forEach { it.onError(throwable) }
    }

    /**
     * Add given [handler] to list of handlers that should receive events.
     */
    fun addHandler(handler: ReportHandler) {
        handlers.add(handler)
    }
}
