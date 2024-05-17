package com.daylifecraft.minigames.minigames.instances.games.towerdefence

private const val MAX_WAVE_VALUE: Long = 40

// Time of update (1200 ticks = 1 minute)
private const val UPDATE_TIME_TICKS = 60 * 20

class TowerDefenceWaveController(
  private val miniGameInstance: TowerDefenceInstance,
) {
  var currentWave: Long = 1
    set(value) {
      require(value in 1..40) { "Wave cannot be negative or greater than 40" }

      field = value
    }

  var needUpdateWaves = true

  fun doTick(currentTickCounter: Long) {
    if (!needUpdateWaves) return

    if (currentTickCounter % UPDATE_TIME_TICKS != 0L) {
      return
    }

    if (currentWave + 1 > MAX_WAVE_VALUE) {
      miniGameInstance.stopRound()
    } else {
      currentWave++
      miniGameInstance.onWaveUpdated(UPDATE_TIME_TICKS)
    }
  }

  fun addWaveValue(value: Long) {
    require(currentWave + value in 1..40) { "Wave cannot be negative or greater than 40" }

    currentWave += value
  }
}
