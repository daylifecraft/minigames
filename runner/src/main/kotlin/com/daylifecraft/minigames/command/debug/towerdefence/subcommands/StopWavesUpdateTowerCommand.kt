package com.daylifecraft.minigames.command.debug.towerdefence.subcommands

import com.daylifecraft.minigames.minigames.instances.games.towerdefence.TowerDefenceInstance
import com.daylifecraft.minigames.text.i18n.PlayerLanguage
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.entity.Player

class StopWavesUpdateTowerCommand : AbstractTowerDefenceCommand("stoptimer") {

  override fun onCommandUse(player: Player, context: CommandContext, towerDefenceInstance: TowerDefenceInstance, playerLanguage: PlayerLanguage) {
    if (!towerDefenceInstance.waveController.needUpdateWaves) {
      playerLanguage.sendMiniMessage("debug.td.stoptimer.fail.already-stopped")
      return
    }

    towerDefenceInstance.waveController.needUpdateWaves = false
    playerLanguage.sendMiniMessage("debug.td.stoptimer.success")
  }
}
