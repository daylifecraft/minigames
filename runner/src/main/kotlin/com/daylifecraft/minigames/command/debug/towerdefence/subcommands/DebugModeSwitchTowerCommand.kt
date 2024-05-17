package com.daylifecraft.minigames.command.debug.towerdefence.subcommands

import com.daylifecraft.minigames.minigames.instances.games.towerdefence.TowerDefenceInstance
import com.daylifecraft.minigames.text.i18n.PlayerLanguage
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.entity.Player

class DebugModeSwitchTowerCommand : AbstractTowerDefenceCommand("debug") {

  init {
      addSyntax(this::onExecute)
  }

  override fun onCommandUse(player: Player, context: CommandContext, towerDefenceInstance: TowerDefenceInstance, playerLanguage: PlayerLanguage) {
    try {
      val newStatus = towerDefenceInstance.switchPlayerDebugModeStatus(player)

      val messageKey = if(newStatus) "debug.td.debugtoggle.success.enabled" else "debug.td.debugtoggle.success.disabled"
      playerLanguage.sendMiniMessage(messageKey)
    }catch (nullPointer: NullPointerException) {
      // TODO 29.04.2024
    }
  }

}
