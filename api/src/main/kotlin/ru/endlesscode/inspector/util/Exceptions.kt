package ru.endlesscode.inspector.util

import java.io.PrintWriter
import java.io.StringWriter

val Throwable.stackTraceText: String
    get() {
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        printStackTrace(pw)
        return sw.toString()
    }

fun Throwable.getFocusedRootStackTrace(focusedPackage: String): String {
    return rootCause.getFocusedStackTrace(focusedPackage)
}

val Throwable.rootCause: Throwable
    get() = this.cause?.rootCause ?: this

fun Throwable.similarTo(other: Throwable): Boolean {
    return stackTrace?.contentEquals(other.stackTrace) ?: false
}

fun Throwable.getFocusedStackTrace(focusedPackage: String): String {
    return buildString {
        append(this@getFocusedStackTrace.javaClass.name)
        append(": ")
        appendLine(localizedMessage)

        var skipCount = 0
        for (element in stackTrace) {
            if (element.className.startsWith(focusedPackage)) {
                appendSkipCount(skipCount)
                appendLine(element.toString(), prefix = "  ")
                skipCount = 0
            } else {
                skipCount++
            }
        }

        appendSkipCount(skipCount)

        cause?.let {
            append(it.getFocusedStackTrace(focusedPackage))
        }

        // Remove last empty line
        setLength(length - 1)
    }
}

private fun StringBuilder.appendLine(text: String?, prefix: String = "") {
    if (prefix.isNotEmpty()) append(prefix)
    append(text)
    append("\n")
}

private fun StringBuilder.appendSkipCount(skipCount: Int) {
    if (skipCount > 0) {
        append("  <skipped ")
        append(skipCount)
        append(" lines>\n")
    }
}
