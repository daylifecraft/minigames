package com.daylifecraft.minigames.command.punishment

import com.daylifecraft.common.util.TimeUtil.formatSecondsToString
import com.daylifecraft.minigames.PlayerManager
import com.daylifecraft.minigames.command.CommandsManager.getSenderLanguage
import com.daylifecraft.minigames.database.DatabaseManager
import com.daylifecraft.minigames.profile.player.PlayerProfile
import com.daylifecraft.minigames.profile.punishment.PunishmentProfile
import com.daylifecraft.minigames.profile.punishment.PunishmentType
import com.daylifecraft.minigames.profile.punishment.PunishmentsManager
import net.kyori.adventure.text.minimessage.MiniMessage
import net.minestom.server.command.CommandSender
import net.minestom.server.entity.Player
import java.util.UUID

// TODO research this magic number and what it means
private const val NOT_SEND_TIME = -2L

abstract class AbstractPunishmentManager {
  fun show(player: Player?, totalEndTime: Long) {
    if (player == null) {
      return
    }

    val totalEndTimeText = formatSecondsToString(totalEndTime)

    if (totalEndTime == -1L) {
      // Show perm punishment
      showPerm(player)
    } else {
      // Show temp punishment
      showTemp(player, totalEndTimeText)
    }
  }

  protected abstract fun showTemp(player: Player, totalEndTime: String)

  protected abstract fun showPerm(player: Player)

  fun give(
    username: String,
    sender: CommandSender,
    moderator: UUID,
    reason: String,
    duration: Long,
  ) {
    val uuid = PlayerManager.getPlayerUuid(username)

    give(uuid, sender, moderator, reason, duration)
  }

  protected abstract fun give(
    uuid: UUID,
    sender: CommandSender,
    moderator: UUID,
    reason: String,
    duration: Long,
  )

  protected fun give(
    playerUuid: UUID,
    sender: CommandSender,
    moderator: UUID,
    reason: String?,
    type: PunishmentType,
    typeName: String,
    duration: Long,
  ): PunishmentProfile {
    val playerProfile = DatabaseManager.getPlayerProfile("uuid", playerUuid.toString())

    val punishmentProfile =
      PunishmentProfile.createProfile(playerUuid, moderator, duration, type, reason, "")

    DatabaseManager.addProfile(punishmentProfile, "punishments")

    val language = getSenderLanguage(sender)

    val message =
      language.string(
        "punishments.apply.success",
        "targetPlayer" to (PlayerProfile.getUsername(playerUuid) ?: "Could not find username"),
        "targetPlayerUuid" to playerUuid.toString(),
        "punishmentType" to language.string("punishments.apply.success-$typeName"),
      )

    sender.sendMessage(MiniMessage.miniMessage().deserialize(message))

    if (playerProfile != null) {
      val totalEndTime = PunishmentsManager.getTotalEndTime(playerUuid, type)

      if (totalEndTime != NOT_SEND_TIME) {
        show(PlayerManager.getPlayerByUuid(playerUuid), totalEndTime)
      }
    }

    return punishmentProfile
  }
}
