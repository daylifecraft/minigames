package com.daylifecraft.minigames.profile.group

import net.minestom.server.entity.Player
import java.util.Objects
import java.util.UUID

class PlayersGroup(var playerLeaderUUID: UUID?, allPlayersUUIDs: List<UUID>) {
  // Tip: For most performance we use HashSet. No duplicates, no order
  private val allPlayersUUIDs = HashSet(allPlayersUUIDs)
  private val playersJoinTime: MutableMap<UUID, Long> = HashMap()
  var isBind: Boolean = true
    private set

  init {
    val currentTime = System.currentTimeMillis()
    allPlayersUUIDs.forEach { uuid: UUID -> playersJoinTime[uuid] = currentTime }
  }

  fun getAllPlayersUUIDs(): Set<UUID> = allPlayersUUIDs

  /**
   * Returns Set of players in group without owner
   *
   * @get set of players uuids
   */
  val playersWithoutOwner: Set<UUID>
    get() {
      val result: MutableSet<UUID> = HashSet(allPlayersUUIDs)
      result.remove(playerLeaderUUID)

      return result
    }

  fun addPlayer(playerUUID: UUID) {
    allPlayersUUIDs.add(playerUUID)
    playersJoinTime[playerUUID] = System.currentTimeMillis()
  }

  fun removePlayer(playerUUID: UUID?) {
    allPlayersUUIDs.remove(playerUUID)
    playersJoinTime.remove(playerUUID)
  }

  /**
   * Determines who will be the next leader in the group. Priority: time of entry
   *
   * @get the UUID of player from group
   */
  val nextLeaderUUID: UUID?
    get() {
      var minimalValue = Long.MAX_VALUE
      var result: UUID? = null
      for ((key, value) in playersJoinTime) {
        if (key == playerLeaderUUID) {
          continue
        }

        if (value < minimalValue) {
          minimalValue = value
          result = key
        }
      }

      return result
    }

  val groupSize: Int
    get() = allPlayersUUIDs.size

  fun isContainsPlayer(player: Player): Boolean = isContainsPlayer(player.uuid)

  fun isContainsPlayer(playerUUID: UUID): Boolean = allPlayersUUIDs.contains(playerUUID)

  fun isPlayerLeader(playerUuid: UUID): Boolean = playerLeaderUUID == playerUuid

  fun isPlayerLeader(player: Player): Boolean = isPlayerLeader(player.uuid)

  fun removed() {
    playerLeaderUUID = null
    allPlayersUUIDs.clear()
    isBind = false
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) {
      return true
    }
    if (other == null || javaClass != other.javaClass) {
      return false
    }
    val that = other as PlayersGroup
    return isBind == that.isBind &&
      allPlayersUUIDs == that.allPlayersUUIDs &&
      playersJoinTime == that.playersJoinTime &&
      playerLeaderUUID == that.playerLeaderUUID
  }

  override fun hashCode(): Int = Objects.hash(isBind, allPlayersUUIDs, playersJoinTime, playerLeaderUUID)
}
