package com.daylifecraft.minigames.minigames

import com.daylifecraft.minigames.event.player.minigame.PlayerPreparationEndEvent.PreparationResult
import com.daylifecraft.minigames.event.player.minigame.PlayerPreparationEvent
import com.daylifecraft.minigames.event.player.minigame.PlayerRoundJoinEvent
import com.daylifecraft.minigames.instance.CraftInstancesManager
import com.daylifecraft.minigames.minigames.instances.AbstractMiniGameInstance
import com.daylifecraft.minigames.minigames.queue.MiniGameQueueElement
import com.daylifecraft.minigames.minigames.queue.MiniGameQueueElement.Companion.createFromPlayers
import com.daylifecraft.minigames.minigames.queue.PlayerMiniGameLockData
import com.daylifecraft.minigames.minigames.queue.PlayerMiniGameQueueData
import com.daylifecraft.minigames.minigames.search.IRoundSearchProvider
import com.google.gson.JsonObject
import net.minestom.server.entity.Entity
import net.minestom.server.entity.Player
import net.minestom.server.event.EventDispatcher
import org.bson.types.ObjectId
import java.util.UUID
import java.util.function.Predicate

/** Manager to search rounds & manage player MiniGame block  */
object PlayerMiniGameManager {
  private val lockedPlayersUuids = mutableMapOf<UUID, PlayerMiniGameLockData>()

  private val _playersSearchQueue = mutableListOf<MiniGameQueueElement>()

  private val _activeMiniGameInstances = mutableListOf<AbstractMiniGameInstance>()

  /**
   * List of active queue elements. (Single player or group of them)
   */
  val playersSearchQueue: List<MiniGameQueueElement> = _playersSearchQueue

  /**
   * List of active mini-game instances
   */
  val activeMiniGameInstances: List<AbstractMiniGameInstance> = _activeMiniGameInstances

  /**
   * Blocks the player (He is in the MiniGame or in round search)
   *
   * @param playerUuid Player UUID
   */
  fun addLockedPlayer(playerUuid: UUID, miniGameId: String) {
    lockedPlayersUuids[playerUuid] = PlayerMiniGameLockData(miniGameId)
  }

  /**
   * Check does the player contains in list of blocked. (He is in the MiniGame or in round search)
   *
   * @param playerUuid Player UUID
   * @return True if player blocked, otherwise false
   */
  fun isPlayerLocked(playerUuid: UUID): Boolean = lockedPlayersUuids.containsKey(playerUuid)

  /**
   * Remove block from player. They stopped the game or abort round search
   *
   * @param playerUuid Player UUID
   */
  fun removeLockedPlayer(playerUuid: UUID) {
    lockedPlayersUuids.remove(playerUuid)
  }

  /**
   * Gets LockData for specific player OR Null, if player doesn't lock
   *
   * @param playerUuid Player UUID
   * @return Information about player MiniGame lock
   */
  fun getPlayerMiniGameLockData(playerUuid: UUID): PlayerMiniGameLockData? =
    lockedPlayersUuids[playerUuid]

  /**
   * Preparing player for RoundsSearch with specific DEFAULT settings & filters. It calls
   * `PlayerPreparationEvent` for MiniGames listener and blocks the player
   *
   * @param player Player instance
   * @param miniGameId String MiniGame ID from config
   * @param defaultSettings JsonObject dict type with default player settings
   * @param defaultFilters JsonObject dict type with default player filters
   */
  fun preparePlayerForRoundSearch(
    player: Player,
    miniGameId: String,
    roundSearchProvider: IRoundSearchProvider,
    defaultSettings: JsonObject = JsonObject(),
    defaultFilters: JsonObject = JsonObject(),
  ) {
    if (!roundSearchProvider.canBePrepared(player)) {
      return
    }

    // Block player
    addLockedPlayer(player.uuid, miniGameId)

    // Call event for MiniGames Listeners & wait for PlayerPreparationEndEvent
    val event =
      PlayerPreparationEvent(
        player,
        miniGameId,
        roundSearchProvider,
        defaultSettings,
        defaultFilters,
      )
    EventDispatcher.call(event)
  }

