package com.daylifecraft.minigames.command.debug

import com.daylifecraft.minigames.command.debug.player.DebugFlyCommand
import com.daylifecraft.minigames.command.debug.player.DebugSpeedCommand
import com.daylifecraft.minigames.command.debug.player.DebugToggleAdminCommand
import com.daylifecraft.minigames.command.debug.rounds.DebugRoundsCommand
import com.daylifecraft.minigames.command.debug.seasons.DebugSeasonsCommand
import com.daylifecraft.minigames.command.debug.server.DebugLagCommand
import com.daylifecraft.minigames.command.debug.server.DebugStopCommand
import com.daylifecraft.minigames.command.debug.towerdefence.DebugTowerDefenceCommand
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.CommandContext

/** Command to perform debugging  */
class DebugCommand : AbstractDebugCommand("~") {
  init {
    addSyntax(this::onExecute)

    addSubcommand(DebugRoundsCommand())
    addSubcommand(DebugStopCommand())
    addSubcommand(DebugSeasonsCommand())
    addSubcommand(DebugFlyCommand())
    addSubcommand(DebugSpeedCommand())
    addSubcommand(DebugToggleAdminCommand())
    addSubcommand(DebugLagCommand())
    addSubcommand(DebugTowerDefenceCommand())
  }

  /**
   * This method will be called when the command is used
   *
   * @param sender who send command
   * @param context command context
   */
  override fun onCommandUse(sender: CommandSender, context: CommandContext) {
    sendMessage(sender, "debug.command.usage", context)
  }
}
