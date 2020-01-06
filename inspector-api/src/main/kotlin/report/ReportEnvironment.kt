package ru.endlesscode.inspector.report

interface ReportEnvironment {

    companion object {
        val EMPTY = object : ReportEnvironment {
            override val appVersion: String = ""
            override val reporterId: String = ""
            override val fields: Map<String, ReportField> = emptyMap()
            override val isInspectorEnabled: Boolean = false
        }
    }

    /**
     * Version of app that uses Inspector.
     */
    val appVersion: String

    /**
     * Unique identifier of reporter.
     */
    val reporterId: String

    /**
     * Environment-related [fields][ReportField]. Stored as relation "name -> field".
     */
    val fields: Map<String, ReportField>

    /**
     * Indicates that inspector enabled.
     */
    val isInspectorEnabled: Boolean
}
