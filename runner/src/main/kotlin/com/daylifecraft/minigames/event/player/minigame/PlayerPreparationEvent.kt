package com.daylifecraft.minigames.event.player.minigame

import com.daylifecraft.minigames.minigames.search.IRoundSearchProvider
import com.google.gson.JsonObject
import net.minestom.server.entity.Player
import net.minestom.server.event.trait.PlayerEvent

/**
 * An event that is called through the rounds search manager. Made for MiniGame Classes which
 * prepare player to search round Ex. Open GUI with settings & filters configuring
 */
class PlayerPreparationEvent(
  private val player: Player,
  val miniGameId: String,
  val roundSearchProvider: IRoundSearchProvider,
  val defaultSettings: JsonObject,
  val defaultFilters: JsonObject,
) : PlayerEvent {
  override fun getPlayer(): Player = player
}
