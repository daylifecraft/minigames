package com.daylifecraft.minigames.command.debug.towerdefence.subcommands

import com.daylifecraft.minigames.argument.ArgumentPlayer
import com.daylifecraft.minigames.command.debug.towerdefence.suggestion.TowerDefenceRoundPlayersSuggestion
import com.daylifecraft.minigames.minigames.PlayerMiniGameManager
import com.daylifecraft.minigames.minigames.instances.games.towerdefence.TowerDefenceInstance
import com.daylifecraft.minigames.text.i18n.PlayerLanguage
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.entity.Player

class GetCurrencyTowerCommand :
  AbstractTowerDefenceCommand(
    "getcurrency",
  ) {

  private val playerArgument = ArgumentPlayer("player")

  init {
    addSyntax(this::onExecute)
    addSyntax(this::onExecute, playerArgument)

    playerArgument.setSuggestionCallback(TowerDefenceRoundPlayersSuggestion())
  }

  override fun onCommandUse(
    player: Player,
    context: CommandContext,
    towerDefenceInstance: TowerDefenceInstance,
    playerLanguage: PlayerLanguage,
  ) {
    val targetPlayerSpecified = context.has(playerArgument)
    val targetPlayer = if (targetPlayerSpecified) context[playerArgument].player else player

    if (targetPlayer == null) {
      playerLanguage.sendMiniMessage("commands.reusable.fails.wrong-player", "targetPlayer" to context[playerArgument].input)
      return
    }

    val miniGameInstance = PlayerMiniGameManager.getMiniGameRoundByMember(targetPlayer)

    if (miniGameInstance == null ||
      miniGameInstance !is TowerDefenceInstance ||
      !miniGameInstance.getRoundPlayerSettings().containsKey(player)
    ) {
      playerLanguage.sendMiniMessage("commands.reusable.fails.rounds.target-player-not-in-round")
      return
    }

    if (targetPlayerSpecified) {
      playerLanguage.sendMiniMessage(
        "debug.td.getcurrency.other.success",
        "targetPlayer" to targetPlayer.username,
        "targetPlayerCurrency" to miniGameInstance.towerDefenceEconomy.getBalance(targetPlayer).toString(),
      )
    } else {
      playerLanguage.sendMiniMessage(
        "debug.td.getcurrency.success",
        "availableCurrency" to miniGameInstance.towerDefenceEconomy.getBalance(targetPlayer).toString(),
      )
    }
  }
}
