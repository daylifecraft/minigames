package com.daylifecraft.minigames.profile.punishment

import com.daylifecraft.minigames.profile.AbstractProfile
import com.daylifecraft.minigames.profile.AbstractProfileDetails
import com.daylifecraft.minigames.text.i18n.Language
import java.sql.Timestamp
import java.time.Instant

class PunishmentProfileDetails(
  profile: AbstractProfile,
  language: Language,
) : AbstractProfileDetails(profile, language) {
  private val AbstractProfile.applyTime: String
    get() = this["applyTime"].toString()

  private val AbstractProfile.expireTime: Timestamp
    get() = this["expireTime"] as Timestamp

  private val AbstractProfile.duration: Long
    get() = this["duration"] as Long

  private val AbstractProfile.isForceExpired: Boolean
    get() = this["isForceExpired"] as Boolean

  private val AbstractProfile.isExpired: Boolean
    get() = expireTime.time >= Instant.now().toEpochMilli()

  override val text: String
    get() {
      val applyTime = parseTime(profile.applyTime)
      var expireTime = parseTime(profile.expireTime.toString())

      val expireColor =
        when {
          profile.duration == -1L -> {
            expireTime = language.string("punishments.list-description-never")
            if (profile.isForceExpired) "green" else "red"
          }

          profile.isExpired && !profile.isForceExpired -> "white"

          else -> "green"
        }

      return language
        .string(
          "punishments.list-description",
          "punishmentType" to (profile["type"] as String?)!!.uppercase().substring(0, 1),
          "punishmentApplyDate" to applyTime,
          "punishmentExpireDate" to "<$expireColor>$expireTime</$expireColor>",
          "punishmentId" to profile.id.toString(),
        )
    }
}
