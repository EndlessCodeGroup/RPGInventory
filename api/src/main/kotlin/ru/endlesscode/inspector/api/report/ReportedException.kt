package ru.endlesscode.inspector.api.report

import ru.endlesscode.inspector.util.rootCause

class ReportedException(cause: Throwable) : RuntimeException("Exception reported with Inspector: ${cause.rootCause.localizedMessage}")
