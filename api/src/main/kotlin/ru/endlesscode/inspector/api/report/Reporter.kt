package ru.endlesscode.inspector.api.report

import kotlinx.coroutines.experimental.Job


interface Reporter {

    val focus: ReporterFocus

    /**
     * Alias for [addHandler] method.
     */
    fun addHandler(
            beforeReport: (String, ExceptionData) -> Unit = { _, _ -> },
            onSuccess: (String, ExceptionData) -> Unit = { _, _ -> },
            onError: (Exception) -> Unit = { throw it }
    ) {
        addHandler(object : ReportHandler {
            override fun beforeReport(message: String, exceptionData: ExceptionData) {
                beforeReport.invoke(message, exceptionData)
            }

            override fun onSuccess(message: String, exceptionData: ExceptionData) {
                onSuccess.invoke(message, exceptionData)
            }

            override fun onError(exception: Exception) {
                onError.invoke(exception)
            }
        })
    }


    /**
     * Add handler to reporter.
     *
     * @param handler The handler
     * @see [ReportHandler]
     */
    fun addHandler(handler: ReportHandler)

    /**
     * Report about exception with message.
     *
     * @param message The message that describes when exception thrown
     * @param exception The exception
     * @return [Job] that can be used to track when report done,
     */
    fun report(
            message: String,
            exception: Exception
    ): Job

    abstract class Builder {

        protected var focus: ReporterFocus = ReporterFocus.NO_FOCUS

        /**
         * Assign focus.
         *
         * @param focus The focus
         */
        fun focusOn(focus: ReporterFocus) {
            this.focus = focus
        }

        abstract fun build(): Reporter
    }
}
