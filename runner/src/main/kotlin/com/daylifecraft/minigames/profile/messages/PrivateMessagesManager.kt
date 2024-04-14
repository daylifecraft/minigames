package com.daylifecraft.minigames.profile.messages

import net.minestom.server.entity.Player
import java.util.UUID
import java.util.concurrent.TimeUnit

object PrivateMessagesManager {
  // Relative to the player, a list of all those from whom they received messages and
  // the time at which they received them is stored
  private val messagesDataMap: MutableMap<UUID, MutableMap<UUID, Long>> = HashMap()
  private const val DEFAULT_RECENT_TIME_MINUTES = 5L
  private val DEFAULT_RECENT_TIME = TimeUnit.MINUTES.toMillis(DEFAULT_RECENT_TIME_MINUTES)

  fun onPlayerSendMessage(sender: Player, target: Player) {
    onPlayerSendMessage(sender.uuid, target.uuid)
  }

  private fun onPlayerSendMessage(sender: UUID, target: UUID) {
    if (!messagesDataMap.containsKey(target)) {
      messagesDataMap[target] = mutableMapOf(sender to System.currentTimeMillis())
    } else {
      messagesDataMap[target]!![sender] = System.currentTimeMillis()
    }
  }

  fun isAnyRecentMessages(sender: Player, target: Player): Boolean = isAnyRecentMessages(sender.uuid, target.uuid, DEFAULT_RECENT_TIME)

  fun isAnyRecentMessages(
    sender: Player,
    target: Player,
    maximumRecentLimit: Long,
  ): Boolean = isAnyRecentMessages(sender.uuid, target.uuid, maximumRecentLimit)

  fun isAnyRecentMessages(senderUUID: UUID, targetUUID: UUID): Boolean = isAnyRecentMessages(senderUUID, targetUUID, DEFAULT_RECENT_TIME)

  private fun isAnyRecentMessages(
    senderUUID: UUID,
    targetUUID: UUID,
    maximumRecentLimit: Long,
  ): Boolean {
    val sendMessageTime =
      messagesDataMap
        .getOrDefault(senderUUID, emptyMap())
        .getOrDefault(targetUUID, 0L)

    return System.currentTimeMillis() - sendMessageTime <= maximumRecentLimit
  }
}
