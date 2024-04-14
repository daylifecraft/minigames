package com.daylifecraft.minigames.command.debug.player

import com.daylifecraft.minigames.PermissionManager.getPermissions
import com.daylifecraft.minigames.command.CommandsManager.getSenderLanguage
import com.daylifecraft.minigames.command.debug.AbstractDebugCommand
import com.daylifecraft.minigames.database.DatabaseManager.getPlayerProfile
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.entity.Player
import net.minestom.server.permission.Permission

/**
 * Toggle Admin command: /~ toggleadmin
 * Command inverts player admin permission (If NOT admin - make admin)
 */
class DebugToggleAdminCommand : AbstractDebugCommand("toggleadmin") {
  override fun onCommandUse(sender: CommandSender, context: CommandContext) {
    val player = sender as Player
    val senderLanguage = getSenderLanguage(sender)

    val isAdmin = player.hasPermission(ADMIN_GROUP_NAME)
    val playerProfile = getPlayerProfile(player)

    if (isAdmin) {
      player.removePermission(ADMIN_GROUP_NAME)
      getPermissions(ADMIN_GROUP_NAME).forEach { permission: Permission -> player.removePermission(permission) }

      playerProfile!!.removePermission(ADMIN_GROUP_NAME)

      senderLanguage.sendMiniMessage("debug.toggleadmin.disable.success")
    } else {
      player.addPermission(Permission(ADMIN_GROUP_NAME))

      playerProfile!!.addPermission(ADMIN_GROUP_NAME)

      senderLanguage.sendMiniMessage("debug.toggleadmin.enable.success")
    }

    player.refreshCommands()
  }

  companion object {
    /**
     * Name of admin group, that described in server.yml
     */
    const val ADMIN_GROUP_NAME: String = "isAdmin"
  }
}
