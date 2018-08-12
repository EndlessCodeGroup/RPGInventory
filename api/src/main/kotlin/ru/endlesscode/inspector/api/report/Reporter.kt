package ru.endlesscode.inspector.api.report

import kotlinx.coroutines.experimental.Job


interface Reporter {

    val focus: ReporterFocus

    fun addHandler(
            beforeReport: (String, ExceptionData) -> Unit = { _, _ -> },
            onSuccess: (String, ExceptionData) -> Unit = { _, _ -> },
            onError: (Throwable) -> Unit = { throw it }
    ) {
        addHandler(object : ReportHandler {
            override fun beforeReport(message: String, exceptionData: ExceptionData) {
                beforeReport.invoke(message, exceptionData)
            }

            override fun onSuccess(message: String, exceptionData: ExceptionData) {
                onSuccess.invoke(message, exceptionData)
            }

            override fun onError(throwable: Throwable) {
                onError.invoke(throwable)
            }
        })
    }

    fun addHandler(handler: ReportHandler)

    fun report(
            message: String,
            exception: Exception
    ): Job

    abstract class Builder {

        protected var focus: ReporterFocus = ReporterFocus.NO_FOCUS

        fun focusOn(focus: ReporterFocus) {
            this.focus = focus
        }

        abstract fun build(): Reporter
    }
}
