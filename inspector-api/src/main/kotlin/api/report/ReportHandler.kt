package ru.endlesscode.inspector.api.report

/**
 * Report handler. Used for tracking report process.
 */
interface ReportHandler {

    /**
     * Called before report sent.
     *
     * @param message Report message
     * @param exceptionData Exception data that will be reported
     */
    fun beforeReport(message: String, exceptionData: ExceptionData)

    /**
     * Called on successfully report.
     *
     * @param message Report message
     * @param exceptionData Exception data that was reported
     */
    fun onSuccess(message: String, exceptionData: ExceptionData)

    /**
     * Called on error during reporting.
     *
     * @param throwable The throwable that was thrown on report
     */
    fun onError(throwable: Throwable)
}
