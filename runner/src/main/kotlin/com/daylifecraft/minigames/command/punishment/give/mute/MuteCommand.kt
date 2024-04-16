package com.daylifecraft.minigames.command.punishment.give.mute

import com.daylifecraft.minigames.Init.stopServerWithError
import com.daylifecraft.minigames.PermissionManager
import com.daylifecraft.minigames.command.CommandsManager
import com.daylifecraft.minigames.command.PlayerFindParameter
import com.daylifecraft.minigames.command.punishment.give.GivePunishmentCommand
import com.daylifecraft.minigames.command.punishment.give.GivePunishmentPermCommand
import com.daylifecraft.minigames.command.punishment.give.GivePunishmentTempCommand
import com.daylifecraft.minigames.database.DatabaseManager
import com.daylifecraft.minigames.profile.punishment.PunishmentProfile
import com.daylifecraft.minigames.profile.punishment.PunishmentsManager
import com.daylifecraft.minigames.text.i18n.PlayerLanguage
import net.minestom.server.command.CommandSender
import net.minestom.server.entity.Player
import java.time.Instant
import java.util.UUID
import java.util.concurrent.TimeUnit

class MuteCommand : GivePunishmentCommand("mute") {
  override val permission = "punishments.mute"

  init {
    addSubcommand(GivePunishmentPermCommand(this, "punishments.mute.perm"))
    addSubcommand(GivePunishmentTempCommand(this, "punishments.mute.temp"))
  }

  override fun give(
    sender: CommandSender,
    findParameter: PlayerFindParameter,
    playerString: String,
    moderatorUUID: UUID,
    reason: String,
    duration: Long,
  ) {
    if (sender is Player) {
      val playerLanguage = PlayerLanguage.get(sender)

      val isAnyAmount: Boolean = PermissionManager.hasPermission(sender, ANY_AMOUNT)

      if (!isAnyAmount && duration >= AMOUNT_LIMIT) {
        playerLanguage.sendMiniMessage("punishments.mute.fail.too-long")
        return
      }
    }

    when (findParameter) {
      PlayerFindParameter.USERNAME -> {
        // Mute player by username
        PunishmentsManager.muteManager
          .give(playerString, sender, moderatorUUID, reason, duration)
      }

      PlayerFindParameter.UUID -> {
        // Mute player by uuid
        PunishmentsManager.muteManager
          .give(UUID.fromString(playerString), sender, moderatorUUID, reason, duration)
      }

      else -> {
        CommandsManager.getSenderLanguage(sender).sendMiniMessage("punishments.fail.wrong-args")
      }
    }
  }

  companion object {
    const val ANY_DURATION: String = "punishments.mute.limit.any-duration"
    private const val ANY_AMOUNT = "punishments.mute.limit.any-amount"

    private val AMOUNT_LIMIT = TimeUnit.DAYS.toSeconds(2)
    private val DURATION_LIMIT = TimeUnit.MINUTES.toMillis(15)

    /**
     * Checks if player has permission to mute
     *
     * @param player player to ban uuid
     */
    fun canMute(player: Player): Boolean {
      val profiles: List<PunishmentProfile>
      try {
        profiles = DatabaseManager.getPunishmentProfiles("moderatorUuid", player.uuid.toString())
      } catch (e: IllegalArgumentException) {
        stopServerWithError(e)
      }

      for (profile in profiles) {
        if (profile.applyTime.time + DURATION_LIMIT > Instant.now().toEpochMilli()) {
          return false
        }
      }

      return true
    }
  }
}
