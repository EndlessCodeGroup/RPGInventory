package ru.endlesscode.inspector.api.report

interface ReportField {
    val shortValue: String
    val value: String
}

class TextField(
        override val shortValue: String,
        override val value: String = shortValue
) : ReportField

class ListField<T>(private val getList: () -> List<T>, private val getShortValue: (List<T>) -> String) : ReportField {

    override val shortValue: String
        get() = getShortValue(getList())

    override val value: String
        get() = getList().joinToString("\n", prefix = "\n") { "- $it" }
}
