package ru.endlesscode.inspector.api.report

class ReportedException(cause: Throwable) : RuntimeException("Exception reported with Inspector", cause)
