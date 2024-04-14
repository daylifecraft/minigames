package com.daylifecraft.minigames.minigames.controllers

import com.daylifecraft.minigames.event.player.minigame.PlayerPreparationEvent
import com.daylifecraft.minigames.minigames.profile.RoundProfile
import com.daylifecraft.minigames.minigames.settings.GeneralGameSettings
import com.daylifecraft.minigames.minigames.settings.MiniGamesSettingManager
import com.google.gson.JsonObject
import net.minestom.server.entity.Player
import java.util.UUID

/** Abstract class for MiniGame controller that stored id & game settings  */
abstract class AbstractMiniGameController protected constructor(
  val miniGameId: String,
  miniGamesSettingManager: MiniGamesSettingManager,
) {
  val generalGameSettings: GeneralGameSettings = miniGamesSettingManager.getGeneralGameSettings(miniGameId)!!

  abstract fun onPlayerPreparationEvent(playerPreparationEvent: PlayerPreparationEvent)

  abstract fun onRoundStartEvent(
    roundProfile: RoundProfile?,
    roundPlayerSettings: Map<Player, JsonObject>,
    playersSpreadByTeams: Set<Set<UUID>>,
    finalGameFilters: JsonObject,
  )
}
