package ru.endlesscode.inspector.api.report

interface ReportEnvironment {

    companion object {
        val EMPTY = object : ReportEnvironment {
            override val fields: Map<String, ReportField> = emptyMap()
        }
    }

    val fields: Map<String, ReportField>
}
