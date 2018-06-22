package ru.endlesscode.inspector.api.report

interface Reporter {
    fun report(env: Environment, throwable: ExceptionData, onError: (Throwable) -> Unit = { throw it })
}
