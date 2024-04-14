package com.daylifecraft.minigames.command.rounds

import com.daylifecraft.common.command.SubCommand
import com.daylifecraft.minigames.command.AbstractPermissionCommand
import com.daylifecraft.minigames.minigames.PlayerMiniGameManager
import com.daylifecraft.minigames.text.i18n.PlayerLanguage
import net.minestom.server.command.CommandSender
import net.minestom.server.entity.Player

/** Allows you to exit the monitoring mode, after exiting, it takes you to the lobby  */
class RoundsSpectateExitCommand internal constructor() :
  AbstractPermissionCommand("exit"),
  SubCommand {
  override val permission = "rounds.spectate"

  init {
    val condition = condition
    setCondition { sender: CommandSender?, commandString: String? ->
      if (condition == null) {
        return@setCondition false
      }
      return@setCondition condition.canUse(sender!!, commandString) &&
        PlayerMiniGameManager.getMiniGameRoundBySpectator(sender as Player?) != null
    }

    addConditionalSyntax(getCondition(), { sender, _ ->
      onExecute(sender)
    })
  }

  private fun onExecute(sender: CommandSender) {
    val player = sender as Player

    val spectatedRound =
      PlayerMiniGameManager.getMiniGameRoundBySpectator(player)

    val playerLanguage = PlayerLanguage.get(player)
    if (spectatedRound == null) {
      return
    }

    spectatedRound.removeSpectator(player)
    playerLanguage.sendMiniMessage("commands.rounds.spectate-exit.success")
    player.refreshCommands()
  }
}
