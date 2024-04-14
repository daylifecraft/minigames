package com.daylifecraft.minigames.event.player.minigame

import com.daylifecraft.minigames.minigames.instances.AbstractMiniGameInstance
import com.google.gson.JsonObject
import net.minestom.server.entity.Player
import net.minestom.server.event.trait.PlayerEvent

/**
 * This event called when player moved to MiniGame world instance It need to be listened by
 * MiniGamesController
 */
class PlayerRoundJoinEvent(
  private val player: Player,
  val miniGameInstance: AbstractMiniGameInstance,
  val playerSettings: JsonObject,
) : PlayerEvent {
  override fun getPlayer(): Player = player
}
