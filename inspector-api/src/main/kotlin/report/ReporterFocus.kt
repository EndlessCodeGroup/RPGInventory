package ru.endlesscode.inspector.report

/**
 * Interface that helps reporter focus on needed theme.
 */
interface ReporterFocus {

    companion object {
        val NO_FOCUS = object : ReporterFocus {
            override val focusedPackage: String = ""
            override val environment: ReportEnvironment = ReportEnvironment.EMPTY
        }
    }

    val focusedPackage: String
    val environment: ReportEnvironment
}
