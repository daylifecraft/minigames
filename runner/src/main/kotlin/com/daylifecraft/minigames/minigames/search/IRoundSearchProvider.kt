package com.daylifecraft.minigames.minigames.search

import com.daylifecraft.minigames.minigames.PlayerMiniGameManager
import com.daylifecraft.minigames.minigames.queue.PlayerMiniGameQueueData
import com.daylifecraft.minigames.text.i18n.PlayerLanguage
import net.minestom.server.entity.Player

/**
 * The provider implements the logic of controlling the processing of getting into the queue of
 * players and starting the round
 */
interface IRoundSearchProvider {
  /**
   * This method called to check, can be current player prepared for round or not In default case it
   * checks that player does not have MiniGame-lock
   *
   * @param player Player instance
   * @return True, if player passed the checks. Otherwise false
   */
  fun canBePrepared(player: Player): Boolean {
    val playerLanguage = PlayerLanguage.get(player)
    if (PlayerMiniGameManager.isPlayerLocked(player.uuid)) {
      playerLanguage.sendMiniMessage("rounds.new-round.queue.fail.in-queue")
      return false
    }

    return true
  }

  /**
   * This method called, when MiniGame Handler called PlayerPreparationEndEvent with {ACTIVE_SEARCH}
   * result
   *
   * @param player Player instance
   * @param playerMiniGameQueueData MiniGame Queue Data, which stores MiniGame, filters, settings,
   * provider
   */
  fun onPlayerPrepared(player: Player, playerMiniGameQueueData: PlayerMiniGameQueueData)

  /**
   * This method called, when MiniGame Handler called PlayerPreparationEndEvent with {CANCELLED}
   * result
   *
   * @param player Player instance
   * @param playerMiniGameQueueData MiniGame Queue Data, which stores MiniGame, filters, settings,
   * provider
   */
  fun onPlayerCancelledPreparation(player: Player, playerMiniGameQueueData: PlayerMiniGameQueueData)

  /**
   * This method called, when player exit from Search Queue (Cancel search)
   *
   * @param player Player instance
   */
  fun onPlayerRejectRoundSearch(player: Player)
}
