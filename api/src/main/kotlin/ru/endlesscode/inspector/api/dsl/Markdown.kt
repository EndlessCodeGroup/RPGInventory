package ru.endlesscode.inspector.api.dsl


internal interface Element {
    fun render(builder: StringBuilder, indent: String = "")
}

internal class Line(private val text: String) : Element {
    override fun render(builder: StringBuilder, indent: String) {
        builder.append("$indent$text  \n")
    }
}

@DslMarker
internal annotation class MarkdownMarker

@MarkdownMarker
internal abstract class Group(
        private val indent: String,
        private val firstLine: String?,
        private val lastLine: String? = firstLine
) : Element {
    private val children = arrayListOf<Element>()

    protected fun <T : Group> initGroup(group: T, init: T.() -> Unit): T {
        group.init()
        children.add(group)
        return group
    }

    override fun render(builder: StringBuilder, indent: String) {
        firstLine?.let { builder.append("$it\n") }
        for (c in children) {
            c.render(builder, indent + this.indent)
        }
        lastLine?.let { builder.append("$it\n") }
    }

    operator fun String?.unaryPlus() {
        children.add(Line(this ?: ""))
    }

    operator fun List<String>?.unaryPlus() {
        this?.forEach { +it }
    }

    override fun toString(): String {
        val builder = StringBuilder()
        render(builder)
        return builder.toString()
    }
}

internal abstract class TextGroup : Group(indent = "", firstLine = null) {

    fun b(text: String): String {
        return "**$text**"
    }

    fun it(text: String): String {
        return "*$text*"
    }

    fun hr(): String {
        return "---"
    }
}

internal class Markdown : TextGroup() {
    fun code(lang: String = "", init: Code.() -> Unit) = initGroup(Code(lang), init)
}

internal class Code(lang: String) : Group(indent = "", firstLine = "```$lang", lastLine = "```")

internal fun markdown(init: Markdown.() -> Unit): Markdown {
    return Markdown().also(init)
}
