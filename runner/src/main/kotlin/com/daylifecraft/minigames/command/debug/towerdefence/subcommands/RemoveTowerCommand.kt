package com.daylifecraft.minigames.command.debug.towerdefence.subcommands

import com.daylifecraft.minigames.minigames.instances.games.towerdefence.TowerData
import com.daylifecraft.minigames.minigames.instances.games.towerdefence.TowerDefenceInstance
import com.daylifecraft.minigames.text.i18n.PlayerLanguage
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.entity.Player

class RemoveTowerCommand : AbstractTowerDefenceCommand("removetower") {

  override fun onCommandUse(player: Player, context: CommandContext, towerDefenceInstance: TowerDefenceInstance, playerLanguage: PlayerLanguage) {
    var targetBlockPosition = player.getTargetBlockPosition(MAX_TARGET_DISTANCE)

    if (targetBlockPosition == null) {
      playerLanguage.sendMiniMessage("debug.td.placetower.fail.air-position")
      return
    }

    val tower: TowerData? = towerDefenceInstance.findTowerData(targetBlockPosition)

    if (tower == null) {
      playerLanguage.sendMiniMessage("debug.td.removetower.fail.not-tower")
      return
    }

    towerDefenceInstance.removeTower(tower, clearBlocks = true)

    playerLanguage.sendMiniMessage("debug.td.removetower.success")
  }

  companion object {
    private const val MAX_TARGET_DISTANCE = 7
  }
}
