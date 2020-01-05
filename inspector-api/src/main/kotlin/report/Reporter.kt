package ru.endlesscode.inspector.report

import ru.endlesscode.inspector.PublicApi

interface Reporter {

    val focus: ReporterFocus

    /**
     * Enable or disable reporter.
     *
     * @see CachingReporter.report
     */
    var enabled: Boolean

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
     * Add given [handler] to the reporter.
     *
     * @see ReportHandler
     */
    fun addHandler(handler: ReportHandler)

    /**
     * Report about [exception] with the [message] that describes when exception thrown (asynchronously).
     */
    fun report(message: String, exception: Exception) {
        report(message, exception, async = true)
    }

    /**
     * Report about [exception] with the [message] that describes when exception thrown.
     *
     * @param async Asynchronously or not
     */
    fun report(message: String, exception: Exception, async: Boolean)

    /**
     * Catch and report all exceptions that will be occurred inside [block].
     *
     * @param message The message that will be used as title
     * @param block Block that should be executed
     */
    fun track(message: String, block: () -> Unit) {
        try {
            block.invoke()
        } catch (e: Exception) {
            report(message, e)
        }
    }

    abstract class Builder {

        protected var focus: ReporterFocus = ReporterFocus.NO_FOCUS

        /**
         * Returns fields given by tag and custom fields.
         */
        protected val fields: Set<ReportField>
            get() {
                val fields = mutableSetOf<ReportField>()
                fields.addAll(fieldsTags.mapNotNull { focus.environment.fields[it] })
                fields.addAll(customFields)

                return fields
            }

        private var fieldsTags: MutableSet<String> = mutableSetOf()
        private var customFields: MutableSet<ReportField> = mutableSetOf()

        /**
         * Assign focus.
         * Also copies default environment fields tags to [fieldsTags].
         *
         * @param focus The focus
         */
        @PublicApi
        fun focusOn(focus: ReporterFocus) : Builder {
            this.focus = focus
            fieldsTags.addAll(focus.environment.defaultFieldsTags)
            return this
        }

        /**
         * Set fields by tags to report.
         */
        @PublicApi
        fun setFields(vararg fieldsTags: String) : Builder {
            this.fieldsTags = fieldsTags.toMutableSet()
            return this
        }

        /**
         * Add fields by tags to report.
         */
        @PublicApi
        fun addFields(vararg fieldsTags: String) : Builder {
            this.fieldsTags.addAll(fieldsTags)
            return this
        }

        /**
         * Remove fields by tags from report.
         */
        @PublicApi
        fun removeFields(vararg fieldsTagsToRemove: String) : Builder {
            fieldsTags.removeAll(fieldsTagsToRemove)
            return this
        }

        /**
         * Add custom fields to report.
         */
        @PublicApi
        fun addCustomFields(vararg customFields: ReportField) : Builder {
            this.customFields.addAll(customFields)
            return this
        }

        abstract fun build(): Reporter
    }
}
