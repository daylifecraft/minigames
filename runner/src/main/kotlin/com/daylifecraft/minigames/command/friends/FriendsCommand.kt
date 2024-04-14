package com.daylifecraft.minigames.command.friends

import net.minestom.server.command.builder.Command

/** Command, for working with friends  */
class FriendsCommand : Command("friends") {
  init {
    addSubcommand(FriendsAddCommand())
    addSubcommand(FriendsRemoveCommand())
  }
}
