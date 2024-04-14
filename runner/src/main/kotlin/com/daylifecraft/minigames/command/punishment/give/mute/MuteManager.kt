package com.daylifecraft.minigames.command.punishment.give.mute

import com.daylifecraft.minigames.PermissionManager
import com.daylifecraft.minigames.command.CommandsManager
import com.daylifecraft.minigames.command.punishment.AbstractPunishmentManager
import com.daylifecraft.minigames.command.punishment.give.mute.MuteCommand.Companion.canMute
import com.daylifecraft.minigames.database.DatabaseManager
import com.daylifecraft.minigames.profile.punishment.PunishmentType
import com.daylifecraft.minigames.text.i18n.PlayerLanguage
import net.minestom.server.command.CommandSender
import net.minestom.server.entity.Player
import java.util.UUID

class MuteManager : AbstractPunishmentManager() {
  public override fun showTemp(player: Player, totalEndTime: String) {
    PlayerLanguage.get(player)
      .sendMiniMessage("message.send-fail.temp-muted", "muteTotalExpireDate" to totalEndTime)
  }

  public override fun showPerm(player: Player) {
    PlayerLanguage.get(player).sendMiniMessage("message.send-fail.perm-muted")
  }

  public override fun give(
    uuid: UUID,
    sender: CommandSender,
    moderator: UUID,
    reason: String,
    duration: Long,
  ) {
    val senderLanguage = CommandsManager.getSenderLanguage(sender)

    if (sender is Player &&
      !PermissionManager.hasPermission(sender, "punishments.mute.protection-ignore")
    ) {
      val isAnyDuration: Boolean =
        PermissionManager.hasPermission(sender, MuteCommand.ANY_DURATION)

      if (!isAnyDuration && !canMute(sender)) {
        senderLanguage.sendMiniMessage("punishments.mute.fail.too-fast")
        return
      }

      val playerProfile = DatabaseManager.getPlayerProfile("uuid", uuid.toString())!!

      for (group in playerProfile.permissions!!) {
        if (PermissionManager.hasPermission(group, "punishments.mute.protected")) {
          senderLanguage.sendMiniMessage("punishments.mute.fail.protected")
          return
        }
      }
    }

    if (uuid == CommandsManager.getUuidFromSender(sender)) {
      senderLanguage.sendMiniMessage("punishments.mute.fail.self")
      return
    }

    give(uuid, sender, moderator, reason, PunishmentType.MUTE, "muted", duration)
  }
}
