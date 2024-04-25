package com.daylifecraft.minigames.command.debug.towerdefence.suggestion

import com.daylifecraft.common.util.extensions.minestom.currentArgumentValue
import com.daylifecraft.minigames.minigames.instances.games.towerdefence.TowerDefenseManager
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.arguments.ArgumentWord
import net.minestom.server.command.builder.suggestion.Suggestion
import net.minestom.server.command.builder.suggestion.SuggestionCallback
import net.minestom.server.command.builder.suggestion.SuggestionEntry
import net.minestom.server.utils.StringUtils

class LoadedTowersLevelsSuggestion(
  private val towerIdArgument: ArgumentWord,
) : SuggestionCallback {

  override fun apply(sender: CommandSender, context: CommandContext, suggestion: Suggestion) {
    if (suggestion.currentArgumentValue.contains(StringUtils.SPACE)) {
      suggestion.entries.clear()
      return
    }

    val towerId = context.getOrDefault(towerIdArgument, "")

    TowerDefenseManager.get().getLoadedTowersData()
      .filter { it.towerId == towerId }
      .forEach { suggestion.addEntry(SuggestionEntry(it.level.toString())) }
  }
}
