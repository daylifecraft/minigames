package com.daylifecraft.minigames.command.rounds

import com.daylifecraft.common.command.SubCommand
import com.daylifecraft.minigames.PlayerManager
import com.daylifecraft.minigames.command.AbstractPermissionCommand
import com.daylifecraft.minigames.command.CommandsManager.getSenderLanguage
import com.daylifecraft.minigames.command.CommandsManager.parseUuidOrUserNameToUuid
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.entity.Player

/**
 * Outputs all current rounds to the chat, accepts the player or his UUID as an optional argument,
 * if the argument is set, then outputs the rounds only where this player participated
 */
class RoundsViewCommand internal constructor() :
  AbstractPermissionCommand("view"),
  SubCommand {
  override val permission: String = "rounds.view"

  private val playerArgument = ArgumentType.String("player")

  init {
    addSyntax(this::onExecute)
    addSyntax(this::onExecute, playerArgument)
  }

  private fun onExecute(sender: CommandSender, context: CommandContext) {
    val playerLanguage = getSenderLanguage(sender)

    var target: Player? = sender as Player
    if (context.has(playerArgument)) {
      val targetUuid = parseUuidOrUserNameToUuid(context[playerArgument])
      if (targetUuid == null) {
        playerLanguage.sendMiniMessage("commands.reusable.fails.wrong-player")
        return
      }

      target = PlayerManager.getPlayerByUuid(targetUuid)
    }
    if (target == null) {
      playerLanguage.sendMiniMessage("commands.reusable.fails.non-existent-player")
    }

    // TODO 28.10.2023 Print rounds info
  }
}
