package com.daylifecraft.minigames.command

import com.daylifecraft.minigames.PermissionManager
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.condition.CommandCondition
import net.minestom.server.entity.Player

abstract class AbstractPermissionCommand(
  name: String,
  vararg aliases: String,
) : Command(name, *aliases) {
  protected abstract val permission: String

  init {
    condition =
      CommandCondition { sender, _ ->
        (sender is Player) && PermissionManager.hasPermission(sender, permission)
      }
  }
}
