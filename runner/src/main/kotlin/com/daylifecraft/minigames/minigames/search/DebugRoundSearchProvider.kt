package com.daylifecraft.minigames.minigames.search

import com.daylifecraft.common.util.FilterUtils
import com.daylifecraft.minigames.PlayerManager
import com.daylifecraft.minigames.PlayerManager.getPlayerByUuid
import com.daylifecraft.minigames.PlayerManager.getPlayerNameByUuidOrDefault
import com.daylifecraft.minigames.minigames.MiniGameStartController
import com.daylifecraft.minigames.minigames.PlayerMiniGameManager
import com.daylifecraft.minigames.minigames.queue.PlayerMiniGameQueueData
import com.daylifecraft.minigames.minigames.settings.GeneralGameSettings
import com.daylifecraft.minigames.profile.group.PlayersGroupManager
import com.daylifecraft.minigames.text.i18n.PlayerLanguage
import com.google.gson.JsonObject
import net.minestom.server.entity.Player
import java.util.Objects
import java.util.UUID

/**
 * Provider controls the start of the round in debug mode, which skips the player search stage and
 * starts the round with a specific set
 */
class DebugRoundSearchProvider(
  private val roundOwner: Player,
  private val miniGameSettings: GeneralGameSettings,
  roundPlayers: List<Player>,
) : IRoundSearchProvider {
  private val roundPlayers: MutableSet<Player> = HashSet(roundPlayers)

  private val preparedPlayersMap: MutableMap<UUID, PlayerMiniGameQueueData> = HashMap()
  private val rejectedPlayers: MutableSet<UUID> = HashSet()

  private val playersSpreadByTeams: MutableSet<Set<UUID>> = HashSet()

  private var codeCompleted = false

  override fun canBePrepared(player: Player): Boolean {
    if (player.uuid != roundOwner.uuid) {
      return true
    }

    if (!super.canBePrepared(player)) {
      return false
    }

    if (roundPlayersCount !in miniGameSettings.playersCountRange) {
      PlayerLanguage.get(roundOwner)
        .sendMiniMessage(
          "debug.rounds.fail.wrong-players-size",
          "totalPlayers" to roundPlayersCount.toString(),
          "minTotalPlayers" to miniGameSettings.playersCountRange.first.toString(),
          "maxTotalPlayers" to miniGameSettings.playersCountRange.last.toString(),
        )
      return false
    }

    return !checkIfAnyPlayerAlreadyLocked()
  }

  override fun onPlayerPrepared(player: Player, playerMiniGameQueueData: PlayerMiniGameQueueData) {
    preparedPlayersMap[player.uuid] = playerMiniGameQueueData

    if (player.uuid == roundOwner.uuid) {
      // Round owner prepared. We need to prepare other players

      if (checkIfAnyPlayerAlreadyLocked()) {
        stopRoundPreparation()
        return
      }

      if (checkIfAllPlayersNotInGroupOrAddMembers()) {
        stopRoundPreparation()
        return
      }

      if (roundPlayersCount !in miniGameSettings.playersCountRange) {
        PlayerLanguage.get(roundOwner)
          .sendMiniMessage(
            "debug.rounds.fail.wrong-players-size",
            "totalPlayers" to roundPlayersCount.toString(),
            "minTotalPlayers" to miniGameSettings.playersCountRange.first.toString(),
            "maxTotalPlayers" to miniGameSettings.playersCountRange.last.toString(),
          )
        stopRoundPreparation()
        return
      }

      // Preparing other players
      for (playerToPrepare in roundPlayers) {
        PlayerMiniGameManager.preparePlayerForRoundSearch(
          playerToPrepare,
          playerMiniGameQueueData.miniGameId,
          this,
          playerMiniGameQueueData.settings,
          playerMiniGameQueueData.filters,
        )
      }
    }

    checkIfAllPlayersProcessed()
  }

  override fun onPlayerCancelledPreparation(player: Player, playerMiniGameQueueData: PlayerMiniGameQueueData) {
    rejectedPlayers.add(player.uuid)
    checkIfAllPlayersProcessed()
  }

  override fun onPlayerRejectRoundSearch(player: Player) {
    rejectedPlayers.add(player.uuid)
  }

  /* TODO 09.11.2023 Check what will happen when player leave before this method is called.
      Will the player be null or not?
      In other words, what will happen if when round owner prepares to the round, another player leave.
   */
  private fun checkIfAnyPlayerAlreadyLocked(): Boolean {
    val alreadyLocked: MutableList<Player> = ArrayList()
    for (player in roundPlayers) {
      if (PlayerMiniGameManager.isPlayerLocked(player.uuid)) {
        alreadyLocked.add(player)
      }
    }

    if (alreadyLocked.isEmpty()) {
      return false
    }

    val ownerLanguage = PlayerLanguage.get(roundOwner)
    ownerLanguage.sendMiniMessage(
      "debug.rounds.fail.players-busy",
      "players" to alreadyLocked.joinToString(", ") { it.username },
    )

    return true
  }

  /**
   * Method for check: If the player is in the group and not the leader, the round will not start If
   * the player is in the group and the leader, Add all the group members to the round (online)
   *
   * @return True - if there is someone among the players who is a member of the group, else - False
   */
  private fun checkIfAllPlayersNotInGroupOrAddMembers(): Boolean {
    // Iterate round players and round owner!
    val alreadyGroupMembers: MutableList<Player> = ArrayList()
    for (playerToCheck in roundPlayers.asSequence() + roundOwner) {
      val groupByPlayer = PlayersGroupManager.getGroupByPlayer(playerToCheck) ?: continue

      if (!groupByPlayer.isPlayerLeader(playerToCheck)) {
        alreadyGroupMembers.add(playerToCheck)
        continue
      }

      // Add players in group to spread set
      playersSpreadByTeams.add(groupByPlayer.getAllPlayersUUIDs())

      // Add players to
      roundPlayers.addAll(
        groupByPlayer.getAllPlayersUUIDs().stream()
          // Remove group leader
          .filter { uuid -> uuid != groupByPlayer.playerLeaderUUID }
          // Get players instances
          .map<Player>(PlayerManager::getPlayerByUuid)
          // Remove offline players
          .filter(Objects::nonNull)
          .toList(),
      )
    }

    if (alreadyGroupMembers.isNotEmpty()) {
      val ownerLanguage = PlayerLanguage.get(roundOwner)
      ownerLanguage.sendMiniMessage(
        "debug.rounds.fail.players-not-leaders",
        "players" to alreadyGroupMembers.joinToString(", ") { it.username },
      )
      return true
    }

    return false
  }

  private val roundPlayersCount: Int
    get() = roundPlayers.size + 1

  private val countOfProcessedPlayers: Int
    get() = rejectedPlayers.size + preparedPlayersMap.size

  private fun checkIfAllPlayersProcessed() {
    // If not all players processed
    if (countOfProcessedPlayers != roundPlayersCount || codeCompleted) {
      return
    }

    codeCompleted = true

    val preparedPlayersFormed: MutableMap<Player, PlayerMiniGameQueueData> = HashMap()
    for ((playerUuid, value) in preparedPlayersMap) {
      val player = getPlayerByUuid(playerUuid)
      if (player == null) {
        rejectedPlayers.add(playerUuid)
        continue
      }
      preparedPlayersFormed[player] = value
    }

    val ownerLanguage = PlayerLanguage.get(roundOwner)
    if (rejectedPlayers.isNotEmpty()) {
      ownerLanguage.sendMiniMessage(
        "debug.rounds.fail.players-declined",
        "players" to
          rejectedPlayers.joinToString(", ") { uuid: UUID ->
            getPlayerNameByUuidOrDefault(
              uuid,
              "null",
            )
          },
      )

      stopRoundPreparation()
      return
    }

    val ownerQueueData = preparedPlayersMap[roundOwner.uuid]
    val notMatchedFiltersPlayers = preparedPlayersFormed.entries.stream() // Remove owner from stream
      .filter { entry -> entry.key.uuid != roundOwner.uuid } // Filter players, which has not equals filters
      .filter { entry -> ownerQueueData!!.filters != entry.value.filters }
      .map { it.key.username } // Get names of uuids
      .toList()

    if (notMatchedFiltersPlayers.isNotEmpty()) {
      ownerLanguage.sendMiniMessage(
        "debug.rounds.fail.players-wrong-filters",
        "players" to notMatchedFiltersPlayers.joinToString(", "),
      )

      stopRoundPreparation()
      return
    }

    // Send message
    ownerLanguage.sendMiniMessage("debug.rounds.new-round")

    // Start round
    val finalFilters: JsonObject = FilterUtils.getResultFilters(
      preparedPlayersFormed.values.map { it.filters },
    )

    val roundPlayerSettings: MutableMap<UUID, JsonObject> = HashMap()
    preparedPlayersFormed.forEach { (key: Player, value: PlayerMiniGameQueueData?) ->
      roundPlayerSettings[key.uuid] = value.settings
    }

    // Spread the remaining players to single teams
    roundPlayerSettings.keys.forEach { uuid: UUID ->
      if (playersSpreadByTeams.none { it.contains(uuid) }) {
        playersSpreadByTeams.add(setOf(uuid))
      }
    }

    MiniGameStartController.startNewRoundWithoutConfirmation(
      miniGameSettings,
      roundPlayerSettings,
      playersSpreadByTeams,
      finalFilters,
    )
  }

  // TODO 09.11.2023 The same question about NULL players
  // checkIfAnyPlayerAlreadyLocked()
  private fun stopRoundPreparation() {
    PlayerMiniGameManager.removeLockedPlayer(roundOwner.uuid)
    roundPlayers.forEach { player: Player -> PlayerMiniGameManager.removeLockedPlayer(player.uuid) }
  }
}
