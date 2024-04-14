package com.daylifecraft.minigames.command.debug.player

import com.daylifecraft.minigames.PlayerManager
import com.daylifecraft.minigames.command.CommandsManager
import com.daylifecraft.minigames.command.debug.AbstractDebugCommand
import com.daylifecraft.minigames.command.suggestion.OnlinePlayersSuggestion
import com.daylifecraft.minigames.text.i18n.Language
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.entity.Player
import java.util.UUID

/** Command for Disable/Enable Flying  */
class DebugFlyCommand : AbstractDebugCommand("fly") {
  private val playerArgument = ArgumentType.String("player")

  init {
    val onlinePlayersSuggestion = OnlinePlayersSuggestion()
    onlinePlayersSuggestion.excludeDifferentInstances(true)
    addSyntax(
      this::onExecute,
      playerArgument.setSuggestionCallback(onlinePlayersSuggestion),
    )
  }

  override fun onCommandUse(sender: CommandSender, context: CommandContext) {
    val senderLanguage: Language = CommandsManager.getSenderLanguage(sender)
    if (context.has(playerArgument)) {
      val playerUuid: UUID? = CommandsManager.parseUuidOrUserNameToUuid(context[playerArgument])

      if (playerUuid == null) {
        senderLanguage.sendMiniMessage("commands.reusable.fails.wrong-player")
        return
      }

      val targetPlayer: Player? = PlayerManager.getPlayerByUuid(playerUuid)

      if (targetPlayer == null) {
        senderLanguage.sendMiniMessage("commands.reusable.fails.target-player-offline")
        return
      }

      val resultState: Boolean = !targetPlayer.isAllowFlying
      targetPlayer.isAllowFlying = resultState

      if (resultState) {
        senderLanguage.sendMiniMessage(
          "debug.fly.other.success.on",
          "targetPlayer" to targetPlayer.username,
        )
      } else {
        senderLanguage.sendMiniMessage(
          "debug.fly.other.success.off",
          "targetPlayer" to targetPlayer.username,
        )
        targetPlayer.isFlying = false
      }
    } else {
      val player: Player = sender as Player

      val resultState: Boolean = !player.isAllowFlying
      player.isAllowFlying = resultState

      if (resultState) {
        senderLanguage.sendMiniMessage("debug.fly.self.success.on")
      } else {
        senderLanguage.sendMiniMessage("debug.fly.self.success.off")
        player.isFlying = false
      }
    }
  }
}
