package com.daylifecraft.minigames.command.messages

import com.daylifecraft.common.finder.PlayerFinder
import com.daylifecraft.minigames.Init
import com.daylifecraft.minigames.PermissionManager
import com.daylifecraft.minigames.PlayerManager
import com.daylifecraft.minigames.argument.ArgumentOnlinePlayers
import com.daylifecraft.minigames.command.CommandsManager.getText
import com.daylifecraft.minigames.command.CommandsManager.getUuidFromSender
import com.daylifecraft.minigames.database.DatabaseManager
import com.daylifecraft.minigames.event.player.PlayerPrivateMessageEvent
import com.daylifecraft.minigames.profile.messages.PrivateMessagesManager
import com.daylifecraft.minigames.profile.punishment.PunishmentType
import com.daylifecraft.minigames.profile.settings.OnlineStatus
import com.daylifecraft.minigames.profile.settings.OnlineStatus.Companion.valueOf
import com.daylifecraft.minigames.text.i18n.PlayerLanguage
import com.daylifecraft.minigames.text.i18n.PlayerLanguage.Companion.get
import com.daylifecraft.minigames.util.ChatUtil.checkMiniMessage
import net.minestom.server.MinecraftServer
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.command.builder.condition.CommandCondition
import net.minestom.server.entity.Player
import net.minestom.server.event.EventDispatcher
import java.util.UUID

class PrivateMessageCommand : Command("w") {
  private val usernameArgument = ArgumentOnlinePlayers("player")
  private val messageArgument = ArgumentType.StringArray("message")

  init {
    condition = CommandCondition { sender: CommandSender?, _ -> (sender is Player) }

    usernameArgument.excludeSamePlayer(true)
    usernameArgument.suggestion.excludeNotFriends(true)
    usernameArgument.suggestion.excludeOffline(true)

    addSyntax(this::onExecute, usernameArgument, messageArgument)
  }

  private fun onExecute(sender: CommandSender, context: CommandContext) {
    val senderUUID = getUuidFromSender(sender)
    val playerLanguage =
      get(
        MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(senderUUID)!!,
      )

    val finder = context[usernameArgument]

    if (finder.isSamePlayer) {
      playerLanguage.sendMiniMessage(SEND_SELF_FAIL)
      return
    }

    if (sender is Player) {
      send(sender, context, playerLanguage, finder, senderUUID)
      return
    }

    playerLanguage.sendMiniMessage(SEND_FAIL)
  }

  private fun send(
    player: Player,
    context: CommandContext,
    playerLanguage: PlayerLanguage,
    finder: PlayerFinder,
    senderUUID: UUID,
  ) {
    if (finder.isPlayerNull) {
      // Really send fail?
      playerLanguage.sendMiniMessage(SEND_FAIL)
      return
    }

    val targetPlayer = finder.player!!

    val isTargetAdmin = PermissionManager.hasPermission(targetPlayer, PERMISSION)

    val isAnyRecentMessage =
      PrivateMessagesManager.isAnyRecentMessages(senderUUID, targetPlayer.uuid)

    val isMuted =
      PlayerManager.checkPunishments(
        player,
        PunishmentType.MUTE,
        !(isAnyRecentMessage && isTargetAdmin),
      )

    if (isMuted && (!isTargetAdmin || !isAnyRecentMessage)) {
      return
    }

    val targetProfile = DatabaseManager.getPlayerProfile(targetPlayer)!!

    val targetSettings = targetProfile.settings

    val targetOnlineStatus = valueOf(targetPlayer)

    val isSenderAdmin = PermissionManager.hasPermission(player, PERMISSION)

    val isTargetPmDisabled = targetSettings!!.disablePM

    val isSenderInFriends =
      targetProfile.getFriends()!!.contains(player.uuid.toString())

    val sendingPossible = (
      targetOnlineStatus == OnlineStatus.ONLINE ||
        (targetOnlineStatus == OnlineStatus.FRIENDS_ONLY && isSenderInFriends)
      )

    val canSenderSendMessageWithoutCheck =
      sendingPossible && !isTargetPmDisabled && !isMuted

    val messageText =
      getText(context.getOrDefault(messageArgument, arrayOf()))

    if (isSenderAdmin || canSenderSendMessageWithoutCheck || isAnyRecentMessage) {
      sendMessage(player, targetPlayer, isSenderAdmin, isTargetAdmin, messageText)
    } else {
      playerLanguage.sendMiniMessage(SEND_FAIL)
    }
  }

  private fun sendMessage(
    player: Player,
    targetPlayer: Player,
    isSenderAdmin: Boolean,
    isTargetAdmin: Boolean,
    messageText: String,
  ) {
    val formattedMessage = checkMiniMessage(player, messageText)
    Init.chatManager!!.sendPrivateMessage(player, targetPlayer, isSenderAdmin, isTargetAdmin, formattedMessage)

    PrivateMessagesManager.onPlayerSendMessage(player, targetPlayer)

    val event = PlayerPrivateMessageEvent(player, formattedMessage, targetPlayer)
    EventDispatcher.call(event)
  }

  companion object {
    private const val PERMISSION = "pm.staff"
    private const val SEND_FAIL = "pm.send.fail"
    private const val SEND_SELF_FAIL = "pm.send.self-fail"
  }
}
