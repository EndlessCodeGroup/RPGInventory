package ru.endlesscode.inspector.api.report

/**
 * Interface that helps reporter focus on needed theme.
 */
interface ReporterFocus {
    val focusedPackage: String
    val environment: ReportEnvironment
}
