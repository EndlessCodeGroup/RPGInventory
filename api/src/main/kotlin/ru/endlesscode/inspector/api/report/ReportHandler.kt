package ru.endlesscode.inspector.api.report

interface ReportHandler {

    fun beforeReport(message: String, exceptionData: ExceptionData)

    fun onSuccess(message: String, exceptionData: ExceptionData)

    fun onError(throwable: Throwable)
}
