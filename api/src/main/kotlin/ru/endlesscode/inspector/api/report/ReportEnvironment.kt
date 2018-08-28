package ru.endlesscode.inspector.api.report

interface ReportEnvironment {

    companion object {
        val EMPTY = object : ReportEnvironment {
            override val fields: Map<String, ReportField> = emptyMap()
            override val defaultFieldsTags: List<String> = emptyList()
        }
    }

    /**
     * Environment-related fields. Stored as relation "tag ([String]) -> field ([ReportField])".
     */
    val fields: Map<String, ReportField>

    /**
     * Tags of fields that will be sent by default.
     */
    val defaultFieldsTags: List<String>
}
