package ru.endlesscode.inspector.report

interface ReportField {

    companion object {
        private const val HIDDEN_FIELD_VALUE = "<value hidden by user>"
    }

    val tag: String
    val shortValue: String
    val value: String
    val show: Boolean

    fun render(
            short: Boolean = true,
            separator: String = ": ",
            prepareTag: (String) -> String = { it },
            prepareValue: (String) -> String = { it }
    ): String {
        val selectedValue = if (show) {
            if (short) shortValue else value
        } else {
            HIDDEN_FIELD_VALUE
        }

        return "${prepareTag(tag)}$separator${prepareValue(selectedValue)}"
    }
}

open class TextField(
        override val tag: String,
        override val shortValue: String,
        override val value: String = shortValue,
        private val shouldShow: TextField.() -> Boolean = { true }
) : ReportField {

    final override val show: Boolean
        get() = shouldShow()
}

open class ListField<T>(
        override val tag: String,
        private val produceList: () -> List<T>,
        private val getSummary: (List<T>) -> String,
        private val shouldShow: ListField<T>.() -> Boolean = { true }
) : ReportField {

    override val shortValue: String
        get() = getSummary(list)

    override val value: String
        get() = list.joinToString("\n", prefix = "\n") { "- $it" }

    final override val show: Boolean
        get() = shouldShow()

    protected open val list: List<T>
        get() = produceList()
}
