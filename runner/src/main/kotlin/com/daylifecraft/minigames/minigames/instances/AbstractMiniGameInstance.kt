package com.daylifecraft.minigames.minigames.instances

import com.daylifecraft.common.logging.building.createLogger
import com.daylifecraft.minigames.event.minigame.RoundReadyEvent
import com.daylifecraft.minigames.event.player.minigame.PlayerRoundLeaveEvent
import com.daylifecraft.minigames.instance.CraftInstancesManager
import com.daylifecraft.minigames.instance.InstancePlayerState
import com.daylifecraft.minigames.instance.InstanceUtil
import com.daylifecraft.minigames.instance.instances.games.MiniGameWorldInstance
import com.daylifecraft.minigames.minigames.PlayerMiniGameManager
import com.daylifecraft.minigames.minigames.profile.RoundProfile
import com.daylifecraft.minigames.minigames.profile.RoundStatus
import com.google.gson.JsonObject
import net.minestom.server.MinecraftServer
import net.minestom.server.attribute.Attribute
import net.minestom.server.entity.Player
import net.minestom.server.event.Event
import net.minestom.server.event.EventDispatcher
import net.minestom.server.event.EventFilter
import net.minestom.server.event.EventNode
import net.minestom.server.event.trait.PlayerEvent

/**
 * Abstract MiniGame instance that stores World instance, RoundProfile in database, players with
 * settings, game filters And provides abstract interaction
 */
abstract class AbstractMiniGameInstance protected constructor(
  @JvmField val miniGameWorldInstance: MiniGameWorldInstance,
  @JvmField val roundProfile: RoundProfile,
  roundPlayerSettings: Map<Player, JsonObject>?,
  val gameFilters: JsonObject,
) {
  private val roundPlayerSettings: MutableMap<Player, JsonObject> =
    HashMap(roundPlayerSettings)

  private val events: EventNode<Event> = EventNode
    .type("AbstractMiniGameInstance", EventFilter.ALL) { event: Event, _ ->
      if ((event is PlayerEvent) &&
        spectatorsList.contains(event.player)
      ) {
        return@type false
      }

      InstanceUtil.isEventRelated(event, miniGameWorldInstance)
    }

  private var readyPlayersCount = 0

  private val spectatorsList: MutableList<Player> = ArrayList()

  private val logger = createLogger()

  fun getRoundPlayerSettings(): Map<Player, JsonObject> = HashMap(roundPlayerSettings)

  fun getSpectatorsList(): List<Player> = ArrayList(spectatorsList)

  protected inline fun <reified T : Event> addEventListener(noinline handler: (T) -> Unit) {
    addEventListener(T::class.java, handler)
  }

  protected fun <T : Event?> addEventListener(eventType: Class<T>, handler: (T) -> Unit) {
    events.addListener(eventType, handler)
  }

  protected fun registerEvents() {
    MinecraftServer.getGlobalEventHandler().addChild(events)
  }

  private fun unregisterEvents() {
    MinecraftServer.getGlobalEventHandler().removeChild(events)
  }

  /**
   * When PlayerRoundJoinEvent called => player moved to instance
   *
   * @param player Player
   * @param playerSettings Player setting in JSON
   */
  open fun onPlayerRoundJoinEvent(player: Player, playerSettings: JsonObject) {
    readyPlayersCount++

    if (readyPlayersCount == roundPlayerSettings.size) {
      onAllPlayersReady()
    }
  }

  protected open fun onAllPlayersReady() {
    val roundReadyEvent = RoundReadyEvent(this)
    EventDispatcher.call(roundReadyEvent)

    PlayerMiniGameManager.addActiveMiniGameInstance(this)
  }

  protected open fun startRound() {
    roundProfile.setRoundStatusAndUpdate(RoundStatus.STARTED)
  }

  protected fun stopRound() {
    roundProfile.endRound()

    for (spectator in ArrayList(spectatorsList)) {
      removeSpectator(spectator)
    }

    for (player in HashSet(roundPlayerSettings.keys)) {
      onPlayerLeaveFromRound(player)
    }

    unregisterEvents()
  }

  private fun onPlayerLeaveFromRound(player: Player) {
    clearPlayerAttributes(player)

    roundPlayerSettings.remove(player)

    CraftInstancesManager.get().toLobby(player)

    val roundLeaveEvent = PlayerRoundLeaveEvent(this, player)
    EventDispatcher.call(roundLeaveEvent)
  }

  protected fun removePlayerFromRound(player: Player) {
    onPlayerLeaveFromRound(player)

    if (roundPlayerSettings.isEmpty()) {
      stopRound()
    }
  }

  /**
   * Add spectating player to round
   *
   * @param spectator Spectator player instance
   * @param target Target player, if spectator join round with specified player to spectate
   */
  open fun addSpectator(spectator: Player, target: Player?) {
    if (getRoundPlayerSettings().containsKey(spectator)) {
      logger.debug(
        "MiniGame ${roundProfile.miniGameId} trying to add ${spectator.username} " +
          "in spectators, but it contained by round",
      )
      return
    }

    spectatorsList.add(spectator)

    // Add lock
    PlayerMiniGameManager.addLockedPlayer(spectator.uuid, roundProfile.miniGameId)

    // Move spectator to instance
    if (target == null) {
      miniGameWorldInstance.addSpawnPoint(
        spectator.uuid,
        miniGameWorldInstance.anySpawnPosition,
      )
    } else {
      miniGameWorldInstance.addSpawnPoint(
        spectator.uuid,
        miniGameWorldInstance.getSpawnPos(target),
      )
    }
    spectator.setInstance(miniGameWorldInstance.instance)

    // Hide spectator
    hideSpectatorForPlayers(spectator)
  }

  /**
   * Remove player from spectators
   *
   * @param spectator Spectator player instance
   */
  open fun removeSpectator(spectator: Player) {
    spectatorsList.remove(spectator)

    // Remove lock
    PlayerMiniGameManager.removeLockedPlayer(spectator.uuid)

    // Move to lobby
    CraftInstancesManager.get().toLobby(spectator)

    // Clear any attributes
    clearPlayerAttributes(spectator)
  }

  private fun hideSpectatorForPlayers(spectator: Player) {
    roundPlayerSettings
      .keys
      .forEach { roundPlayer: Player -> roundPlayer.viewers.remove(spectator) }
  }

  /**
   * Method called, when recipient tries to send message to sender.
   *
   * @param recipient Player instance
   * @param sender Player instance
   * @return True, if message can be sent. Otherwise False
   */
  fun canReceiveMessageFrom(recipient: Player, sender: Player): Boolean = !spectatorsList.contains(sender) || !roundPlayerSettings.containsKey(recipient)

  /**
   * Method called, when player settings up for moving to instance
   *
   * @param player Player instance
   * @param instancePlayerState player state data, which could be edited
   */
  abstract fun setupPlayerToInstance(player: Player, instancePlayerState: InstancePlayerState)

  /**
   * Method called, when player joined to instance
   *
   * @param player Player instance
   */
  abstract fun onPlayerJoinToInstance(player: Player)

  /**
   * Method called, when player left from instance
   *
   * @param player Player instance
   */
  abstract fun onPlayerLeaveFromInstance(player: Player)

  companion object {
    private fun clearPlayerAttributes(player: Player) {
      // Clear attribute modifiers
      for (attribute in Attribute.values()) {
        for (attributeModifier in player.getAttribute(attribute).modifiers) {
          player.getAttribute(attribute).removeModifier(attributeModifier)
        }
      }

      // Remove title & clear effects
      player.clearTitle()
      player.clearEffects()
    }
  }
}
