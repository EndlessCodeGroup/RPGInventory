package ru.endlesscode.inspector.api.report

import io.sentry.Sentry
import io.sentry.SentryClient
import io.sentry.connection.EventSendCallback
import io.sentry.event.Event
import io.sentry.event.EventBuilder
import io.sentry.event.interfaces.ExceptionInterface
import ru.endlesscode.inspector.api.PublicApi
import java.util.UUID


/**
 * Reporter that sends reports to Sentry.
 */
class SentryReporter private constructor(
        override val focus: ReporterFocus,
        dsn: String,
        private val fields: Set<ReportField>
) : Reporter {

    override var enabled: Boolean = true

    private var sentry: SentryClient

    private val handlers = CompoundReportHandler()
    private val pendingExceptions = mutableMapOf<UUID, ExceptionData>()

    init {
        Sentry.init(dsn)

        sentry = checkNotNull(Sentry.getStoredClient())
        sentry.addEventSendCallback(object : EventSendCallback {
            override fun onSuccess(event: Event) {
                val reportedException = removePendingException(event.id)
                if (reportedException != null) {
                    handlers.onSuccess(event.message, reportedException)
                }
            }

            override fun onFailure(event: Event, exception: Exception) {
                val isErrorReport = removePendingException(event.id) != null
                if (isErrorReport) {
                    handlers.onError(exception)
                }
            }
        })
    }

    override fun addHandler(handler: ReportHandler) {
        handlers.addHandler(handler)
    }

    override fun report(message: String, exception: Exception, async: Boolean) {
        val exceptionData = ExceptionData(exception)
        handlers.beforeReport(message, exceptionData)

        val event = EventBuilder()
            .withMessage(message)
            .withRelease(focus.environment.appVersion)
            .withSentryInterface(ExceptionInterface(exception))
            .apply {
                fields.asSequence()
                    .filter(ReportField::show)
                    .forEach { withExtra(it.tag, it.value) }
            }
            .build()

        addPendingException(event.id, exceptionData)
        sentry.sendEvent(event)
    }

    private fun addPendingException(id: UUID, exception: ExceptionData) {
        pendingExceptions[id] = exception
    }

    private fun removePendingException(id: UUID): ExceptionData? = pendingExceptions.remove(id)

    /**
     * Builder that should be used to build [SentryReporter].
     *
     * You should specify DSN with one of [setDataSourceName] methods.
     */
    class Builder : Reporter.Builder() {

        private var dsn: String = ""
        private var options: Map<String, String> = emptyMap()

        /**
         * Set Sentry [dsn] with one string.
         * See: https://docs.sentry.io/clients/java/config/#setting-the-dsn
         */
        @PublicApi
        fun setDataSourceName(dsn: String): Builder {
            this.dsn = dsn

            return this
        }

        /**
         * Set Sentry [dsn] built from separated parts.
         * See: https://docs.sentry.io/clients/java/config/#setting-the-dsn
         */
        @PublicApi
        @JvmOverloads
        fun setDataSourceName(
            publicKey: String,
            projectId: String,
            protocol: String = "https",
            host: String = "sentry.io",
            port: String = "",
            options: Map<String, String> = emptyMap()
        ): Builder {
            this.dsn = buildString {
                append(protocol)
                append("://")
                append(publicKey)
                append('@')
                append(host)
                if (port.isNotBlank()) append(':').append(port)
                append('/')
                append(projectId)
            }

            this.options = options

            return this
        }

        /**
         * Build configured [SentryReporter].
         */
        override fun build(): Reporter {
            require(dsn.isNotBlank()) {
                "You should specify DSN with method `setDataSourceName(...)` and it shouldn't be blank."
            }

            val options = mutableMapOf(
                "stacktrace.app.packages" to focus.focusedPackage
            )
            options.putAll(this.options)

            val optionsString = options.asSequence()
                .joinToString(prefix = "?", separator = "&") { (key, value) -> "$key=$value" }

            return SentryReporter(focus, "$dsn$optionsString", fields)
        }
    }
}
