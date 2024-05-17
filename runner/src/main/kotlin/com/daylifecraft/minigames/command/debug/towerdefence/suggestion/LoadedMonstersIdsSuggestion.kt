package com.daylifecraft.minigames.command.debug.towerdefence.suggestion

import com.daylifecraft.common.util.extensions.minestom.currentArgumentValue
import com.daylifecraft.minigames.minigames.instances.games.towerdefence.TowerDefenseManager
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.suggestion.Suggestion
import net.minestom.server.command.builder.suggestion.SuggestionCallback
import net.minestom.server.command.builder.suggestion.SuggestionEntry
import net.minestom.server.utils.StringUtils

class LoadedMonstersIdsSuggestion : SuggestionCallback {

  override fun apply(sender: CommandSender, context: CommandContext, suggestion: Suggestion) {
    val currentArgumentValue = suggestion.currentArgumentValue

    if (currentArgumentValue.contains(StringUtils.SPACE)) {
      return
    }

    TowerDefenseManager.get().getLoadedMonstersIds().toSet()
      .filter { it.startsWith(currentArgumentValue) }
      .forEach {
        suggestion.addEntry(SuggestionEntry(it))
      }
  }
}
