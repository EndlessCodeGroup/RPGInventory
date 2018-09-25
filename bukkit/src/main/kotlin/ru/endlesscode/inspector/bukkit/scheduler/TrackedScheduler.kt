package ru.endlesscode.inspector.bukkit.scheduler

import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitScheduler
import org.bukkit.scheduler.BukkitTask
import ru.endlesscode.inspector.api.PublicApi
import ru.endlesscode.inspector.api.report.Reporter
import ru.endlesscode.inspector.bukkit.plugin.TrackedPlugin

class TrackedScheduler(
    private val delegate: BukkitScheduler,
    private val reporter: Reporter
) : BukkitScheduler by delegate {

    @PublicApi
    constructor(plugin: TrackedPlugin) : this(plugin.server.scheduler, plugin.reporter)

    override fun scheduleSyncDelayedTask(plugin: Plugin, task: Runnable): Int {
        return scheduleTracked(task) {
            delegate.scheduleSyncDelayedTask(plugin, it)
        }
    }

    @Deprecated(
        message = "Use BukkitRunnable.runTask instead",
        replaceWith = ReplaceWith("task.runTask(plugin)")
    )
    override fun scheduleSyncDelayedTask(plugin: Plugin, task: BukkitRunnable): Int {
        return scheduleSyncDelayedTask(plugin, task as Runnable)
    }

    override fun scheduleSyncDelayedTask(plugin: Plugin, task: Runnable, delay: Long): Int {
        return scheduleTracked(task) {
            delegate.scheduleSyncDelayedTask(plugin, it, delay)
        }
    }

    @Deprecated(
        message = "Use BukkitRunnable.runTaskLater instead",
        replaceWith = ReplaceWith("task.runTaskLater(plugin, delay)")
    )
    override fun scheduleSyncDelayedTask(plugin: Plugin, task: BukkitRunnable, delay: Long): Int {
        return scheduleSyncDelayedTask(plugin, task as Runnable, delay)
    }

    override fun scheduleSyncRepeatingTask(plugin: Plugin, task: Runnable, delay: Long, period: Long): Int {
        return scheduleTracked(task) {
            delegate.scheduleSyncRepeatingTask(plugin, it, delay, period)
        }
    }

    @Deprecated(
        message = "Use BukkitRunnable.runTaskTimer instead",
        replaceWith = ReplaceWith("task.runTaskTimer(plugin, delay, period)")
    )
    override fun scheduleSyncRepeatingTask(plugin: Plugin, task: BukkitRunnable, delay: Long, period: Long): Int {
        return scheduleSyncRepeatingTask(plugin, task as Runnable, delay, period)
    }

    @Deprecated("""This name is misleading, as it does not schedule "a sync" task, but rather, "an async" task""")
    override fun scheduleAsyncDelayedTask(plugin: Plugin, task: Runnable): Int {
        return scheduleTracked(task) {
            delegate.scheduleAsyncDelayedTask(plugin, it)
        }
    }

    @Deprecated("""This name is misleading, as it does not schedule "a sync" task, but rather, "an async" task""")
    override fun scheduleAsyncDelayedTask(plugin: Plugin, task: Runnable, delay: Long): Int {
        return scheduleTracked(task) {
            delegate.scheduleAsyncDelayedTask(plugin, it, delay)
        }
    }

    @Deprecated("""This name is misleading, as it does not schedule "a sync" task, but rather, "an async" task""")
    override fun scheduleAsyncRepeatingTask(plugin: Plugin, task: Runnable, delay: Long, period: Long): Int {
        return scheduleTracked(task) {
            delegate.scheduleAsyncRepeatingTask(plugin, it, delay, period)
        }
    }

    private fun scheduleTracked(task: Runnable, block: (Runnable) -> Int): Int {
        val wrapped = TrackedRunnable(task)

        return block(wrapped).also {
            wrapped.taskId = it
        }
    }

    override fun runTask(plugin: Plugin , task: Runnable): BukkitTask {
        return runTracked(task) {
            delegate.runTask(plugin, it)
        }
    }

    @Deprecated(
        message = "Use BukkitRunnable.runTask instead",
        replaceWith = ReplaceWith("task.runTask(plugin)")
    )
    override fun runTask(plugin: Plugin , task: BukkitRunnable): BukkitTask {
        return runTask(plugin, task as Runnable)
    }

    override fun runTaskAsynchronously(plugin: Plugin, task: Runnable): BukkitTask {
        return runTracked(task) {
            delegate.runTaskAsynchronously(plugin, it)
        }
    }

    @Deprecated(
        message = "Use BukkitRunnable.runTaskAsynchronously instead",
        replaceWith = ReplaceWith("task.runTaskAsynchronously(plugin)")
    )
    override fun runTaskAsynchronously(plugin: Plugin, task: BukkitRunnable): BukkitTask {
        return runTaskAsynchronously(plugin, task as Runnable)
    }

    override fun runTaskLater(plugin: Plugin, task: Runnable, delay: Long): BukkitTask {
        return runTracked(task) {
            delegate.runTaskLater(plugin, it, delay)
        }
    }

    @Deprecated(
        message = "Use BukkitRunnable.runTaskLater instead",
        replaceWith = ReplaceWith("task.runTaskLater(plugin, delay)")
    )
    override fun runTaskLater(plugin: Plugin, task: BukkitRunnable, delay: Long): BukkitTask {
        return runTaskLater(plugin, task as Runnable, delay)
    }

    override fun runTaskLaterAsynchronously(plugin: Plugin, task: Runnable, delay: Long): BukkitTask {
        return runTracked(task) {
            delegate.runTaskLaterAsynchronously(plugin, it, delay)
        }
    }

    @Deprecated(
        message = "Use BukkitRunnable.runTaskLaterAsynchronously instead",
        replaceWith = ReplaceWith("task.runTaskLaterAsynchronously(plugin, delay)")
    )
    override fun runTaskLaterAsynchronously(plugin: Plugin, task: BukkitRunnable, delay: Long): BukkitTask {
        return runTaskLaterAsynchronously(plugin, task as Runnable, delay)
    }

    override fun runTaskTimer(plugin: Plugin, task: Runnable, delay: Long, period: Long): BukkitTask {
        return runTracked(task) {
            delegate.runTaskTimer(plugin, it, delay, period)
        }
    }

    @Deprecated(
        message = "Use BukkitRunnable.runTaskTimer instead",
        replaceWith = ReplaceWith("task.runTaskTimer(plugin, delay, period)")
    )
    override fun runTaskTimer(plugin: Plugin, task: BukkitRunnable, delay: Long, period: Long): BukkitTask {
        return runTaskTimer(plugin, task as Runnable, delay, period)
    }

    override fun runTaskTimerAsynchronously(plugin: Plugin, task: Runnable, delay: Long, period: Long): BukkitTask {
        return runTracked(task) {
            delegate.runTaskTimerAsynchronously(plugin, it, delay, period)
        }
    }

    @Deprecated(
        message = "Use BukkitRunnable.runTaskTimerAsynchronously instead",
        replaceWith = ReplaceWith("task.runTaskTimerAsynchronously(plugin, delay, period)")
    )
    override fun runTaskTimerAsynchronously(plugin: Plugin, task: BukkitRunnable, delay: Long, period: Long): BukkitTask {
        return runTaskTimerAsynchronously(plugin, task as Runnable, delay, period)
    }

    private fun runTracked(task: Runnable, block: (Runnable) -> BukkitTask): BukkitTask {
        val wrapped = TrackedRunnable(task)

        return block(wrapped).also {
            wrapped.taskId = it.taskId
        }
    }


    private inner class TrackedRunnable(private val delegate: Runnable) : Runnable {
        var taskId = -1

        override fun run() {
            try {
                delegate.run()
            } catch (e: Exception) {
                reporter.report("Exception was thrown in task #$taskId", e)
            }
        }
    }
}
