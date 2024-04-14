package com.daylifecraft.common

import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit

object TaskManager {
  private val factory: ThreadFactory = Thread.ofVirtual().factory()
  private val scheduledExecutorService: ScheduledExecutorService = Executors.newScheduledThreadPool(0, factory)

  /**
   * Start new task and return it
   *
   * @param taskRunnable runnable, which will run
   * @param initialDelay delay before task starts
   * @param period the period with which the task will be completed
   * @param unit Unit of measurement for time (TimeUnit)
   * @return ScheduledFuture with task
   */
  fun addTask(
    initialDelay: Long,
    period: Long,
    unit: TimeUnit,
    taskRunnable: Runnable,
  ): ScheduledFuture<*> = scheduledExecutorService.scheduleAtFixedRate(taskRunnable, initialDelay, period, unit)

  /** Stop all task in manager  */
  fun removeAllTasks() {
    scheduledExecutorService.shutdownNow()
  }
}
