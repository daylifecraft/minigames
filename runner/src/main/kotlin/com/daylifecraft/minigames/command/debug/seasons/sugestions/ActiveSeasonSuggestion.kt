package com.daylifecraft.minigames.command.debug.seasons.sugestions

import com.daylifecraft.common.seasons.Season
import com.daylifecraft.common.util.extensions.minestom.currentArgumentValue
import com.daylifecraft.minigames.seasons.SeasonsManager.configSeasonsList
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.suggestion.Suggestion
import net.minestom.server.command.builder.suggestion.SuggestionCallback
import net.minestom.server.command.builder.suggestion.SuggestionEntry

/** Suggest active seasons to command sender  */
class ActiveSeasonSuggestion : SuggestionCallback {
  override fun apply(
    commandSender: CommandSender,
    commandContext: CommandContext,
    suggestion: Suggestion,
  ) {
    val currentArgumentValue = suggestion.currentArgumentValue

    configSeasonsList?.activeSeasonsPrioritized
      ?.filter { season: Season -> season.name.startsWith(currentArgumentValue) }
      ?.forEach { season: Season -> suggestion.addEntry(SuggestionEntry(season.name)) }
  }
}
