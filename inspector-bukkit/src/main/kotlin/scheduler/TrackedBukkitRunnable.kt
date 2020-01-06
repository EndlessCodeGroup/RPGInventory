package ru.endlesscode.inspector.bukkit.scheduler

import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitScheduler
import org.bukkit.scheduler.BukkitTask
import ru.endlesscode.inspector.PublicApi

@PublicApi
abstract class TrackedBukkitRunnable : BukkitRunnable() {

    private var task: BukkitTask? = null

    @Synchronized
    override fun isCancelled(): Boolean {
        return checkScheduled().isCancelled
    }

    @Synchronized
    override fun getTaskId(): Int {
        return checkScheduled().taskId
    }

    @Synchronized
    override fun runTask(plugin: Plugin): BukkitTask {
        checkNotYetScheduled()
        return setupTask(getScheduler(plugin).runTask(plugin, this as Runnable))
    }

    @Synchronized
    override fun runTaskAsynchronously(plugin: Plugin): BukkitTask {
        checkNotYetScheduled()
        return setupTask(getScheduler(plugin).runTaskAsynchronously(plugin, this as Runnable))
    }

    @Synchronized
    override fun runTaskLater(plugin: Plugin, delay: Long): BukkitTask {
        checkNotYetScheduled()
        return setupTask(getScheduler(plugin).runTaskLater(plugin, this as Runnable, delay))
    }

    @Synchronized
    override fun runTaskLaterAsynchronously(plugin: Plugin, delay: Long): BukkitTask {
        checkNotYetScheduled()
        return setupTask(getScheduler(plugin).runTaskLaterAsynchronously(plugin, this as Runnable, delay))
    }

    @Synchronized
    override fun runTaskTimer(plugin: Plugin, delay: Long, period: Long): BukkitTask {
        checkNotYetScheduled()
        return setupTask(getScheduler(plugin).runTaskTimer(plugin, this as Runnable, delay, period))
    }

    @Synchronized
    override fun runTaskTimerAsynchronously(plugin: Plugin, delay: Long, period: Long): BukkitTask {
        checkNotYetScheduled()
        return setupTask(getScheduler(plugin).runTaskTimerAsynchronously(plugin, this as Runnable, delay, period))
    }

    private fun getScheduler(plugin: Plugin): BukkitScheduler = plugin.server.scheduler

    private fun checkScheduled(): BukkitTask {
        return checkNotNull(task) { "Not scheduled yet" }
    }

    private fun checkNotYetScheduled() {
        task?.let { error("Already scheduled as ${it.taskId}") }
    }

    private fun setupTask(task: BukkitTask): BukkitTask {
        this.task = task
        return task
    }
}
