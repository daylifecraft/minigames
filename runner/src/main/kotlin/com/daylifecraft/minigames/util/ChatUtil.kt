package com.daylifecraft.minigames.util

import com.daylifecraft.minigames.PermissionManager
import com.daylifecraft.minigames.database.DatabaseManager
import com.daylifecraft.minigames.text.i18n.PlayerLanguage
import com.daylifecraft.minigames.util.LangUtil.replaceAllTranslateKeys
import net.kyori.adventure.text.minimessage.MiniMessage
import net.minestom.server.entity.Player

object ChatUtil {
  /**
   * Username format for the chat
   */
  const val GLOBAL_CHAT_PLAYER_USERNAME = "<click:suggest_command:'/w $(senderUsername) '>$(senderUsername)</click>"

  /**
   * Removes miniMessages tags from a message if the sender does not have rights to such messages
   *
   * @param sender who sent the message
   * @param message message sent by the sender
   * @return processed message depending on the sender's permission
   */
  fun checkMiniMessage(sender: Player, message: String): String {
    if (PermissionManager.hasPermission(sender, "chat.minimessage.full")) {
      return message
    }
    return MiniMessage.miniMessage().stripTags(message)
  }

  /**
   * Sends a mini-message to the player, this message will automatically replace all translation
   * keys.
   *
   * @param recipient who will receive the message
   * @param message the message we need to send
   * @param variables set of variables to replace
   */
  fun sendMiniMessage(
    recipient: Player,
    message: String,
    vararg variables: Pair<String, String?>,
  ) {
    val playerLanguage = PlayerLanguage.get(recipient)

    recipient.sendMessage(
      MiniMessage.miniMessage()
        .deserialize(
          replaceAllTranslateKeys(
            playerLanguage,
            replaceVariables(message, *variables),
            *variables,
          ),
        ),
    )
  }

  /**
   * Gets the chat color for the player
   *
   * @param player an instance of the player whose color we need
   * @return player color
   */
  fun getGlobalChatMessageColorFromPlayer(player: Player): String {
    val playerProfile =
      DatabaseManager.getPlayerProfile(player)
        ?: throw IllegalArgumentException("Player profile is null!")
    var color = "#AAAAAA"
    for (permission in playerProfile.permissions!!) {
      val optional = PermissionManager.getGlobalChatColor(permission)
      if (optional.isPresent) {
        color = optional.get()
      }
    }
    return color
  }

  /**
   * Gets the chat badge for the player
   *
   * @param player an instance of the player whose badge we need
   * @return player badge
   */
  fun getGlobalChatPlayerBadge(player: Player): String {
    val playerProfile =
      DatabaseManager.getPlayerProfile(player)
        ?: throw IllegalArgumentException("Player profile is null!")
    var badge = ""
    for (permission in playerProfile.permissions!!) {
      val optional = PermissionManager.getBadge(permission)
      if (optional.isPresent) {
        badge = optional.get()
      }
    }
    return badge
  }

  /**
   * Replace variable placeholder with value
   *
   * @param source the string where we need to replace the variables
   * @param variables the variables we will replace with
   * @return string with replaced values
   */
  fun replaceVariables(source: String, vararg variables: Pair<String, String?>): String {
    var currentString = source

    for ((name, value) in variables) {
      currentString = currentString.replace("$($name)", value ?: "null")
    }

    return currentString
  }
}
