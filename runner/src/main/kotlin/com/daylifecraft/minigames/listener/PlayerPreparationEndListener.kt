package com.daylifecraft.minigames.listener

import com.daylifecraft.minigames.event.player.minigame.PlayerPreparationEndEvent
import com.daylifecraft.minigames.minigames.PlayerMiniGameManager

class PlayerPreparationEndListener :
  Listener<PlayerPreparationEndEvent>(
    PlayerPreparationEndEvent::class.java,
  ) {
  override fun onCalled(event: PlayerPreparationEndEvent) {
    PlayerMiniGameManager.onPlayerPreparationEnd(
      event.player,
      event.playerMiniGameQueueData,
      event.preparationResult,
    )
  }
}
