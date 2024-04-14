package com.daylifecraft.minigames.command.group

import com.daylifecraft.minigames.command.group.invites.GroupAcceptCommand
import com.daylifecraft.minigames.command.group.invites.GroupInviteCommand
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.condition.CommandCondition
import net.minestom.server.entity.Player

/** Command, for working with groups  */
class GroupCommand : Command("group") {
  init {
    condition = CommandCondition { sender, _ -> (sender is Player) }

    addSubcommand(GroupInviteCommand())
    addSubcommand(GroupAcceptCommand())
    addSubcommand(GroupLeaveCommand())
    addSubcommand(GroupKickCommand())
    addSubcommand(GroupNewLeaderCommand())
  }
}
