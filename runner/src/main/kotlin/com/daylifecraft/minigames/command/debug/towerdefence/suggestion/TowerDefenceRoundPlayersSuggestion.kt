package com.daylifecraft.minigames.command.debug.towerdefence.suggestion

import com.daylifecraft.minigames.minigames.PlayerMiniGameManager
import com.daylifecraft.minigames.minigames.instances.games.towerdefence.TowerDefenceInstance
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.suggestion.Suggestion
import net.minestom.server.command.builder.suggestion.SuggestionCallback
import net.minestom.server.command.builder.suggestion.SuggestionEntry
import net.minestom.server.entity.Player

class TowerDefenceRoundPlayersSuggestion : SuggestionCallback {
  override fun apply(sender: CommandSender, context: CommandContext, suggestion: Suggestion) {
    val player = sender as Player

    // Check whether the player is in the round or not
    val miniGameInstance = PlayerMiniGameManager.getMiniGameRoundByMember(player)
    if (miniGameInstance == null || miniGameInstance !is TowerDefenceInstance) {
      return
    }

    miniGameInstance.getRoundPlayerSettings().keys.forEach {
      suggestion.addEntry(SuggestionEntry(it.username))
    }
  }
}
