package com.daylifecraft.minigames.command.debug.towerdefence.subcommands

import com.daylifecraft.minigames.argument.ArgumentPlayer
import com.daylifecraft.minigames.minigames.PlayerMiniGameManager
import com.daylifecraft.minigames.minigames.instances.games.towerdefence.TowerDefenceInstance
import com.daylifecraft.minigames.text.i18n.PlayerLanguage
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.arguments.ArgumentString
import net.minestom.server.command.builder.arguments.number.ArgumentLong
import net.minestom.server.entity.Player

private const val FORCE_FLAG_VALUE = "force"

class SetCurrencyTowerCommand :
  AbstractTowerDefenceCommand(
    "setcurrency",
  ) {

  private val playerArgument = ArgumentPlayer("player")
  private val currencyValueArgument = ArgumentLong("currencyValue")
  private val forceFlagArgument = ArgumentString("forceFlag")

  init {
    addSyntax(this::onExecute, playerArgument, currencyValueArgument, forceFlagArgument)
    addSyntax(this::onExecute, playerArgument, currencyValueArgument)
    addSyntax(this::onExecute, currencyValueArgument)
    addSyntax(this::onExecute, currencyValueArgument, forceFlagArgument)
  }

  override fun onCommandUse(player: Player, context: CommandContext, towerDefenceInstance: TowerDefenceInstance, playerLanguage: PlayerLanguage) {
    if (context.map.isEmpty() || !context.has(currencyValueArgument)) {
      playerLanguage.sendMiniMessage("commands.reusable.fails.required-args-not-found")
      return
    }

    val targetPlayerSpecified = context.has(playerArgument)
    val targetPlayer = if (targetPlayerSpecified) context[playerArgument].player else player

    if (targetPlayer == null) {
      playerLanguage.sendMiniMessage(
        "commands.reusable.fails.wrong-player",
        "targetPlayer" to context[playerArgument].input,
      )
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

    val currencyValue = context[currencyValueArgument]
    val isForceSet = context.has(forceFlagArgument) && context[forceFlagArgument] == FORCE_FLAG_VALUE

    try {
      if (isForceSet) {
        miniGameInstance.towerDefenceEconomy.setBalance(targetPlayer, currencyValue)
      } else {
        miniGameInstance.towerDefenceEconomy.addBalance(targetPlayer, currencyValue)
      }

      val currentTargetBalance = miniGameInstance.towerDefenceEconomy.getBalance(targetPlayer).toString()

      if (targetPlayerSpecified) {
        playerLanguage.sendMiniMessage(
          "debug.td.setcurrency.other.success",
          "targetPlayer" to targetPlayer.username,
          "targetPlayerCurrency" to currentTargetBalance,
        )
      } else {
        playerLanguage.sendMiniMessage(
          "debug.td.setcurrency.success",
          "availableCurrency" to currentTargetBalance,
        )
      }
    } catch (exception: IllegalArgumentException) {
      playerLanguage.sendMiniMessage(
        "debug.td.setcurrency.fail.out-of-range",
        "targetPlayerCurrency" to miniGameInstance.towerDefenceEconomy.getBalance(targetPlayer).toString(),
      )
      return
    }
  }
}
