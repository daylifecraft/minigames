package com.daylifecraft.minigames.command.friends

import com.daylifecraft.common.command.SubCommand
import com.daylifecraft.minigames.PlayerManager
import com.daylifecraft.minigames.argument.ArgumentFriends
import com.daylifecraft.minigames.command.CommandsManager
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandContext

// TODO make all javadoc for command classes contain usage.

/** Command to remove friends  */
internal class FriendsRemoveCommand :
  Command("remove"),
  SubCommand {
  private val friendArgument = ArgumentFriends("friend")

  init {
    addSyntax(::onExecute, friendArgument)
  }

  // TODO Watch for static executors and remove them.
  private fun onExecute(sender: CommandSender, context: CommandContext) {
    // Get friend from context
    val player = context[friendArgument]?.player

    // Remove friend from player
    PlayerManager.removeFriend(sender, CommandsManager.getUuidFromSender(sender), player)
  }
}
