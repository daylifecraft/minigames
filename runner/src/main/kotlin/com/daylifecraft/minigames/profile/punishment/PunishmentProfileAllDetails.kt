package com.daylifecraft.minigames.profile.punishment

import com.daylifecraft.minigames.profile.AbstractProfile
import com.daylifecraft.minigames.profile.AbstractProfileDetails
import com.daylifecraft.minigames.profile.player.PlayerProfile.Companion.getUsername
import com.daylifecraft.minigames.text.i18n.Language
import java.sql.Timestamp
import java.time.Duration

class PunishmentProfileAllDetails(profile: AbstractProfile?, language: Language) :
  AbstractProfileDetails(
    profile!!,
    language,
  ) {
  override val text: String
    get() {
      val playerUuid = profile["uuid"] as String
      val playerUsername = getUsername(playerUuid)

      val moderatorUuid = profile["moderatorUuid"] as String
      val moderatorUsername = getUsername(moderatorUuid)

      val durationSeconds = profile["duration"] as Long
      val applyTime = profile["applyTime"] as Timestamp?

      val duration = Duration.ofSeconds(durationSeconds)
      val expireTime = profile["expireTime"] as Timestamp?

      var durationString = duration.toString().substring(2)
      var expireTimeString = parseTime(expireTime.toString())

      if (durationSeconds == -1L) {
        expireTimeString = "NEVER"
        durationString = "PERMANENT"
      }

      val type =
        language.string("punishments.details.punishment-type-" + profile["type"])

      return (
        language.string("punishments.details-string1") +
          "\n" +
          "\n" +
          language
            .string(
              "punishments.details.punishment-id",
              "punishmentId" to profile["_id"].toString(),
            ) +
          "\n" +
          language.string("punishments.details.punishment-type", "punishmentType" to type) +
          "\n" +
          "\n" +
          language
            .string("punishments.details.punishment-target-player", "targetPlayer" to playerUsername) +
          "\n" +
          language
            .string(
              "punishments.details.punishment-target-player-uuid",
              "targetPlayerUuid" to playerUuid,
            ) +
          "\n" +
          "\n" +
          language
            .string(
              "punishments.details.punishment-target-moderator",
              "moderatorUsername" to moderatorUsername,
            ) +
          "\n" +
          language
            .string(
              "punishments.details.punishment-target-moderator-uuid",
              "moderatorUuid" to moderatorUuid,
            ) +
          "\n" +
          language
            .string(
              "punishments.details.punishment-reason",
              "punishmentReason" to profile["reason"].toString(),
            ) +
          "\n" +
          language
            .string(
              "punishments.details.punishment-internal-note",
              "punishmentInternalNote" to profile["internalNote"].toString(),
            ) +
          "\n" +
          "\n" +
          language
            .string("punishments.details.punishment-duration", "punishmentDuration" to durationString) +
          "\n" +
          language
            .string(
              "punishments.details.punishment-apply-date",
              "punishmentApplyDate" to parseTime(applyTime.toString()),
            ) +
          "\n" +
          language
            .string(
              "punishments.details.punishment-expire-date",
              "punishmentExpireDate" to expireTimeString,
            ) +
          "\n" +
          "\n" +
          language
            .string(
              "punishments.details.punishment-quick-actions",
              "punishmentId" to profile["_id"].toString(),
            ) + "\n"
        )
    }
}
