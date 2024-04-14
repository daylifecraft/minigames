package com.daylifecraft.minigames.listener

import com.daylifecraft.minigames.event.minigame.RoundPreparationEvent
import com.daylifecraft.minigames.minigames.PlayerMiniGameManager

/** This listener listen when round preparation started  */
class RoundPreparationListener : Listener<RoundPreparationEvent>(RoundPreparationEvent::class.java) {
  override fun onCalled(event: RoundPreparationEvent) {
    PlayerMiniGameManager.onRoundPreparationEvent(event.miniGameInstance)
  }
}
