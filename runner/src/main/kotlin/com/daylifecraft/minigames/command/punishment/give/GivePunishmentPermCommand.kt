package com.daylifecraft.minigames.command.punishment.give

import net.minestom.server.command.builder.arguments.ArgumentType

class GivePunishmentPermCommand(command: GivePunishmentCommand, permission: String) : GivePunishmentCommandType("permanent", permission, command, "perm") {
  init {
    addSyntax(
      this::onExecute,
      ArgumentType.String("player"),
      ArgumentType.StringArray("reason"),
    )
  }
}
