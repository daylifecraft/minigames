package com.daylifecraft.minigames.command.debug.towerdefence.subcommands

import com.daylifecraft.minigames.minigames.instances.games.towerdefence.TowerDefenceInstance
import com.daylifecraft.minigames.text.i18n.PlayerLanguage
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.entity.Player

class StartWavesUpdateTowerCommand : AbstractTowerDefenceCommand("starttimer") {

  override fun onCommandUse(player: Player, context: CommandContext, towerDefenceInstance: TowerDefenceInstance, playerLanguage: PlayerLanguage) {
    if (towerDefenceInstance.waveController.needUpdateWaves) {
      playerLanguage.sendMiniMessage("debug.td.starttimer.fail.already-started")
      return
    }

    towerDefenceInstance.waveController.needUpdateWaves = true
    playerLanguage.sendMiniMessage("debug.td.starttimer.success")
  }
}
