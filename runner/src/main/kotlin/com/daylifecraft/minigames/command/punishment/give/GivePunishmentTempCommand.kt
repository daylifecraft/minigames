package com.daylifecraft.minigames.command.punishment.give

import net.minestom.server.command.builder.arguments.ArgumentType

class GivePunishmentTempCommand(command: GivePunishmentCommand?, permission: String?) : GivePunishmentCommandType("temporary", permission!!, command!!, "temp") {
  init {
    addSyntax(
      this::onExecute,
      ArgumentType.Time("duration"),
      ArgumentType.String("player"),
      ArgumentType.StringArray("reason"),
    )
  }
}
