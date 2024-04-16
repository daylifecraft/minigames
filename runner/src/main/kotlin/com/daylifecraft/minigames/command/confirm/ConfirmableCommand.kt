package com.daylifecraft.minigames.command.confirm

import com.daylifecraft.common.util.extensions.miniMessage
import com.daylifecraft.minigames.command.CommandsManager
import com.daylifecraft.minigames.text.i18n.PlayerLanguage
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.entity.Player

interface ConfirmableCommand {
  fun showConfirm(sender: CommandSender, context: CommandContext) {
    ConfirmCommand.addSender(sender)

    // Create confirm request
    // TODO this probably should be rewritten
    val id = CommandsManager.createConfirmRequest(this, sender, context)

    // Get sender`s language
    val language = PlayerLanguage.get(sender as Player)

    val message =
      """
      ${language.string("commands.confirm-action-string1")}
      <gray>/${context.input}</gray>

      ${language.string("commands.confirm-action-string2")}

      ${language.string("commands.confirm-action-confirm", "confirmActionId" to id.toString())}

      ${language.string("commands.confirm-action-tip")}

      """.trimIndent()

    // Send message to player
    sender.sendMessage(message.miniMessage())
  }

  fun confirm(sender: CommandSender, context: CommandContext) {
    onConfirm(sender, context)
  }

  fun onConfirm(sender: CommandSender, context: CommandContext)
}
