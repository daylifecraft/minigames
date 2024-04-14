package com.daylifecraft.minigames.instance

import com.daylifecraft.common.instance.InstanceType
import com.daylifecraft.minigames.Init.chatManager
import com.daylifecraft.minigames.event.player.PlayerInstanceChatEvent
import com.daylifecraft.minigames.util.ChatUtil.checkMiniMessage
import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.Player
import net.minestom.server.event.Event
import net.minestom.server.event.EventDispatcher
import net.minestom.server.event.EventFilter
import net.minestom.server.event.EventNode
import net.minestom.server.instance.Instance
import net.minestom.server.instance.InstanceManager
import java.util.UUID

abstract class AbstractCraftInstance {
  private val playersPos: MutableMap<UUID, Pos> = HashMap()
  protected val events: EventNode<Event> = EventNode
    .type("AbstractCraftInstance", EventFilter.ALL) { event: Event, _ ->
      InstanceUtil.isEventRelated(event, this@AbstractCraftInstance)
    }

  /**
   * Get the controller of the current instance
   *
   * @return controller for this instance
   */
  var controller: InstanceController? = null
    private set

  /** Gets type of this CraftInstance  */
  abstract val type: InstanceType

  /** Gets instance in CraftInstance  */
  abstract val instance: Instance

  /** Gets CraftInstance uuid  */
  val instanceUniqueId: UUID
    get() = instance.uniqueId

  protected val manager: CraftInstancesManager
    get() = controller!!.manager

  protected val defaultManager: InstanceManager
    get() = controller!!.defaultManager

  /**
   * Gets all online players
   *
   * @get array of all players on that instance
   */
  val onlinePlayers: Array<Player>
    get() = instance.players.toTypedArray<Player>()

  private val isAttached: Boolean
    get() = controller != null

  /** Check if player is missing  */
  abstract fun isMissingPlayer(player: Player): Boolean

  /** Gets priority of missing player  */
  abstract fun getMissingPlayerPriority(player: Player): Long

  /**
   * Setup player for this CraftInstance
   *
   * @param player player instance
   */
  abstract fun setupPlayer(player: Player): InstancePlayerState

  /**
   * Triggered only when player instance is changed.
   * Made to clear the player before moving them to another instance.
   * And not being called when player leaves the server.
   *
   * @param player player instance
   */
  abstract fun playerLeave(player: Player)

  /**
   * Triggers the player's join to CraftInstance
   *
   * @param player player instance
   */
  abstract fun playerJoin(player: Player)

  /**
   * Triggers the player disconnect
   *
   * @param player player instance
   */
  abstract fun playerDisconnect(player: Player)

  /**
   * Attach current instance to controller
   *
   * @param instanceController InstanceController instance
   */
  open fun attach(instanceController: InstanceController) {
    check(!isAttached) { "Instance already attached!" }
    controller = instanceController
    instanceController.attached(this)
  }

  /** Detach current instance to controller  */
  fun detach() {
    unregisterEvents()
    if (isAttached) {
      controller!!.detached(this)
    }
    controller = null
  }

  protected fun registerEvents() {
    MinecraftServer.getGlobalEventHandler().addChild(events)
  }

  protected fun unregisterEvents() {
    MinecraftServer.getGlobalEventHandler().addChild(events)
  }

  protected inline fun <reified T : Event> addEventListener(noinline handler: (T) -> Unit) {
    addEventListener(T::class.java, handler)
  }

  protected fun <T : Event?> addEventListener(eventType: Class<T>, handler: (T) -> Unit) {
    events.addListener(eventType, handler)
  }

  /**
   * Sends a Chat message to players in current instance. Used for isolate instances chats.
   *
   * @param sender Message sender
   * @param message Message text
   */
  fun sendInstanceChatMessage(sender: Player?, message: String?) {
    val recipients: MutableList<Player> = ArrayList()
    val formattedMessage = checkMiniMessage(sender!!, message!!)

    for (recipient in instance.players) {
      if (!canReceiveMessageFrom(recipient, sender)) {
        continue
      }

      recipients.add(recipient)
      chatManager!!.sendPlayerChatMessage(sender, recipient!!, formattedMessage)
    }

    val event = PlayerInstanceChatEvent(sender, this, formattedMessage, recipients)
    EventDispatcher.call(event)
  }

  /**
   * Saves the player's position
   *
   * @param uuid uuid of player
   * @param pos position to save
   */
  fun rememberPos(uuid: UUID, pos: Pos) {
    playersPos[uuid] = pos
  }

  /**
   * Gets player saved position
   *
   * @param uuid uuid of player
   * @return position for player
   */
  fun loadPos(uuid: UUID): Pos? = playersPos[uuid]

  /**
   * Gets player spawn position
   *
   * @param player player instance
   * @return position for player
   */
  abstract fun getSpawnPos(player: Player?): Pos

  /**
   * Checks whether player positions should be saved for the instance
   *
   * @return true if the instance needs to save the players' positions
   */
  open fun doRememberPos(): Boolean = false

  protected fun setupPlayerPos(playerState: InstancePlayerState, player: Player) {
    playerState.pos = if (doRememberPos()) {
      loadPos(player.uuid) ?: getSpawnPos(player).also { rememberPos(player.uuid, it) }
    } else {
      getSpawnPos(player)
    }
  }

  /** Unload this instance  */
  fun unloadInstance() {
    MinecraftServer.getInstanceManager().unregisterInstance(instance)
    detach()
  }

  /**
   * Check if player can receive message from sender
   *
   * @param recipient who will receive message
   * @param sender who send message
   * @return true if recipient can receive message from sender
   */
  open fun canReceiveMessageFrom(recipient: Player, sender: Player): Boolean = true
}
