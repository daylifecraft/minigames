package com.daylifecraft.minigames.command.debug.seasons

import com.daylifecraft.common.command.SubCommand
import com.daylifecraft.minigames.command.debug.AbstractDebugCommand
import com.daylifecraft.minigames.command.debug.seasons.subcommands.DebugSeasonsListCommand
import com.daylifecraft.minigames.command.debug.seasons.subcommands.DebugSeasonsPrioritizeCommand
import com.daylifecraft.minigames.command.debug.seasons.subcommands.DebugSeasonsStartCommand
import com.daylifecraft.minigames.command.debug.seasons.subcommands.DebugSeasonsStopCommand
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.CommandContext

/** '/~ seasons' command executor  */
class DebugSeasonsCommand :
  AbstractDebugCommand("seasons"),
  SubCommand {

  init {
    addSubcommand(DebugSeasonsStartCommand())
    addSubcommand(DebugSeasonsStopCommand())
    addSubcommand(DebugSeasonsListCommand())
    addSubcommand(DebugSeasonsPrioritizeCommand())
  }

  override fun onCommandUse(sender: CommandSender, context: CommandContext) {
    // Do nothing
  }
}
