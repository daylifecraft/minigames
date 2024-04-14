package com.daylifecraft.minigames.command.punishment

import com.daylifecraft.minigames.command.AbstractPermissionCommand

class PunishmentCommand : AbstractPermissionCommand("punishment") {
  override val permission = "punishments"

  init {
    addSubcommand(PunishmentExpireCommand())
    addSubcommand(PunishmentViewDetailsCommand())
    addSubcommand(PunishmentSetNoteCommand())
    addSubcommand(PunishmentListCommand())
  }
}
