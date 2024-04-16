package com.daylifecraft.minigames.command.debug.rounds

import com.daylifecraft.common.command.SubCommand
import com.daylifecraft.common.util.extensions.runOnNull
import com.daylifecraft.minigames.Init.miniGamesSettingsManager
import com.daylifecraft.minigames.PlayerManager
import com.daylifecraft.minigames.command.CommandsManager
import com.daylifecraft.minigames.command.debug.AbstractDebugCommand
import com.daylifecraft.minigames.command.suggestion.MiniGameIdSuggestion
import com.daylifecraft.minigames.command.suggestion.OnlinePlayersSuggestion
import com.daylifecraft.minigames.minigames.PlayerMiniGameManager
import com.daylifecraft.minigames.minigames.search.DebugRoundSearchProvider
import com.daylifecraft.minigames.profile.group.PlayersGroupManager
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.command.builder.condition.CommandCondition
import net.minestom.server.entity.Player

private const val WRONG_MINIGAME_ID = "debug.rounds.fail.wrong-minigameid"
private const val WRONG_PLAYER = "commands.reusable.fails.wrong-player"
private const val TARGET_PLAYER_OFFLINE = "commands.reusable.fails.wrong-player"

/**
 * Command perform option to start a debug round It skips players search & starts round with
 * specific players list
 */
class DebugRoundStartCommand :
  AbstractDebugCommand("start"),
  SubCommand {
  private val miniGameIdArgument = ArgumentType.String("miniGameId")
  private val playersArgument = ArgumentType.StringArray("players")

  init {
    condition = CommandCondition { sender, _ -> sender is Player }
    miniGameIdArgument.setSuggestionCallback(MiniGameIdSuggestion())
    playersArgument.setSuggestionCallback(OnlinePlayersSuggestion())

    addSyntax(this::onExecute, miniGameIdArgument, playersArgument)
    addSyntax(this::onExecute, miniGameIdArgument)
  }

  override fun onCommandUse(sender: CommandSender, context: CommandContext) {
    val senderLanguage = CommandsManager.getSenderLanguage(sender)

    val miniGameId: String = context[miniGameIdArgument]
    val miniGameSettings = miniGamesSettingsManager.getGeneralGameSettings(miniGameId)
    if (miniGameSettings == null) {
      senderLanguage.sendMiniMessage(WRONG_MINIGAME_ID)
      return
    }

    val playersData = context[playersArgument]

    val players =
      playersData?.mapNotNull { playerData ->
        val uuid =
          CommandsManager.parseUuidOrUserNameToUuid(playerData).runOnNull {
            senderLanguage.sendMiniMessage(WRONG_PLAYER)
          } ?: return@mapNotNull null

        PlayerManager.getPlayerByUuid(uuid).runOnNull {
          senderLanguage.sendMiniMessage(
            TARGET_PLAYER_OFFLINE,
            "targetPlayer" to playerData,
          )
        }
      }

    var totalPlayersCount = 1

    players?.forEach { player ->
      val playerGroup = PlayersGroupManager.getGroupByPlayer(player)
      if (playerGroup != null && playerGroup.isPlayerLeader(player)) {
        totalPlayersCount += playerGroup.groupSize

        if (!miniGameSettings.groupSize.isInBorder(playerGroup.groupSize)) {
          // TODO 10.11.2023 i18n key
          return
        }
      } else {
        totalPlayersCount += 1
      }
    }

    if (players != null && players.size != playersData.size) {
      return
    }

    if (!miniGameSettings.playersCount.isInBorder(totalPlayersCount)) {
      // TODO 10.11.2023 i18n key
      return
    }

    val roundOwner = sender as Player
    PlayerMiniGameManager.preparePlayerForRoundSearch(
      roundOwner,
      miniGameId,
      DebugRoundSearchProvider(roundOwner, miniGameSettings, players ?: emptyList()),
    )
  }
}
