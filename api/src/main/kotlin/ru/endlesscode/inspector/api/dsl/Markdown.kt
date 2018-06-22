package ru.endlesscode.inspector.api.dsl


interface Element {
    fun render(builder: StringBuilder, indent: String = "")
}

class Line(private val text: String) : Element {
    override fun render(builder: StringBuilder, indent: String) {
        builder.append("$indent$text  \n")
    }
}

@DslMarker
annotation class MarkdownMarker

@MarkdownMarker
abstract class Group(
        private val indent: String,
        private val firstLine: String?,
        private val lastLine: String? = firstLine
) : Element {
    protected val children = arrayListOf<Element>()

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

    operator fun String.unaryPlus() {
        children.add(Line(this))
    }

    override fun toString(): String {
        val builder = StringBuilder()
        render(builder)
        return builder.toString()
    }
}

abstract class TextGroup : Group(indent = "", firstLine = null) {

    fun b(text: String): String {
        return "**$text**"
    }

    fun it(text: String): String {
        return "*$text*"
    }

    fun u(text: String): String {
        return "__${text}__"
    }

    fun code (text: String): String {
        return "`$text`"
    }
}

class Markdown : TextGroup() {
    fun code(lang: String = "", init: Code.() -> Unit) = initGroup(Code(lang), init)
}

class Code(lang: String) : Group(indent = "", firstLine = "```$lang", lastLine = "```")

fun markdown(init: Markdown.() -> Unit): Markdown {
    return Markdown().also(init)
}
