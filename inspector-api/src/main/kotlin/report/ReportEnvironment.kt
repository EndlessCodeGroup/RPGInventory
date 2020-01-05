package ru.endlesscode.inspector.report

interface ReportEnvironment {

    companion object {
        val EMPTY = object : ReportEnvironment {
            override val appVersion: String = ""
            override val fields: Map<String, ReportField> = emptyMap()
            override val defaultFieldsTags: Set<String> = emptySet()
            override val isInspectorEnabled: Boolean = false
        }
    }

    /**
     * Version of app that uses Inspector.
     */
    val appVersion: String

    /**
     * Environment-related [fields][ReportField]. Stored as relation "tag -> field".
     */
    val fields: Map<String, ReportField>

    /**
     * Tags of fields that will be sent by default.
     */
    val defaultFieldsTags: Set<String>

    /**
     * Indicates that inspector enabled.
     */
    val isInspectorEnabled: Boolean
}
