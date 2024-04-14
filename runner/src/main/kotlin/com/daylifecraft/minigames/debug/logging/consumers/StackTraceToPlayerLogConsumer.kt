package com.daylifecraft.minigames.debug.logging.consumers

import com.daylifecraft.common.logging.foundation.LogRecord
import com.daylifecraft.minigames.Dev
import com.daylifecraft.minigames.util.sendStackTrace
import net.minestom.server.MinecraftServer
import java.util.function.Consumer

// TODO move it to common when isDev could be reached from there
class StackTraceToPlayerLogConsumer : Consumer<LogRecord> {
  override fun accept(logRecord: LogRecord) {
    if (Dev.LOGGER_PLAYER_MINECRAFT_CHAT) {
      logRecord.playerUuid?.let { playerUuid ->
        MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(playerUuid)?.let { player ->
          sendStackTrace(player)
        }
      }
    }
  }
}
