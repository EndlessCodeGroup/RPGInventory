package ru.endlesscode.inspector.api.report

interface ReportEnvironment {

    companion object {
        val EMPTY = object : ReportEnvironment {
            override val fields: List<Pair<String, ReportField>> = emptyList()
        }
    }

    val fields: List<Pair<String, ReportField>>
}
