package com.daylifecraft.minigames

import com.daylifecraft.minigames.config.ConfigManager.mainConfig
import com.daylifecraft.minigames.profile.group.PlayersGroup
import com.daylifecraft.minigames.util.ChatUtil.GLOBAL_CHAT_PLAYER_USERNAME
import com.daylifecraft.minigames.util.ChatUtil.getGlobalChatMessageColorFromPlayer
import com.daylifecraft.minigames.util.ChatUtil.getGlobalChatPlayerBadge
import com.daylifecraft.minigames.util.ChatUtil.sendMiniMessage
import net.minestom.server.MinecraftServer
import net.minestom.server.entity.Player

private const val SENDER_USERNAME_KEY = "senderUsername"
private const val SENDER_UUID_KEY = "senderUUID"
private const val RECIPIENT_USERNAME_KEY = "recipientUsername"
private const val RECIPIENT_UUID_KEY = "recipientUUID"
private const val CHAT_MESSAGE_TEXT_KEY = "chatMessageText"

/**
 * used to manage chat and make it more convenient
 *
 * @author FazziCLAY
 */
class ChatManager {
  // TODO create dto
  private val simpyChatPattern = mainConfig!!.getString("chat.global.pattern")!!
  private val groupMemberPattern = mainConfig!!.getString("chat.group.members_pattern")!!
  private val groupLeaderPattern = mainConfig!!.getString("chat.group.leader_pattern")!!
  private val privateMessageFromDefaultPattern = mainConfig!!.getString("chat.privateMessages.from_default_pattern")!!
  private val privateMessageToDefaultPattern = mainConfig!!.getString("chat.privateMessages.to_default_pattern")!!
  private val privateMessageFromAdminPattern = mainConfig!!.getString("chat.privateMessages.from_admin_pattern")!!
  private val privateMessageToAdminPattern = mainConfig!!.getString("chat.privateMessages.to_admin_pattern")!!

  /**
   * send a message from player to player and format for chat
   *
   * @param sender the player who sends the message (whose name will be used)
   * @param recipient player that will receive message
   * @param message message to be sent
   */
  fun sendPlayerChatMessage(
    sender: Player,
    recipient: Player,
    message: String,
  ) {
    sendMiniMessage(
      recipient,
      getMiniMessageForChat(sender),
      SENDER_USERNAME_KEY to sender.username,
      SENDER_UUID_KEY to sender.uuid.toString(),
      RECIPIENT_USERNAME_KEY to recipient.username,
      RECIPIENT_UUID_KEY to recipient.uuid.toString(),
      CHAT_MESSAGE_TEXT_KEY to message,
    )
  }

  /**
   * send a message from player to group and format for group chat
   *
   * @param sender the player who sends the message (whose name will be used)
   * @param playersGroup group that will receive message
   * @param message message to be sent
   */
  fun sendGroupChatMessage(
    sender: Player,
    playersGroup: PlayersGroup,
    message: String,
  ) {
    for (recipientUUID in playersGroup.getAllPlayersUUIDs()) {
      val recipient =
        MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(recipientUUID) ?: continue

      sendMiniMessage(
        recipient,
        getMiniMessageForGroup(
          sender,
          playersGroup.playerLeaderUUID == sender.uuid,
        ),
        SENDER_USERNAME_KEY to sender.username,
        SENDER_UUID_KEY to sender.uuid.toString(),
        CHAT_MESSAGE_TEXT_KEY to message,
      )
    }
  }

  /**
   * send a message from player to group and format for group chat
   *
   * @param sender the player who sends the message (whose name will be used)
   * @param targetPlayer group that will receive message
   * @param isSenderAdmin a value that determines whether the sender player is an admin
   * @param isTargetAdmin a value that determines whether the target player is an admin
   * @param messageText message to be sent
   */
  fun sendPrivateMessage(
    sender: Player,
    targetPlayer: Player,
    isSenderAdmin: Boolean,
    isTargetAdmin: Boolean,
    messageText: String,
  ) {
    val isStaffPm = isTargetAdmin || isSenderAdmin

    sendMiniMessage(
      targetPlayer,
      getMiniPrivateMessage(sender, isStaffPm, isSenderAdmin, "from"),
      SENDER_USERNAME_KEY to sender.username,
      SENDER_UUID_KEY to sender.uuid.toString(),
      CHAT_MESSAGE_TEXT_KEY to messageText,
    )

    sendMiniMessage(
      sender,
      getMiniPrivateMessage(targetPlayer, isStaffPm, isSenderAdmin, "to"),
      SENDER_USERNAME_KEY to targetPlayer.username,
      SENDER_UUID_KEY to targetPlayer.uuid.toString(),
      CHAT_MESSAGE_TEXT_KEY to messageText,
    )
  }

  private fun getMiniMessageForChat(badge: String, messageColor: String): String {
    // TODO here code copy from getMiniMessage()
    return simpyChatPattern
      .replace("$($SENDER_USERNAME_KEY)", GLOBAL_CHAT_PLAYER_USERNAME)
      .replace("$(senderBadge)", badge)
      .replace("$(chatMessageColor)", messageColor)
  }

  private fun getMiniMessageForChat(sender: Player): String = getMiniMessageForChat(
    getGlobalChatPlayerBadge(sender),
    getGlobalChatMessageColorFromPlayer(sender),
  )

  private fun getMiniMessageForGroup(sender: Player, isLeader: Boolean): String {
    val chatPattern =
      if (isLeader) {
        groupLeaderPattern
      } else {
        groupMemberPattern
      }

    return getMiniMessage(sender, chatPattern)
  }

  private fun getMiniPrivateMessage(
    player: Player,
    isStaffPm: Boolean,
    isSenderAdmin: Boolean,
    messageType: String,
  ): String {
    val chatPattern: String?
    val messageColor: String
    when (messageType) {
      "from" -> {
        chatPattern =
          if (isStaffPm) {
            privateMessageFromAdminPattern
          } else {
            privateMessageFromDefaultPattern
          }

        messageColor =
          if (isSenderAdmin) {
            "#aa00aa"
          } else {
            getGlobalChatMessageColorFromPlayer(player)
          }
      }

      "to" -> {
        chatPattern =
          if (isStaffPm) {
            privateMessageToAdminPattern
          } else {
            privateMessageToDefaultPattern
          }

        messageColor =
          if (isSenderAdmin) {
            "#800080"
          } else {
            getGlobalChatMessageColorFromPlayer(player)
          }
      }

      else -> {
        return ""
      }
    }

    return getMiniMessage(
      chatPattern,
      getGlobalChatPlayerBadge(player),
      messageColor,
    )
  }

  private fun getMiniMessage(
    chatPattern: String,
    badge: String,
    messageColor: String,
  ): String = chatPattern
    .replace("$($SENDER_USERNAME_KEY)", GLOBAL_CHAT_PLAYER_USERNAME)
    .replace("$(senderBadge)", badge)
    .replace("$(chatMessageColor)", messageColor)

  private fun getMiniMessage(sender: Player, chatPattern: String): String = getMiniMessage(
    chatPattern,
    getGlobalChatPlayerBadge(sender),
    getGlobalChatMessageColorFromPlayer(sender),
  )
}
