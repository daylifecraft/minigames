package com.daylifecraft.minigames.command.debug.towerdefence.subcommands

import com.daylifecraft.minigames.argument.ArgumentPlayer
import com.daylifecraft.minigames.command.debug.towerdefence.suggestion.LoadedMonstersIdsSuggestion
import com.daylifecraft.minigames.command.debug.towerdefence.suggestion.TowerDefenceRoundPlayersSuggestion
import com.daylifecraft.minigames.minigames.instances.games.towerdefence.TowerDefenceInstance
import com.daylifecraft.minigames.minigames.instances.games.towerdefence.TowerDefenseManager
import com.daylifecraft.minigames.text.i18n.PlayerLanguage
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.entity.Player
import kotlin.math.abs

class SpawnMonsterTowerCommand : AbstractTowerDefenceCommand("spawnmonster") {

  private val playerArgument = ArgumentPlayer("target")
  private val monsterIdArgument = ArgumentType.String("monster")
  private val amountArgument = ArgumentType.Integer("amount")

  init {
    addSyntax(playerArgument, monsterIdArgument, amountArgument)
    addSyntax(playerArgument, monsterIdArgument)

    playerArgument.setSuggestionCallback(TowerDefenceRoundPlayersSuggestion())
    monsterIdArgument.setSuggestionCallback(LoadedMonstersIdsSuggestion())
  }

  override fun onCommandUse(player: Player, context: CommandContext, towerDefenceInstance: TowerDefenceInstance, playerLanguage: PlayerLanguage) {
    val target = context[playerArgument].player
    val monsterId = context[monsterIdArgument]
    val amount = context.getOrDefault(amountArgument, 1)

    if (target == null || towerDefenceInstance.getTeamInfo(target.uuid) == null) {
      playerLanguage.sendMiniMessage("commands.reusable.fails.rounds.target-player-not-in-round")
      return
    }

    val targetTeamInfo = towerDefenceInstance.getTeamInfo(target.uuid)!!
    val monsterData = TowerDefenseManager.get().findMonsterData(monsterId)

    if (monsterData == null) {
      playerLanguage.sendMiniMessage("debug.td.spawnmonster.fail.wrong-monster", "monster" to monsterId)
      return
    }

    val monsterNameTranslated = playerLanguage.string(monsterData.getNameKey())

    if (amount > 0) {
      towerDefenceInstance.addMonstersToQueue(player, targetTeamInfo, monsterData, amount)

      playerLanguage.sendMiniMessage(
        "debug.td.spawnmonster.success",
        "monsterAmount" to amount.toString(),
        "monster" to monsterNameTranslated,
        "targetPlayer" to target.username,
      )
    } else if (amount == 0) {
      towerDefenceInstance.removeMonstersFromQueue(targetTeamInfo, monsterData)

      playerLanguage.sendMiniMessage(
        "debug.td.spawnmonster.success.deleted",
        "monster" to monsterNameTranslated,
        "targetPlayer" to target.username,
      )
    } else {
      towerDefenceInstance.removeMonstersFromQueue(targetTeamInfo, monsterData, abs(amount))

      playerLanguage.sendMiniMessage(
        "debug.td.spawnmonster.success.reduced",
        "monster" to monsterNameTranslated,
        "monsterAmount" to abs(amount).toString(),
        "targetPlayer" to target.username,
      )
    }
  }
}
