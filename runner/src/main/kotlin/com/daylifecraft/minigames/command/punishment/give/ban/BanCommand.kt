package com.daylifecraft.minigames.command.punishment.give.ban

import com.daylifecraft.minigames.command.CommandsManager
import com.daylifecraft.minigames.command.PlayerFindParameter
import com.daylifecraft.minigames.command.punishment.give.GivePunishmentCommand
import com.daylifecraft.minigames.command.punishment.give.GivePunishmentPermCommand
import com.daylifecraft.minigames.command.punishment.give.GivePunishmentTempCommand
import com.daylifecraft.minigames.profile.punishment.PunishmentsManager
import net.minestom.server.command.CommandSender
import java.util.UUID

class BanCommand : GivePunishmentCommand("ban") {
  override val permission = "punishments.ban"

  init {
    addSubcommand(GivePunishmentTempCommand(this, "punishments.ban.temp"))
    addSubcommand(GivePunishmentPermCommand(this, "punishments.ban.perm"))
  }

  override fun give(
    sender: CommandSender,
    findParameter: PlayerFindParameter,
    playerString: String,
    moderatorUUID: UUID,
    reason: String,
    duration: Long,
  ) {
    when (findParameter) {
      PlayerFindParameter.USERNAME -> {
        // Ban player by username
        PunishmentsManager.banManager
          .give(playerString, sender, moderatorUUID, reason, duration)
      }

      PlayerFindParameter.UUID -> {
        // Ban player by uuid
        PunishmentsManager.banManager
          .give(UUID.fromString(playerString), sender, moderatorUUID, reason, duration)
      }

      else -> {
        CommandsManager.getSenderLanguage(sender).sendMiniMessage("punishments.fail.wrong-args")
      }
    }
  }
}
