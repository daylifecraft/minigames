package com.daylifecraft.minigames.command.group

import com.daylifecraft.minigames.Init.chatManager
import com.daylifecraft.minigames.PlayerManager
import com.daylifecraft.minigames.command.CommandsManager.getText
import com.daylifecraft.minigames.event.player.PlayerGroupMessageEvent
import com.daylifecraft.minigames.profile.group.PlayersGroup
import com.daylifecraft.minigames.profile.punishment.PunishmentType
import com.daylifecraft.minigames.text.i18n.PlayerLanguage
import com.daylifecraft.minigames.util.ChatUtil.checkMiniMessage
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.entity.Player
import net.minestom.server.event.EventDispatcher

class GroupMessageCommand : AbstractGroupCommand("g") {
  private val messageTextArgument = ArgumentType.StringArray("messageText")

  init {
    addSyntax(messageTextArgument)
  }

  /**
   * This method will be called when the command is used
   *
   * @param sender who send command
   * @param context command context
   * @param senderGroup group
   * @param playerLanguage language
   */
  public override fun onCommandUse(
    sender: Player,
    context: CommandContext,
    senderGroup: PlayersGroup?,
    playerLanguage: PlayerLanguage,
  ) {
    if (PlayerManager.checkPunishments(sender, PunishmentType.MUTE, true)) {
      return
    }

    // It's necessary to use that check instead of setGroupRequired(), because the messages are
    // different
    if (senderGroup == null) {
      playerLanguage.sendMiniMessage("group.send.fail")
      return
    }

    // Getting message and send to own group with chat manager
    val messageTextArray = context[messageTextArgument] ?: return

    val formattedMessage =
      checkMiniMessage(sender, getText(messageTextArray))

    chatManager!!.sendGroupChatMessage(sender, senderGroup, formattedMessage)

    val event = PlayerGroupMessageEvent(formattedMessage, sender)
    EventDispatcher.call(event)
  }
}
