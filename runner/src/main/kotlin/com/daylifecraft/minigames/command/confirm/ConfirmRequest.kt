package com.daylifecraft.minigames.command.confirm

import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.CommandContext

class ConfirmRequest(
  private val command: ConfirmableCommand,
  private val sender: CommandSender,
  private val context: CommandContext,
) {
  /**
   * Confirm the request
   *
   * @param sender sender
   */
  fun confirm(sender: CommandSender) {
    if (sender == this.sender) {
      command.confirm(sender, context)
    }
  }
}
