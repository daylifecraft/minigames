package com.daylifecraft.minigames.command.debug.seasons.subcommands

import com.daylifecraft.common.command.SubCommand
import com.daylifecraft.common.seasons.Season.Activeness
import com.daylifecraft.minigames.command.CommandsManager
import com.daylifecraft.minigames.command.debug.AbstractDebugCommand
import com.daylifecraft.minigames.seasons.SeasonsManager.configSeasonsList
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.command.builder.suggestion.SuggestionCallback

/** Base class for commands that changes activeness of a season  */
abstract class AbstractSeasonActivenessCommand protected constructor(
  command: String,
  nameSuggestionCallback: SuggestionCallback,
  private val activeness: Activeness,
  private val successKey: String,
) : AbstractDebugCommand(command),
  SubCommand {
  private val nameArgument = ArgumentType.String("name")

  init {
    nameArgument.setSuggestionCallback(nameSuggestionCallback)

    addSyntax(::onExecute, nameArgument)
  }

  override fun onCommandUse(sender: CommandSender, context: CommandContext) {
    val name = context[nameArgument] ?: return

    val senderLanguage = CommandsManager.getSenderLanguage(sender)

    val season = configSeasonsList.getSeasonByName(name)
    if (season == null) {
      senderLanguage.sendMiniMessage(WRONG_NAME_KEY, "seasonName" to name)
      return
    }
    season.setActiveness(activeness)

    senderLanguage.sendMiniMessage(successKey, "seasonName" to name)
  }

  companion object {
    private const val WRONG_NAME_KEY = "debug.seasons.fail.wrong-name"
  }
}
