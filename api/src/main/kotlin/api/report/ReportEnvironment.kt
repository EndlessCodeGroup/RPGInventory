package ru.endlesscode.inspector.api.report

interface ReportEnvironment {

    companion object {
        val EMPTY = object : ReportEnvironment {
            override val fields: Map<String, ReportField> = emptyMap()
            override val defaultFieldsTags: List<String> = emptyList()
            override val isInspectorEnabled: Boolean = false
        }
    }

    /**
     * Environment-related [fields][ReportField]. Stored as relation "tag -> field".
     */
    val fields: Map<String, ReportField>

    /**
     * Tags of fields that will be sent by default.
     */
    val defaultFieldsTags: List<String>

    /**
     * Indicates that inspector enabled.
     */
    val isInspectorEnabled: Boolean
}
