package ru.endlesscode.inspector.api.report

interface Reporter {
    fun report(env: Environment, exceptionData: ExceptionData, onError: (Throwable) -> Unit = { throw it })
}
