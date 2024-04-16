package com.daylifecraft.minigames.profile.group

import com.daylifecraft.minigames.text.i18n.PlayerLanguage
import net.minestom.server.MinecraftServer
import net.minestom.server.entity.Player
import java.util.UUID

object PlayersGroupManager {
  private val playersGroupsList: MutableList<PlayersGroup> = ArrayList()

  fun getGroupByPlayer(player: Player): PlayersGroup? = getGroupByPlayer(player.uuid)

  fun getGroupByPlayer(playerUUID: UUID): PlayersGroup? {
    for (playersGroup in playersGroupsList) {
      if (playersGroup.isContainsPlayer(playerUUID)) {
        return playersGroup
      }
    }

    return null
  }

  fun createGroup(leaderUUID: UUID, vararg members: UUID): PlayersGroup? = createGroup(leaderUUID, listOf(*members))

  private fun createGroup(leaderUUID: UUID, members: List<UUID>): PlayersGroup? {
    val playersUUIDs: MutableList<UUID> = ArrayList(members)
    playersUUIDs.add(leaderUUID)

    // Remove users who as something got on this list and is already in the group
    playersUUIDs.removeIf { playerUUID -> getGroupByPlayer(playerUUID) != null }

    // Can`t create group from single player
    if (playersUUIDs.size <= 1) {
      return null
    }

    val playersGroup = PlayersGroup(leaderUUID, playersUUIDs)
    playersGroupsList.add(playersGroup)
    return playersGroup
  }

  fun removeGroup(playersGroup: PlayersGroup) {
    playersGroup.removed()
    playersGroupsList.remove(playersGroup)
  }

  // TODO remake it as extension method for playersGroup
  fun sendMessageToPlayerGroup(
    playersGroup: PlayersGroup,
    messageKey: String,
    vararg variables: Pair<String, String?>,
  ) {
    for (memberUUID in playersGroup.getAllPlayersUUIDs()) {
      val player =
        MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(
          memberUUID,
        ) ?: continue

      PlayerLanguage.get(player).sendMiniMessage(messageKey, *variables)
    }
  }

  fun isPlayerInGroup(player: Player): Boolean = getGroupByPlayer(player) != null
}
