package com.daylifecraft.minigames.minigames.queue

import com.daylifecraft.common.util.TimeUtil.currentUtcSeconds

/** Data-Class to control player MiniGames Lock  */
class PlayerMiniGameLockData(
  val miniGameId: String,
) {
  /**
   * Returns time, when player change queue state. So, they got in queue/game
   *
   * @return Unix-type time in seconds
   */
  var startStageSeconds: Long
    private set

  init {
    startStageSeconds = currentUtcSeconds()
  }

  /** Updates stage time to current UTC seconds  */
  fun resetTime() {
    startStageSeconds = currentUtcSeconds()
  }
}
