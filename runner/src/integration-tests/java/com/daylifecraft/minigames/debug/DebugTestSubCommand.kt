package com.daylifecraft.minigames.debug

import com.daylifecraft.common.command.SubCommand
import com.daylifecraft.minigames.command.debug.AbstractDebugCommand
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.arguments.number.ArgumentInteger

class DebugTestSubCommand internal constructor() :
  AbstractDebugCommand("test"),
  SubCommand {

  init {
    addSyntax(::onExecute, ArgumentInteger("TestInteger"))
  }

  public override fun onCommandUse(sender: CommandSender, context: CommandContext) {
    var i = context.get<Int>("TestInteger")
    i++
    sendMessage(sender, "correct_command $i", context)
  }
}