  /**
   * Calls by MiniGame listeners when Player ready to round search and have configured settings &
   * filters
   *
   * @param player Player Instance
   * @param playerMiniGameQueueData MiniGameID & Settings/Filters
   * @param preparationResult Result of preparation. It can be: cancelled or ready to round search
   */
  fun onPlayerPreparationEnd(
    player: Player,
    playerMiniGameQueueData: PlayerMiniGameQueueData,
    preparationResult: PreparationResult,
  ) {
    val roundSearchProvider = playerMiniGameQueueData.roundSearchProvider
    if (preparationResult == PreparationResult.CANCELLED) {
      removeLockedPlayer(player.uuid)
      roundSearchProvider.onPlayerCancelledPreparation(player, playerMiniGameQueueData)
      return
    }

    roundSearchProvider.onPlayerPrepared(player, playerMiniGameQueueData)
  }

  /**
   * Call this, when player was cancelled the round search
   *
   * @param player Player instance
   */
  fun onPlayerRejectRoundSearch(player: Player) {
    removeLockedPlayer(player.uuid)

    val miniGameQueueData =
      getMiniGameQueueElement(player.uuid)
        ?: // TODO 09.11.2023 Error
        return
    miniGameQueueData.roundSearchProvider.onPlayerRejectRoundSearch(player)
  }

  /**
   * When MiniGame instance initialized => we need to move all players to instance
   *
   * @param miniGameInstance MiniGame instance
   */
  fun onRoundPreparationEvent(miniGameInstance: AbstractMiniGameInstance) {
    for (player in miniGameInstance.getRoundPlayerSettings().keys) {
      player.setInstance(miniGameInstance.miniGameWorldInstance.instance)

      val joinEvent =
        PlayerRoundJoinEvent(
          player,
          miniGameInstance,
          miniGameInstance.getRoundPlayerSettings()[player]!!,
        )
      EventDispatcher.call(joinEvent)
    }
  }

  /**
   * Called when player leave from round
   *
   * @param player Player instance
   */
  fun onPlayerLeaveFromRoundEvent(player: Entity) {
    removeLockedPlayer(player.uuid)
  }

  /**
   * Add Player to Round Search Queue
   *
   * @param playerUuid Player UUID
   * @param miniGameQueueData MiniGameQueue data, which is obtained by PlayerPreparationEndEvent
   */
  fun addPlayerToSearchQueue(
    miniGameId: String,
    roundSearchProvider: IRoundSearchProvider,
    playerUuid: UUID,
    miniGameQueueData: PlayerMiniGameQueueData,
  ) {
    addPlayerGroupToSearchQueue(
      miniGameId,
      roundSearchProvider,
      java.util.Map.of(playerUuid, miniGameQueueData.settings),
      miniGameQueueData.filters,
    )
  }

  /**
   * Add group of players to Search Queue
   *
   * @param miniGameId MiniGame ID
   * @param roundSearchProvider Round search provider
   * @param playersWithSettings players with their settings
   * @param filters Game filters
   */
  fun addPlayerGroupToSearchQueue(
    miniGameId: String,
    roundSearchProvider: IRoundSearchProvider,
    playersWithSettings: Map<UUID, JsonObject>,
    filters: JsonObject,
  ) {
    _playersSearchQueue.add(
      createFromPlayers(
        miniGameId,
        roundSearchProvider,
        playersWithSettings,
        filters,
      ),
    )
  }

  /**
   * Remove player (Or player group with specified player) from search queue
   *
   * @param playerUuid Player UUID
   */
  fun removeFromSearchQueue(playerUuid: UUID) {
    _playersSearchQueue.removeIf { queueElement: MiniGameQueueElement ->
      queueElement.playersWithSettings.containsKey(playerUuid)
    }
  }

