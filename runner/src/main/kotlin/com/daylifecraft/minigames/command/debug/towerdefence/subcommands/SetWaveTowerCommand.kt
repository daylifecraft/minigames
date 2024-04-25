package com.daylifecraft.minigames.command.debug.towerdefence.subcommands

import com.daylifecraft.minigames.minigames.instances.games.towerdefence.TowerDefenceInstance
import com.daylifecraft.minigames.text.i18n.PlayerLanguage
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.entity.Player

private const val FORCE_FLAG_VALUE = "force"

class SetWaveTowerCommand : AbstractTowerDefenceCommand("setwave") {

  private val differenceArgument = ArgumentType.Long("value")
  private val forceFlag = ArgumentType.String("forceFlag")

  init {
    addSyntax(this::onExecute, differenceArgument)
    addSyntax(this::onExecute, differenceArgument, forceFlag)
  }

  override fun onCommandUse(player: Player, context: CommandContext, towerDefenceInstance: TowerDefenceInstance, playerLanguage: PlayerLanguage) {
    if (context.map.isEmpty()) {
      playerLanguage.sendMiniMessage("commands.reusable.fails.required-args-not-found")
      return
    }

    val difference = context[differenceArgument]
    val isForce = context.has(forceFlag) && context[forceFlag] == FORCE_FLAG_VALUE

    if (isForce && towerDefenceInstance.waveController.currentWave == difference) {
      playerLanguage.sendMiniMessage(
        "debug.td.setwave.fail.same-force-wave",
        "currentWave" to difference.toString(),
      )
      return
    }

    try {
      if (isForce) {
        towerDefenceInstance.waveController.currentWave = difference
      } else {
        towerDefenceInstance.waveController.addWaveValue(difference)
      }

      playerLanguage.sendMiniMessage(
        "debug.td.setwave.success",
        "currentWave" to towerDefenceInstance.waveController.currentWave.toString(),
      )
    } catch (_: IllegalArgumentException) {
      playerLanguage.sendMiniMessage("debug.td.setwave.fail.out-of-range")
    }
  }
}
