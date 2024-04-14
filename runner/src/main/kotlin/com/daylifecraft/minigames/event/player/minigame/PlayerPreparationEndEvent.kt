package com.daylifecraft.minigames.event.player.minigame

import com.daylifecraft.minigames.minigames.queue.PlayerMiniGameQueueData
import com.google.gson.JsonObject
import net.minestom.server.entity.Player
import net.minestom.server.event.trait.PlayerEvent

/**
 * An event that is called through the MiniGames Managers. It is listened by a system of rounds
 * search. It is called after the player has set up the settings and filters and is ready to search
 * (or refused to search for a minigame)
 */
class PlayerPreparationEndEvent private constructor(
  private val player: Player,
  val playerMiniGameQueueData: PlayerMiniGameQueueData,
  val preparationResult: PreparationResult,
) : PlayerEvent {
  override fun getPlayer(): Player = player

  enum class PreparationResult {
    ACTIVE_SEARCH,
    CANCELLED,
  }

  companion object {
    /**
     * Create PlayerPreparationEndEvent with specific player settings & filters
     *
     * @param event PlayerPreparationEvent
     * @param preparationResult PreparationResult: ready to search or cancel
     * @param settings player settings
     * @param filters player filters
     * @return new PlayerPreparationEndEvent
     */
    fun createFromPreparationEvent(
      event: PlayerPreparationEvent,
      preparationResult: PreparationResult,
      settings: JsonObject,
      filters: JsonObject,
    ): PlayerPreparationEndEvent = PlayerPreparationEndEvent(
      event.player,
      PlayerMiniGameQueueData(
        event.miniGameId,
        event.roundSearchProvider,
        settings,
        filters,
      ),
      preparationResult,
    )

    /**
     * Create PlayerPreparationEndEvent with empty player settings and filters
     *
     * @param event PlayerPreparationEvent
     * @param preparationResult PreparationResult: ready to search or cancel
     * @return new PlayerPreparationEndEvent
     */
    fun createFromPreparationEvent(
      event: PlayerPreparationEvent,
      preparationResult: PreparationResult,
    ): PlayerPreparationEndEvent = PlayerPreparationEndEvent(
      event.player,
      PlayerMiniGameQueueData(event.miniGameId, event.roundSearchProvider),
      preparationResult,
    )
  }
}
