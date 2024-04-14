package com.daylifecraft.minigames.minigames

import com.daylifecraft.common.TaskManager.addTask
import java.util.concurrent.TimeUnit

private const val EXECUTION_PERIOD = 60L

class MiniGamesInstancesCleaner {
  fun setup() {
    addTask(
      initialDelay = 1,
      period = EXECUTION_PERIOD,
      TimeUnit.SECONDS,
    ) {
      PlayerMiniGameManager.cleanEmptyInstances()
    }
  }
}
