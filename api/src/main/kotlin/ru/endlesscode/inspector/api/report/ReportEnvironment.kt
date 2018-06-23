package ru.endlesscode.inspector.api.report

interface ReportEnvironment {
    val fields: List<Pair<String, ReportField>>
}
