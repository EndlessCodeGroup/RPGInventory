package ru.endlesscode.inspector.bukkit.command

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import ru.endlesscode.inspector.api.report.ReportedException
import ru.endlesscode.inspector.api.report.Reporter


class TrackedCommandExecutor(
    private val delegate: CommandExecutor,
    private val reporter: Reporter
) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        try {
            return delegate.onCommand(sender, command, label, args)
        } catch (e: Exception) {
            reporter.report("Exception occurred on command '$label' with arguments: ${args.contentToString()}", e)
            throw ReportedException(e)
        }
    }
}
