package com.daylifecraft.minigames.minigames.queue

import com.daylifecraft.minigames.minigames.search.IRoundSearchProvider
import com.google.gson.JsonObject
import java.util.UUID

/**
 * Class that represents a queue element. That can be single player or players-group.
 * It contains Players, their settings & filters, MiniGame id
 * @property miniGameId target mini-game type
 * @property playersWithSettings players settings
 */
class MiniGameQueueElement private constructor(
  val miniGameId: String,
  val roundSearchProvider: IRoundSearchProvider,
  val playersWithSettings: Map<UUID, JsonObject>,
  val filters: JsonObject,
  private var addedToQueueTime: Long,
) {

  private var startingGame = false

  val totalPlayersCount: Int
    get() = playersWithSettings.size

  val timeElapsedInSearch: Long
    get() = System.currentTimeMillis() - addedToQueueTime

  fun updateAddedToQueueTime() {
    addedToQueueTime = System.currentTimeMillis()
  }

  val isNotStartingGame: Boolean
    get() = !startingGame

  fun setStartingGame(startingGame: Boolean) {
    this.startingGame = startingGame
  }

  companion object {
    fun createFromPlayers(
      miniGameId: String,
      roundSearchProvider: IRoundSearchProvider,
      playersWithSettings: Map<UUID, JsonObject>,
      filters: JsonObject,
    ): MiniGameQueueElement = MiniGameQueueElement(
      miniGameId,
      roundSearchProvider,
      playersWithSettings,
      filters,
      System.currentTimeMillis(),
    )
  }
}
