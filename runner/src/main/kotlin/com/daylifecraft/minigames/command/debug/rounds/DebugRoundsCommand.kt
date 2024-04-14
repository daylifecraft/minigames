package com.daylifecraft.minigames.command.debug.rounds

import com.daylifecraft.common.command.SubCommand
import com.daylifecraft.minigames.command.debug.AbstractDebugCommand
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.CommandContext

/** Command to work with debug rounds  */
class DebugRoundsCommand :
  AbstractDebugCommand("round"),
  SubCommand {

  init {
    addSubcommand(DebugRoundStartCommand())
  }

  override fun onCommandUse(sender: CommandSender, context: CommandContext) {
    // Do nothing
  }
}
