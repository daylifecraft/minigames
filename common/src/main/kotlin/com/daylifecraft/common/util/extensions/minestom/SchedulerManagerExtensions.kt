package com.daylifecraft.common.util.extensions.minestom

import net.minestom.server.timer.ExecutionType
import net.minestom.server.timer.SchedulerManager
import net.minestom.server.timer.TaskSchedule

/**
 * Schedules task with Minestom SchedulerManager
 *
 * @param delay delay before first execution
 * @param repeat repetition rate
 * @param executionType sync or async
 * @param task to be executed by scheduler
 */
fun SchedulerManager.scheduleTask(
  delay: TaskSchedule,
  repeat: TaskSchedule,
  executionType: ExecutionType = ExecutionType.TICK_START,
  task: () -> Unit,
) = this.scheduleTask(task, delay, repeat, executionType)
