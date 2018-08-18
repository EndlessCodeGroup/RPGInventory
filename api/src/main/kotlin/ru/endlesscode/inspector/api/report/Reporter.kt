package ru.endlesscode.inspector.api.report

import kotlinx.coroutines.experimental.Job


interface Reporter {

    val focus: ReporterFocus

    /**
     * Alias for [addHandler] method.
     */
    fun addHandler(
            beforeReport: (String, ExceptionData) -> Unit = { _, _ -> },
            onSuccess: (String, ExceptionData) -> Unit = { _, _ -> },
            onError: (Throwable) -> Unit = { throw it }
    ) {
        addHandler(object : ReportHandler {
            override fun beforeReport(message: String, exceptionData: ExceptionData) {
                beforeReport.invoke(message, exceptionData)
            }

            override fun onSuccess(message: String, exceptionData: ExceptionData) {
                onSuccess.invoke(message, exceptionData)
            }

            override fun onError(throwable: Throwable) {
                onError.invoke(throwable)
            }
        })
    }


    /**
     * Add handler to reporter.
     *
     * @param handler The handler
     * @see [ReportHandler]
     */
    fun addHandler(handler: ReportHandler)

    /**
     * Report about exception with message.
     *
     * @param message The message that describes when exception thrown
     * @param exception The exception
     * @return [Job] that can be used to track when report done,
     */
    fun report(
            message: String,
            exception: Exception
    ): Job

    abstract class Builder {

        protected var focus: ReporterFocus = ReporterFocus.NO_FOCUS

        /**
         * Returns fields given by tag and custom fields.
         */
        protected val fields: Set<ReportField>
            get() {
                val fields = mutableSetOf<ReportField>()
                fields.addAll(fieldsTags.map { focus.environment.fields.getValue(it) })
                fields.addAll(customFields)

                return fields
            }

        private var fieldsTags: MutableList<String> = mutableListOf()
        private var customFields: MutableList<ReportField> = mutableListOf()

        /**
         * Assign focus.
         * Also copies default environment fields tags to [fieldsTags].
         *
         * @param focus The focus
         */
        fun focusOn(focus: ReporterFocus) {
            this.focus = focus
            fieldsTags.addAll(focus.environment.defaultFieldsTags)
        }

        /**
         * Set fields tags to report.
         */
        fun setFields(vararg newFields: String) {
            fieldsTags = newFields.toMutableList()
        }

        /**
         * Add fields tags to report.
         */
        fun addFields(vararg newFields: String) {
            fieldsTags.addAll(newFields)
        }

        /**
         * Remove fields tags from report.
         */
        fun removeFields(vararg fieldsToRemove: String) {
            fieldsTags.removeAll(fieldsToRemove)
        }

        /**
         * Add custom fields to report.
         */
        fun addCustomFields(vararg customFields: ReportField) {
            this.customFields.addAll(customFields)
        }

        abstract fun build(): Reporter
    }
}
