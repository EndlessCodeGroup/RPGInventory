package ru.endlesscode.inspector.api.report

class ExceptionData(
        val throwable: Throwable,
        var times: Int = 1
)
