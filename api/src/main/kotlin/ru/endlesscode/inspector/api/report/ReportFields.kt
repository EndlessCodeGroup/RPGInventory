package ru.endlesscode.inspector.api.report

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
        val shouldShow: TextField.() -> Boolean = { value.isNotBlank() }
) : ReportField {

    override val show: Boolean
        get() = shouldShow()
}

open class ListField<T>(
        override val tag: String,
        private val produceList: () -> List<T>,
        private val getSummary: (List<T>) -> String
) : ReportField {

    override val shortValue: String
        get() = getSummary(list)

    override val value: String
        get() = list.joinToString("\n", prefix = "\n") { "- $it" }

    override val show: Boolean
        get() = list.isNotEmpty()

    open protected val list
        get() = produceList()
}

open class FilterableListField<T>(
        tag: String,
        produceList: () -> List<T>,
        getSummary: (List<T>) -> String
) : ListField<T>(tag, produceList, getSummary) {

    private val filters = arrayListOf<(T) -> Boolean>()

    override val list: List<T>
        get() = getFilteredList()

    fun addFilter(filter: (T) -> Boolean) {
        this.filters += filter
    }

    fun getFilteredList(): List<T> {
        var list = super.list
        for (predicate in filters) {
            list = list.filter(predicate)
        }

        return list
    }
}
