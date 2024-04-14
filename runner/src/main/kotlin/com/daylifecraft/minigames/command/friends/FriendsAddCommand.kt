package com.daylifecraft.minigames.command.friends

import com.daylifecraft.common.command.SubCommand
import com.daylifecraft.minigames.PlayerManager
import com.daylifecraft.minigames.argument.ArgumentOnlinePlayers
import com.daylifecraft.minigames.command.CommandsManager
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandContext

/** Command to add friends  */
internal class FriendsAddCommand :
  Command("add"),
  SubCommand {
  private val friendArgument = ArgumentOnlinePlayers("friend")

  init {
    friendArgument.excludeFriends(true)

    addSyntax(this::onExecute, friendArgument)
  }

  private fun onExecute(sender: CommandSender, context: CommandContext) {
    // Get player from context
    val player = context[friendArgument]?.player

    // Add friend to player
    PlayerManager.addFriend(sender, CommandsManager.getUuidFromSender(sender), player)
  }
}
