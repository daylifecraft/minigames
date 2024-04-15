package com.daylifecraft.minigames

import com.daylifecraft.common.util.safeCastToList
import com.daylifecraft.minigames.config.ConfigManager.mainConfig
import net.minestom.server.entity.Player
import net.minestom.server.permission.Permission
import java.util.Optional

object PermissionManager {
  /**
   * Gets a specialized badge for the group.
   *
   * @param group group id
   * @return Optional with badge for group
   */
  fun getBadge(group: String): Optional<String> {
    val o = getGroupParameters(group)["badge"]

    return if (o == null) {
      Optional.empty()
    } else {
      Optional.of(o.toString())
    }
  }

  /**
   * Gets a special chat color for the group.
   *
   * @param group group id
   * @return Optional with color for group
   */
  fun getGlobalChatColor(group: String): Optional<String> {
    val o = getGroupParameters(group)["globalChatColor"]

    return if (o == null) {
      Optional.empty()
    } else {
      Optional.of(o.toString())
    }
  }

  /**
   * Check if the player has permission
   *
   * @param player player instance
   * @param permissionName name for permission
   * @param parameter parameter for permission
   * @return return true if player have permission
   */
  fun hasPermission(
    player: Player,
    permissionName: String,
    parameter: String,
  ): Boolean {
    val has = hasPermission(player, "$permissionName.$parameter")

    if (has) {
      return true
    }

    return hasPermission(player, "$permissionName.*")
  }

  /**
   * Check if the player has permission
   *
   * @param player player instance
   * @param permissionName name for permission
   * @return return true if player have permission
   */
  fun hasPermission(player: Player, permissionName: String): Boolean {
    val permissions = player.allPermissions

    for (permission in permissions) {
      if (hasPermission(permission.permissionName, permissionName)) {
        return true
      }
    }

    return false
  }

  /**
   * Check if the group has permission
   *
   * @param group group id
   * @param permission name for permission
   * @return return true if group have permission
   */
  fun hasPermission(group: String, permission: String): Boolean {
    // Get permission names
    val groupPermissionsNames = getPermissionsNames(group)

    for (groupPermissionName in groupPermissionsNames) {
      if (groupPermissionName == permission) {
        return true
      }
    }

    return false
  }

  /**
   * Gets all group permissions
   *
   * @param group group id
   * @return list of permission's for group
   */
  fun getPermissions(group: String): List<Permission> {
    val permissions: MutableList<Permission> = ArrayList()
    // Get permission names
    val permissionsNames = getPermissionsNames(group)

    for (permissionName in permissionsNames) {
      // Add permission to permissions list
      permissions.add(Permission(permissionName))
    }

    return permissions
  }

  /**
   * Gets the names of all group permissions.
   *
   * @param group group id
   * @return list of permission names for group
   */
  private fun getPermissionsNames(group: String): List<String> = getGroupParameters(group)["permissions"]!!.safeCastToList<String>()

  /**
   * Gets the names of all group permissions.
   *
   * @param group group id
   * @return map of permission for group
   */
  private fun getGroupParameters(group: String): Map<String, Any?> = mainConfig!!.getValueFromList("groups", "name", group)
}
