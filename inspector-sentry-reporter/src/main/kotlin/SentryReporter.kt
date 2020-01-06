package ru.endlesscode.inspector.report

import io.sentry.DefaultSentryClientFactory
import io.sentry.DefaultSentryClientFactory.IN_APP_FRAMES_OPTION
import io.sentry.DefaultSentryClientFactory.UNCAUGHT_HANDLER_ENABLED_OPTION
import io.sentry.Sentry
import io.sentry.SentryClientFactory
import io.sentry.connection.EventSendCallback
import io.sentry.event.Event
import io.sentry.event.EventBuilder
import io.sentry.event.UserBuilder
import io.sentry.event.interfaces.ExceptionInterface
import ru.endlesscode.inspector.PublicApi
import java.util.UUID


/**
 * Reporter that sends reports to Sentry.
 */
@PublicApi
class SentryReporter private constructor(
    dsn: String,
    factory: SentryClientFactory,
    override val focus: ReporterFocus,
    private val fields: Set<ReportField>
) : Reporter {

    override var enabled: Boolean = true

    private val sentry = Sentry.init(dsn, factory)

    private val handlers = CompoundReportHandler()
    private val pendingExceptions = mutableMapOf<UUID, ExceptionData>()

    init {
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

        sentry.release = focus.environment.appVersion
        sentry.context.user = UserBuilder()
            .setId(focus.environment.reporterId)
            .build()
    }

    override fun addHandler(handler: ReportHandler) {
        handlers.addHandler(handler)
    }

    override fun report(message: String, exception: Exception, async: Boolean) {
        val exceptionData = ExceptionData(exception)
        handlers.beforeReport(message, exceptionData)

        val eventBuilder = EventBuilder()
            .withMessage(message)
            .withSentryInterface(ExceptionInterface(exception))
            .apply {
                fields.asSequence()
                    .filter(ReportField::show)
                    .forEach { withExtra(it.name, it.value) }
            }

        sentry.sendEvent(eventBuilder)
        addPendingException(sentry.context.lastEventId, exceptionData)
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
    @PublicApi
    class Builder : Reporter.Builder() {

        private var clientFactory: SentryClientFactory? = null
        private var dsn: String = ""
        private var options: Map<String, String> = emptyMap()

        /**
         * Set [SentryClientFactory], that will be used to create SentryClient.
         */
        @PublicApi
        fun setClientFactory(clientFactory: SentryClientFactory): Builder {
            this.clientFactory = clientFactory
            return this
        }

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
                IN_APP_FRAMES_OPTION to focus.focusedPackage,
                UNCAUGHT_HANDLER_ENABLED_OPTION to "false"
            )
            options.putAll(this.options)

            val optionsString = options.asSequence()
                .joinToString(prefix = "?", separator = "&") { (key, value) -> "$key=$value" }

            return SentryReporter(
                dsn = "$dsn$optionsString",
                factory = clientFactory ?: DefaultSentryClientFactory(),
                focus = focus,
                fields = fields
            )
        }
    }
}
