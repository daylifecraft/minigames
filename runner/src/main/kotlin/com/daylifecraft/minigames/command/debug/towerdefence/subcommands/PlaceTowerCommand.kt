package com.daylifecraft.minigames.command.debug.towerdefence.subcommands

import com.daylifecraft.minigames.command.debug.towerdefence.suggestion.LoadedTowersIdsSuggestion
import com.daylifecraft.minigames.command.debug.towerdefence.suggestion.LoadedTowersLevelsSuggestion
import com.daylifecraft.minigames.minigames.instances.games.towerdefence.TowerData
import com.daylifecraft.minigames.minigames.instances.games.towerdefence.TowerDefenceInstance
import com.daylifecraft.minigames.minigames.instances.games.towerdefence.TowerDefenseManager
import com.daylifecraft.minigames.text.i18n.PlayerLanguage
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.command.builder.arguments.ArgumentWord
import net.minestom.server.command.builder.arguments.number.ArgumentInteger
import net.minestom.server.entity.Player

class PlaceTowerCommand :
  AbstractTowerDefenceCommand(
    "placetower",
  ) {
  private val towerIdArgument: ArgumentWord = ArgumentType.Word("towerId")
  private val towerLevelArgument: ArgumentInteger = ArgumentType.Integer("towerLevel")

  init {
    towerIdArgument.setSuggestionCallback(LoadedTowersIdsSuggestion())
    towerLevelArgument.setSuggestionCallback(LoadedTowersLevelsSuggestion(towerIdArgument))

    addSyntax(towerIdArgument, towerLevelArgument)
    addSyntax(towerIdArgument)
  }

  override fun onCommandUse(
    player: Player,
    context: CommandContext,
    towerDefenceInstance: TowerDefenceInstance,
    playerLanguage: PlayerLanguage,
  ) {
    if (!context.has(towerIdArgument)) {
      playerLanguage.sendMiniMessage("commands.reusable.fails.required-args-not-found")
      return
    }

    val inputTowerId = context[towerIdArgument]
    val inputTowerLevel: Int = if (context.has(towerLevelArgument)) {
      context[towerLevelArgument]
    } else {
      TowerDefenseManager.get().getMinimalTowerLevel(inputTowerId)
    }

    val inputTower = TowerDefenseManager.get().findTowerData(inputTowerId, inputTowerLevel)
    if (inputTower == null) {
      for (towerData in TowerDefenseManager.get().getLoadedTowersData()) {
        if (towerData.towerId == inputTowerId && towerData.level != inputTowerLevel) {
          playerLanguage.sendMiniMessage(
            "debug.td.placetower.fail.wrong-level",
            "tower" to inputTowerId,
            "towerLevel" to inputTowerLevel.toString(),
          )
          return
        }
      }

      playerLanguage.sendMiniMessage("debug.td.placetower.fail.wrong-name", "tower" to inputTowerId)
      return
    }

    var targetBlockPosition = player.getTargetBlockPosition(MAX_TARGET_DISTANCE)

    if (targetBlockPosition == null) {
      playerLanguage.sendMiniMessage("debug.td.placetower.fail.air-position")
      return
    }

    val tower: TowerData? = towerDefenceInstance.findTowerData(targetBlockPosition)

    if (tower == null) {
      targetBlockPosition = targetBlockPosition.add(0.0, 1.0, 0.0)
      val optimalStartPosition = inputTower.findEmptyPlacePositionFrom(
        towerDefenceInstance.getTeamInfo(player.uuid)!!.blockType,
        towerDefenceInstance.miniGameWorldInstance.instance,
        targetBlockPosition,
      )

      if (optimalStartPosition == null) {
        playerLanguage.sendMiniMessage("debug.td.placetower.fail.no-space")
        return
      }

      val newTower = inputTower.createCopy(optimalStartPosition)

      // Add tower and build
      towerDefenceInstance.addTower(newTower)
      newTower.deepCopyTower(inputTower, towerDefenceInstance.miniGameWorldInstance.instance, TowerDefenseManager.get().defaultTowerDefenseInstance)

      playerLanguage.sendMiniMessage("debug.td.placetower.success", "tower" to newTower.towerId)
    } else if (tower.towerId != inputTowerId) {
      towerDefenceInstance.replaceTowerByNew(tower, inputTower)

      playerLanguage.sendMiniMessage("debug.td.placetower.success", "tower" to inputTower.towerId)
    } else {
      // Update level
      tower.level = inputTowerLevel

      // Update building
      tower.deepCopyTower(
        inputTower,
        towerDefenceInstance.miniGameWorldInstance.instance,
        TowerDefenseManager.get().defaultTowerDefenseInstance,
        tower.towerHeight,
      )

      playerLanguage.sendMiniMessage("debug.td.placetower.success", "tower" to tower.towerId)
    }
  }

  companion object {
    private const val MAX_TARGET_DISTANCE = 7
  }
}
