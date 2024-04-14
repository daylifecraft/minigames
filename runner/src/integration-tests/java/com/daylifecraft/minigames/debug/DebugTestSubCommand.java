package com.daylifecraft.minigames.debug;

import com.daylifecraft.common.command.SubCommand;
import com.daylifecraft.minigames.command.debug.AbstractDebugCommand;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.number.ArgumentInteger;

public class DebugTestSubCommand extends AbstractDebugCommand implements SubCommand {
  DebugTestSubCommand() {
    super("test");
    addSyntax(this::onExecute, new ArgumentInteger("TestInteger"));
  }

  @Override
  public void onCommandUse(final CommandSender sender, final CommandContext context) {
    int i = context.get("TestInteger");
    i++;
    sendMessage(sender, "correct_command", context);
  }
}
