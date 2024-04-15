package com.daylifecraft.minigames.minigames.search

import com.daylifecraft.minigames.Init
import com.daylifecraft.minigames.minigames.PlayerMiniGameManager
import com.daylifecraft.minigames.minigames.queue.PlayerMiniGameQueueData
import com.daylifecraft.minigames.text.i18n.PlayerLanguage
import com.daylifecraft.minigames.text.i18n.TranslateText
import net.minestom.server.entity.Player

/** Provider for processing a single player who don`t attached to a group  */
class SinglePlayerRoundSearchProvider : IRoundSearchProvider {
  override fun onPlayerPrepared(player: Player, playerMiniGameQueueData: PlayerMiniGameQueueData) {
    // Add player to search queue
    PlayerMiniGameManager.addPlayerToSearchQueue(
      playerMiniGameQueueData.miniGameId,
      this,
      player.uuid,
      playerMiniGameQueueData,
    )

    val playerLanguage = PlayerLanguage.get(player)
    playerLanguage.sendMiniMessage(
      "rounds.new-round.queue.started",
      "minigameDisplayName" to
        TranslateText(
          Init.miniGamesSettingsManager.getGeneralGameSettings(playerMiniGameQueueData.miniGameId)!!.displayNameKey,
        ).string(player),
    )
  }

  override fun onPlayerCancelledPreparation(player: Player, playerMiniGameQueueData: PlayerMiniGameQueueData) {
    // Nothing to do
  }

  override fun onPlayerRejectRoundSearch(player: Player) {
    // Remove player from Search Queue
    PlayerMiniGameManager.removeFromSearchQueueAndUnlockGroup(player.uuid)
  }

  companion object {
    @JvmStatic
    val defaultRoundSearchProvider: SinglePlayerRoundSearchProvider = SinglePlayerRoundSearchProvider()
  }
}
