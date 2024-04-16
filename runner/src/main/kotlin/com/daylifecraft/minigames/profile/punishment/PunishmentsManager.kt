package com.daylifecraft.minigames.profile.punishment

import com.daylifecraft.common.util.TimeUtil.MILLIS_IN_A_SECOND
import com.daylifecraft.common.util.TimeUtil.currentUtcSeconds
import com.daylifecraft.minigames.Init.stopServerWithError
import com.daylifecraft.minigames.PlayerManager.getPlayerUuid
import com.daylifecraft.minigames.command.punishment.AbstractPunishmentManager
import com.daylifecraft.minigames.command.punishment.give.ban.BanManager
import com.daylifecraft.minigames.command.punishment.give.mute.MuteManager
import com.daylifecraft.minigames.database.DatabaseManager.getPunishmentProfiles
import java.util.UUID
import kotlin.math.max

object PunishmentsManager {
  val muteManager: MuteManager = MuteManager()
  val banManager: BanManager = BanManager()

  fun getList(username: String?): List<PunishmentProfile> {
    // TODO 29.07.2023 WTF? use encoding and get by other methods (from DB)
    val uuid = getPlayerUuid(username!!)

    try {
      return getPunishmentProfiles(uuid)
    } catch (e: IllegalArgumentException) {
      stopServerWithError(e)
    }
  }

  fun getList(uuid: UUID?): List<PunishmentProfile> {
    try {
      return getPunishmentProfiles(uuid!!)
    } catch (e: IllegalArgumentException) {
      stopServerWithError(e)
    }
  }

  private fun getTotalEndTime(type: PunishmentType, profiles: List<PunishmentProfile>): Long {
    val totalDuration = getTotalDuration(type, profiles, false)

    if (totalDuration == -1L) {
      return -1
    }

    val totalEndTime = getTotalStartTime(type, profiles) + totalDuration

    return if (totalEndTime == 0L) {
      currentUtcSeconds()
    } else {
      totalEndTime
    }
  }

  private fun getTotalDuration(
    type: PunishmentType,
    profiles: List<PunishmentProfile>,
    ignorePermDuration: Boolean,
  ): Long {
    var totalDuration: Long = 0

    for (profile in profiles) {
      if (profile.isExpired && type.typeName == profile.type) {
        if (profile.duration == -1L && !ignorePermDuration) {
          return profile.duration
        } else if (profile.duration != -1L) {
          totalDuration += profile.duration
        }
      }
    }

    return totalDuration
  }

  private fun getTotalStartTime(type: PunishmentType, profiles: List<PunishmentProfile>): Long {
    var totalStartTime: Long = 0

    for (profile in profiles) {
      val startTime = profile.startTime.time / MILLIS_IN_A_SECOND
      if (profile.isExpired && type.typeName == profile.type && profile.duration != -1L) {
        totalStartTime = max(totalStartTime.toDouble(), startTime.toDouble()).toLong()
      }
    }

    return if (totalStartTime == 0L) {
      currentUtcSeconds()
    } else {
      totalStartTime
    }
  }

  fun getPunishmentManager(type: PunishmentType): AbstractPunishmentManager = when (type) {
    PunishmentType.MUTE -> muteManager
    PunishmentType.BAN -> banManager
  }

  fun getTotalEndTime(uuid: UUID?, type: PunishmentType): Long = getTotalEndTime(type, getList(uuid))

  fun getTotalDuration(
    uuid: UUID?,
    type: PunishmentType,
    ignorePermDuration: Boolean,
  ): Long = getTotalDuration(type, getList(uuid), ignorePermDuration)

  fun getTotalStartTime(uuid: UUID?, type: PunishmentType): Long = getTotalStartTime(type, getList(uuid))
}
