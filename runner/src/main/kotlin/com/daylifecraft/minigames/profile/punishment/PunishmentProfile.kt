package com.daylifecraft.minigames.profile.punishment

import com.daylifecraft.common.util.TimeUtil.MILLIS_IN_A_SECOND
import com.daylifecraft.common.util.TimeUtil.currentUtcSeconds
import com.daylifecraft.minigames.profile.AbstractProfile
import com.daylifecraft.minigames.profile.AbstractProfileDetails
import com.daylifecraft.minigames.profile.punishment.PunishmentsManager.getTotalDuration
import com.daylifecraft.minigames.profile.punishment.PunishmentsManager.getTotalStartTime
import com.daylifecraft.minigames.text.i18n.Language
import org.bson.types.ObjectId
import java.sql.Timestamp
import java.time.Instant
import java.util.UUID

class PunishmentProfile(
  id: ObjectId?,
  val uuid: String,
  val moderatorUuid: String,
  val type: String,
  val reason: String?,
  private var internalNote: String,
  isForceExpired: Boolean,
  val duration: Long,
  applyTime: Timestamp,
  startTime: Timestamp,
  expireTime: Timestamp?,
) : AbstractProfile(id!!, 1, "punishments") {
  val applyTime: Timestamp = Timestamp(applyTime.time)
  val startTime: Timestamp = Timestamp(startTime.time)
  private var allDetails = false
  var isForceExpired: Boolean = isForceExpired
    private set

  init {
    parameters["uuid"] = uuid
    parameters["type"] = type
    parameters["applyTime"] = applyTime
    parameters["startTime"] = startTime
    parameters["duration"] = duration
    parameters["isForceExpired"] = isForceExpired
    parameters["moderatorUuid"] = moderatorUuid
    parameters["reason"] = reason
    parameters["internalNote"] = internalNote
    parameters["expireTime"] = expireTime
  }

  fun setInternalNote(internalNote: String) {
    this.internalNote = internalNote
    set("internalNote", internalNote)
  }

  val isExpired: Boolean
    get() =
      !isForceExpired && ((startTime.time / MILLIS_IN_A_SECOND + duration) > currentUtcSeconds() || duration == -1L)

  fun forceExpire(): Boolean {
    if (isForceExpired) {
      return false
    }
    isForceExpired = true
    set("isForceExpired", true)
    return true
  }

  fun setMoreDetails() {
    allDetails = true
  }

  fun setLessDetails() {
    allDetails = false
  }

  override fun getDetails(language: Language): AbstractProfileDetails = if (allDetails) {
    PunishmentProfileAllDetails(this, language)
  } else {
    PunishmentProfileDetails(this, language)
  }

  companion object {
    fun createProfile(
      player: UUID,
      moderator: UUID,
      duration: Long,
      type: PunishmentType,
      reason: String?,
      internalNote: String,
    ): PunishmentProfile {
      val startTime = getTotalStartTime(player, type)

      val expireTime =
        Timestamp.from(
          Instant.ofEpochSecond(
            getTotalDuration(player, type, true) + startTime + duration,
          ),
        )

      return PunishmentProfile(
        ObjectId(),
        player.toString(),
        moderator.toString(),
        type.typeName,
        reason,
        internalNote,
        false,
        duration,
        Timestamp.from(Instant.ofEpochSecond(currentUtcSeconds())),
        Timestamp.from(Instant.ofEpochSecond(startTime)),
        expireTime,
      )
    }
  }
}
