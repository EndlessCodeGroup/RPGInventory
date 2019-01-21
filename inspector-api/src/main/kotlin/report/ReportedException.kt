package ru.endlesscode.inspector.report

class ReportedException(cause: Throwable) : RuntimeException("Exception reported with Inspector", cause)
