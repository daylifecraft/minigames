package com.daylifecraft.minigames.command.debug.seasons.subcommands

import com.daylifecraft.common.command.SubCommand
import com.daylifecraft.minigames.command.CommandsManager
import com.daylifecraft.minigames.command.debug.AbstractDebugCommand
import com.daylifecraft.minigames.command.debug.seasons.sugestions.ActiveSeasonSuggestion
import com.daylifecraft.minigames.seasons.SeasonsManager.configSeasonsList
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.command.builder.suggestion.Suggestion
import net.minestom.server.command.builder.suggestion.SuggestionEntry

/** '/~ seasons prioritize &lt;name&gt; &lt;priority&gt;' command executor  */
class DebugSeasonsPrioritizeCommand :
  AbstractDebugCommand("prioritize"),
  SubCommand {
  private val nameArgument = ArgumentType.String("name")
  private val priorityArgument = ArgumentType.Integer("priority")

  init {
    nameArgument.setSuggestionCallback(ActiveSeasonSuggestion())

    priorityArgument.setSuggestionCallback { _, _, suggestion: Suggestion ->
      if (suggestion.input[suggestion.start - 1] == '\u0000') {
        suggestion.addEntry(ZERO_SUGGESTION)
      }
    }

    addSyntax(
      this::onCommandUse,
      nameArgument,
      priorityArgument,
    )
    addSyntax(this::onCommandUse, nameArgument)
  }

  override fun onCommandUse(sender: CommandSender, context: CommandContext) {
    val name = context[nameArgument] ?: return

    val senderLanguage = CommandsManager.getSenderLanguage(sender)

    val priority = context[priorityArgument]
    if (priority == null) {
      senderLanguage.sendMiniMessage(MISSING_PRIORITY)
      return
    }

    val season = configSeasonsList.getSeasonByName(name)
    if (season == null) {
      senderLanguage.sendMiniMessage(WRONG_NAME_KEY, SEASON_NAME_PLACEHOLDER to name)
      return
    }
    if (!season.isActive) {
      senderLanguage.sendMiniMessage(NOT_ACTIVE_KEY, SEASON_NAME_PLACEHOLDER to name)
      return
    }
    season.priority = priority

    senderLanguage.sendMiniMessage(SUCCESS_KEY, SEASON_NAME_PLACEHOLDER to name)
  }

  companion object {
    private val ZERO_SUGGESTION = SuggestionEntry("0")
    private const val WRONG_NAME_KEY = "debug.seasons.fail.wrong-name"
    private const val NOT_ACTIVE_KEY = "debug.seasons.prioritize.fail.not-active"
    private const val SUCCESS_KEY = "debug.seasons.prioritize.success"
    private const val MISSING_PRIORITY = "debug.seasons.prioritize.fail.priority-missing"
    private const val SEASON_NAME_PLACEHOLDER = "seasonName"
  }
}
