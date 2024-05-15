package com.daylifecraft.minigames.command.debug.seasons.subcommands

import com.daylifecraft.common.command.SubCommand
import com.daylifecraft.common.seasons.Season
import com.daylifecraft.minigames.command.CommandsManager
import com.daylifecraft.minigames.command.debug.AbstractDebugCommand
import com.daylifecraft.minigames.seasons.SeasonsManager.configSeasonsList
import com.daylifecraft.minigames.text.i18n.Language
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.arguments.ArgumentType

/** '/~ seasons list &lt;type&gt;' command executor  */
class DebugSeasonsListCommand :
  AbstractDebugCommand("list"),
  SubCommand {
  private val listTypeArgument = ArgumentType.Enum("type", ListType::class.java)

  init {
    addSyntax(this::onExecute, listTypeArgument)
  }

  override fun onCommandUse(sender: CommandSender, context: CommandContext) {
    val senderLanguage = CommandsManager.getSenderLanguage(sender)
    val seasonsList = configSeasonsList

    val listType = context[listTypeArgument] ?: return
    val seasons =
      when (listType) {
        ListType.ALL -> seasonsList.allSeasons
        ListType.ACTIVE -> seasonsList.activeSeasonsPrioritized
        ListType.INACTIVE -> seasonsList.inactiveSeasons
      }

    if (seasons.isEmpty()) {
      senderLanguage.sendMiniMessage(EMPTY_RESULT_KEY, "filter" to listType.name)
    }

    for (season in seasons) {
      sendSeason(senderLanguage, season)
    }
  }

  private enum class ListType {
    ALL,
    ACTIVE,
    INACTIVE,
  }

  companion object {
    private const val LINE_FORMAT_KEY = "debug.seasons.list.line-format"
    private const val EMPTY_RESULT_KEY = "debug.seasons.list.empty-result"

    private fun sendSeason(language: Language, season: Season) {
      language.sendMiniMessage(
        LINE_FORMAT_KEY,
        "name" to season.name,
        "displayNameKey" to season.displayName,
        "displayName" to language.string(season.displayName),
        "priority" to season.priority.toString(),
        "isActive" to season.isActive.toString(),
        "startDate" to season.startDate.toString(),
        "endDate" to season.endDate.toString(),
      )
    }
  }
}
