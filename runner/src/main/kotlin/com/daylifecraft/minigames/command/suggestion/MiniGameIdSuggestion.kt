package com.daylifecraft.minigames.command.suggestion

import com.daylifecraft.common.util.extensions.minestom.currentArgumentValue
import com.daylifecraft.minigames.Init.miniGamesSettingsManager
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.suggestion.Suggestion
import net.minestom.server.command.builder.suggestion.SuggestionCallback
import net.minestom.server.command.builder.suggestion.SuggestionEntry

class MiniGameIdSuggestion : SuggestionCallback {
  override fun apply(
    sender: CommandSender,
    context: CommandContext,
    suggestion: Suggestion,
  ) {
    miniGamesSettingsManager!!.loadedMiniGamesIds.forEach { miniGameId: String ->
      if (miniGameId.startsWith(suggestion.currentArgumentValue)) {
        suggestion.addEntry(SuggestionEntry(miniGameId))
      }
    }
  }
}
