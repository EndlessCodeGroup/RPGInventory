package ru.endlesscode.inspector.bukkit.report

import org.bukkit.command.CommandException
import org.bukkit.event.EventException
import ru.endlesscode.inspector.report.Reporter


internal class BukkitUnwrapReporter(private val delegate: Reporter) : Reporter by delegate {

    override fun report(message: String, exception: Exception) {
        report(message, exception, async = true)
    }

    override fun report(message: String, exception: Exception, async: Boolean) {
        delegate.report(message, unwrapException(exception), async)
    }

    private fun unwrapException(exception: Exception): Exception {
        var resultException = exception
        while (resultException is EventException || resultException is CommandException) {
            if (resultException.stackTrace.any { it.className.startsWith(focus.focusedPackage) }) break
            resultException = resultException.cause as? Exception ?: break
        }

        return resultException
    }
}
