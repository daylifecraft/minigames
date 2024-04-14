package com.daylifecraft.minigames.profile.settings

import com.daylifecraft.common.logging.building.createLogger
import com.daylifecraft.minigames.database.DatabaseManager
import net.minestom.server.entity.Player

enum class OnlineStatus(val dbKey: String) {
  ONLINE("online"),
  FRIENDS_ONLY("friends-only"),
  OFFLINE("offline"),
  ;

  companion object {
    private val LOGGER = createLogger<OnlineStatus>()

    fun ofDBKey(key: String): OnlineStatus {
      for (value in entries) {
        if (value.dbKey == key) return value
      }

      LOGGER.debug(
        "WARNING: OnlineStatus.ofDBKey() received unknown key=$key" +
          ". Returned default value=ONLINE",
      )
      return ONLINE
    }

    fun valueOf(player: Player): OnlineStatus {
      val dbKey = DatabaseManager.getPlayerProfile(player)!!.settings!!.onlineStatus
      return ofDBKey(dbKey)
    }
  }
}
