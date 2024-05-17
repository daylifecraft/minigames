package com.daylifecraft.minigames.minigames.instances.games.towerdefence

import com.daylifecraft.common.TaskManager
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

class TowerDefenceInstanceTicker(
  private val instance: TowerDefenceInstance,
) {

  private lateinit var currentSchedulerTask: ScheduledFuture<*>

  private val currentTaskTick = AtomicLong(0)

  fun run() {
    // Period: 50 milliseconds = 1 tick
    currentSchedulerTask = TaskManager.addTask(0, 50, TimeUnit.MILLISECONDS, this::tick)
  }

  private fun tick() {
    try {
      val currentTickValue = if (currentTaskTick.get() == 0L) {
        1
      } else {
        currentTaskTick.get()
      }

      instance.updateHud()
      instance.waveController.doTick(currentTickValue)

      instance.doSpawnMonstersPerTick(currentTickValue)
      instance.doIncomeDistribution(currentTickValue)

      // TODO Remove when core pathfinder will be fixed
      instance.clearStuckedMonsters()

      instance.doTowersAttack(currentTickValue)
      instance.doUpdateHolograms()

      currentTaskTick.incrementAndGet()
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  fun stop() {
    currentSchedulerTask.cancel(true)
  }
}
