package com.daylifecraft.minigames.command.punishment.give

import com.daylifecraft.common.command.SubCommand
import com.daylifecraft.minigames.Init.stopServerWithError
import com.daylifecraft.minigames.command.AbstractPermissionCommand
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.CommandContext

abstract class GivePunishmentCommandType internal constructor(
  name: String,
  override val permission: String,
  private val command: GivePunishmentCommand,
  vararg aliases: String,
) : AbstractPermissionCommand(name, *aliases),
  SubCommand {
  fun onExecute(sender: CommandSender, context: CommandContext) {
    try {
      command.execute(sender, context)
    } catch (e: IllegalArgumentException) {
      stopServerWithError(e)
    }
  }
}