  /**
   * Remove player from Round Search Queue
   *
   * @param playerUuid Player UUID
   */
  fun removeFromSearchQueueAndUnlockGroup(playerUuid: UUID) {
    val miniGameQueueElement = getMiniGameQueueElement(playerUuid) ?: return

    miniGameQueueElement.playersWithSettings.keys.forEach { queuedPlayer: UUID ->
      removeLockedPlayer(queuedPlayer)
    }
    _playersSearchQueue.remove(miniGameQueueElement)
  }

  /**
   * Returns QueueData for specific player in search queue (if player is looking for a game)
   *
   * @param playerUuid Player UUID
   * @return real value, if player is searching for a game, else null
   */
  fun getMiniGameQueueElement(playerUuid: UUID): MiniGameQueueElement? =
    _playersSearchQueue.firstOrNull { it.playersWithSettings.containsKey(playerUuid) }

  /**
   * Called on player quit from server
   *
   * @param player Player instance
   */
  fun onPlayerQuit(player: Player) {
    val queueElement = getMiniGameQueueElement(player.uuid)

    queueElement?.roundSearchProvider?.onPlayerRejectRoundSearch(player)
  }

  /**
   * Add Round Instance to list of active rounds (When it started)
   *
   * @param abstractMiniGameInstance MiniGame Round instance
   */
  fun addActiveMiniGameInstance(abstractMiniGameInstance: AbstractMiniGameInstance) {
    _activeMiniGameInstances.add(abstractMiniGameInstance)
  }

  /**
   * Remove Round Instance from list of active rounds (When it stopped)
   *
   * @param abstractMiniGameInstance MiniGame Round instance
   */
  fun removeActiveMiniGameInstance(abstractMiniGameInstance: AbstractMiniGameInstance) {
    _activeMiniGameInstances.remove(abstractMiniGameInstance)
  }

  /**
   * Find MiniGame Round instance by its ObjectId (database id) or null
   *
   * @param objectId round ObjectId
   * @return Founded instance or null
   */
  fun getMiniGameRoundByObjectId(objectId: ObjectId): AbstractMiniGameInstance? =
    activeMiniGameInstances.firstOrNull { it.roundProfile.id == objectId }

  /**
   * Find MiniGame Round instance by player member
   *
   * @param player Player in round (member, not spectator!)
   * @return Founded instance or null
   */
  fun getMiniGameRoundByMember(player: Player): AbstractMiniGameInstance? =
    activeMiniGameInstances.firstOrNull { it.getRoundPlayerSettings().containsKey(player) }

  /**
   * Find MiniGame Round instance by player member
   *
   * @param spectator Spectator in round (not member!!)
   * @return Founded instance or null
   */
  fun getMiniGameRoundBySpectator(spectator: Player?): AbstractMiniGameInstance? =
    activeMiniGameInstances.firstOrNull { it.getSpectatorsList().contains(spectator) }

  /**
   * Unloads and removes from active list all instances without players in stop state
   */
  fun cleanEmptyInstances() {
    val toRemove = activeMiniGameInstances.filter {
      it.miniGameWorldInstance.instance.players.isEmpty() && it.roundProfile.roundStatus!!.isStopState
    }

    toRemove.forEach { CraftInstancesManager.get().unloadInstance(it.miniGameWorldInstance) }

    _activeMiniGameInstances.removeAll(toRemove)
  }

  /**
   * Find MiniGame Round instance by condition
   *
   * @param condition Condition (filter) for round search
   * @return Founded instance or null
   */
  fun getMiniGameRoundByCondition(condition: Predicate<AbstractMiniGameInstance?>): AbstractMiniGameInstance? = activeMiniGameInstances.stream()
    .filter(condition)
    .findAny()
    .orElse(null)
}
