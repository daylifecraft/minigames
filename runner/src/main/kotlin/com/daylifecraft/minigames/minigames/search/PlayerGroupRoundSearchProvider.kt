package com.daylifecraft.minigames.minigames.search

import com.daylifecraft.common.util.extensions.minestom.sendMiniMessage
import com.daylifecraft.minigames.Init
import com.daylifecraft.minigames.minigames.PlayerMiniGameManager.addPlayerGroupToSearchQueue
import com.daylifecraft.minigames.minigames.PlayerMiniGameManager.isPlayerLocked
import com.daylifecraft.minigames.minigames.PlayerMiniGameManager.preparePlayerForRoundSearch
import com.daylifecraft.minigames.minigames.PlayerMiniGameManager.removeFromSearchQueueAndUnlockGroup
import com.daylifecraft.minigames.minigames.PlayerMiniGameManager.removeLockedPlayer
import com.daylifecraft.minigames.minigames.queue.PlayerMiniGameQueueData
import com.daylifecraft.minigames.minigames.settings.GeneralGameSettings
import com.daylifecraft.minigames.text.i18n.PlayerLanguage
import com.daylifecraft.minigames.text.i18n.TranslateText
import com.google.gson.JsonObject
import net.minestom.server.entity.Player
import java.util.UUID
import java.util.stream.Collectors
import java.util.stream.Stream

/** Provider responsible for working with groups of players  */
class PlayerGroupRoundSearchProvider(
  private val groupOwner: Player,
  groupMembers: List<Player>,
  private val generalGameSettings: GeneralGameSettings,
) : IRoundSearchProvider {
  private val groupMembers: List<Player> = ArrayList(groupMembers)

  private val preparedPlayers: MutableMap<Player, PlayerMiniGameQueueData?> = HashMap()
  private val rejectedPlayers: MutableList<Player?> = ArrayList()

  private var codeCompleted = false

  override fun canBePrepared(player: Player): Boolean {
    // All checks only for group owner. We don`t need to check again
    if (player.uuid != groupOwner.uuid) {
      return true
    }

    if (groupMembers.any { member: Player? -> (member == null || !member.isOnline) }) {
      val offlinePlayers =
        groupMembers.stream()
          .filter { member: Player? -> member != null && !member.isOnline }
          .map { obj: Player -> obj.username }
          .toList()
      sendMessageToAllPlayers(
        "group.round.players-offline",
        "players" to java.lang.String.join(", ", offlinePlayers),
      )
      return false
    }

    if (playersGroupSize !in generalGameSettings.groupSizeRange) {
      sendMessageToAllPlayers(
        "rounds.queue.fail.player-amount",
        "curPlayersNum" to playersGroupSize.toString(),
        "minStartSize" to generalGameSettings.groupSizeRange.first.toString(),
        "maxStartSize" to generalGameSettings.groupSizeRange.last.toString(),
      )
      return false
    }

    val alreadyLockedPlayer = alreadyLockedPlayers
    if (alreadyLockedPlayer.isNotEmpty()) {
      sendMessageToAllPlayers(
        "group.round.players-busy",
        "players" to alreadyLockedPlayer.joinToString(", "),
      )
      return false
    }

    return true
  }

  override fun onPlayerPrepared(player: Player, playerMiniGameQueueData: PlayerMiniGameQueueData) {
    preparedPlayers[player] = playerMiniGameQueueData

    checkIfAllGroupPrepared()

    if (groupOwner.uuid == player.uuid) {
      // Group owner prepared => preparing group members
      for (playerToPrepare in groupMembers) {
        preparePlayerForRoundSearch(
          playerToPrepare,
          playerMiniGameQueueData.miniGameId,
          this,
          playerMiniGameQueueData.settings,
          playerMiniGameQueueData.filters,
        )
      }
    }
  }

  override fun onPlayerCancelledPreparation(player: Player, playerMiniGameQueueData: PlayerMiniGameQueueData) {
    rejectedPlayers.add(player)
    checkIfAllGroupPrepared()
  }

  override fun onPlayerRejectRoundSearch(player: Player) {
    // Remove all group from search queue. Remove owner == remove all group
    if (codeCompleted) {
      removeFromSearchQueueAndUnlockGroup(groupOwner.uuid)
    } else {
      rejectedPlayers.add(player)
    }
  }

  private val playersGroupSize: Int
    get() = groupMembers.size + 1

  private val countOfProcessedPlayers: Int
    get() = preparedPlayers.size + rejectedPlayers.size

  private val alreadyLockedPlayers: Set<String>
    get() =
      Stream.concat(
        Stream.of(
          groupOwner,
        ),
        groupMembers.stream(),
      )
        .filter { player: Player -> isPlayerLocked(player.uuid) }
        .map { obj: Player -> obj.username }
        .collect(Collectors.toSet())

  private fun stopPreparation() {
    removeLockedPlayer(groupOwner.uuid)
    groupMembers.forEach { player: Player -> removeLockedPlayer(player.uuid) }
  }

  private fun checkIfAllGroupPrepared() {
    if (playersGroupSize != countOfProcessedPlayers || codeCompleted) {
      return
    }

    codeCompleted = true

    if (rejectedPlayers.isNotEmpty()) {
      sendMessageToAllPlayers(
        "group.round.player-not-configured",
        "player" to rejectedPlayers.first()!!.username,
      )
      stopPreparation()
      return
    }

    val ownerQueueData = preparedPlayers[groupOwner]
    val playersWithUnmatchedFilters =
      preparedPlayers.entries.stream()
        // Filter not group owner
        .filter { entry -> entry.key.uuid != groupOwner.uuid }
        // Find unmatched filters
        .filter { entry -> entry.value!!.filters != ownerQueueData!!.filters }
        .map { entry -> entry.key.username }
        .collect(Collectors.toSet())

    if (playersWithUnmatchedFilters.isNotEmpty()) {
      sendMessageToAllPlayers(
        "group.round.filters-not-matched",
        "players" to playersWithUnmatchedFilters.joinToString(", "),
      )
      stopPreparation()
      return
    }

    val playersWithSettings: MutableMap<UUID, JsonObject> = HashMap()
    preparedPlayers.forEach { (player: Player?, miniGameQueueData: PlayerMiniGameQueueData?) ->
      playersWithSettings[player.uuid] = miniGameQueueData!!.settings
    }

    addPlayerGroupToSearchQueue(
      ownerQueueData!!.miniGameId,
      this,
      playersWithSettings,
      ownerQueueData.filters,
    )

    // Send message to all group
    val message =
      TranslateText(
        "rounds.new-round.queue.started",
        "minigameDisplayName" to
          TranslateText(
            Init.miniGamesSettingsManager
              .getGeneralGameSettings(ownerQueueData.miniGameId)!!.displayNameKey,
          ),
      )
    preparedPlayers.keys.forEach { player ->
      player.sendMiniMessage(message.string(player))
    }
  }

  private val allPlayers: Stream<Player>
    get() =
      Stream.concat(
        Stream.of(
          groupOwner,
        ),
        groupMembers.stream(),
      )

  private fun sendMessageToAllPlayers(messageKey: String, vararg variables: Pair<String, String?>) {
    allPlayers
      .forEach { player: Player? ->
        if (player != null) {
          PlayerLanguage.get(player).sendMiniMessage(messageKey, *variables)
        }
      }
  }
}
