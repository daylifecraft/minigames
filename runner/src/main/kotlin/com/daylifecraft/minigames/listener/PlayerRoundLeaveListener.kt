package com.daylifecraft.minigames.listener

import com.daylifecraft.minigames.event.player.minigame.PlayerRoundLeaveEvent
import com.daylifecraft.minigames.minigames.PlayerMiniGameManager

/** This listener listen when player leave from MiniGame  */
class PlayerRoundLeaveListener : Listener<PlayerRoundLeaveEvent>(PlayerRoundLeaveEvent::class.java) {
  override fun onCalled(event: PlayerRoundLeaveEvent) {
    PlayerMiniGameManager.onPlayerLeaveFromRoundEvent(event.player)
  }
}
