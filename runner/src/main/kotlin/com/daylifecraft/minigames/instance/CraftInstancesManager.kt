package com.daylifecraft.minigames.instance

import com.daylifecraft.common.instance.InstanceType
import com.daylifecraft.common.logging.building.createLogger
import com.daylifecraft.minigames.Init.craftInstancesManager
import com.daylifecraft.minigames.event.instance.InstanceAttachEvent
import com.daylifecraft.minigames.event.instance.InstanceDetachEvent
import com.daylifecraft.minigames.instance.InstanceUtil.moveAllPlayersToInstance
import com.daylifecraft.minigames.util.sendStackTrace
import net.minestom.server.entity.Entity
import net.minestom.server.entity.Player
import net.minestom.server.event.EventDispatcher
import net.minestom.server.instance.Instance
import net.minestom.server.instance.InstanceManager
import java.util.UUID
import java.util.function.Predicate

class CraftInstancesManager(private val instanceManager: InstanceManager) {
  private val instanceList: MutableList<AbstractCraftInstance> = ArrayList()
  private val instanceController: InstanceController = CraftInstanceController()
  private val playersLastInstance: MutableMap<UUID, Instance> = HashMap()

  /**
   * Update last player instance (that they in)
   *
   * @param player player instance
   * @param instance the instance to which we update
   */
  private fun updateLastPlayerInstance(player: Player, instance: Instance) {
    playersLastInstance[player.uuid] = instance
  }

  private fun removeLastPlayerInstance(player: Player) {
    playersLastInstance.remove(player.uuid)
  }

  /**
   * Returns the player's last active instance. May return null if the player is just logging into
   * the server.
   *
   * @param player player instance
   * @return last player instance
   */
  fun getLastPlayerInstance(player: Entity): Instance? = playersLastInstance[player.uuid]

  /**
   * Say to CraftInstancesManager global event PlayerSpawnEvent
   *
   *
   * CONTEXT: Player.getInstance() return spawnInstance.
   *
   * @param spawnInstance from event
   * @param player from event
   */
  fun notifyPlayerSpawn(spawnInstance: Instance, player: Player) {
    val instance = getInstance(spawnInstance)
    instance!!.playerJoin(player)
    updateLastPlayerInstance(player, spawnInstance)
  }

  /** Notify from global event: player leave from instance (PlayerSpawnEvent)  */
  fun notifyPlayerLeave(disconnectInstance: Instance, player: Player) {
    val instance = getInstance(disconnectInstance)
    instance!!.playerLeave(player)
  }

  /** Notify from global event: player disconnected from server (PlayerDisconnectEvent)  */
  fun notifyPlayerDisconnect(disconnectInstance: Instance, player: Player) {
    val instance = getInstance(disconnectInstance)
    instance!!.playerDisconnect(player)
    removeLastPlayerInstance(player)
  }

  /**
   * Attach an instance to the manager
   *
   * @param craftInstance craft instance
   */
  fun addInstance(craftInstance: AbstractCraftInstance) {
    craftInstance.attach(instanceController)
  }

  fun getLoginInstanceAndPrepare(player: Player): Instance {
    val instances =
      getInstances { craftInstance: AbstractCraftInstance? -> craftInstance!!.isMissingPlayer(player) }
    var resultInstance: AbstractCraftInstance? = null
    var maxPriority = Long.MIN_VALUE
    for (currInstance in instances) {
      val p = currInstance!!.getMissingPlayerPriority(player)
      if (p > maxPriority) {
        maxPriority = p
        resultInstance = currInstance
      }
    }

    if (resultInstance == null) {
      resultInstance = anyLobby
    }

    return resultInstance!!.instance
  }

  /**
   * Move this player to any lobby server.
   *
   * @param player player instance
   */
  fun toLobby(player: Player) {
    sendStackTrace(player)
    player.setInstance(anyLobby!!.instance)
  }

  private val instances: Array<AbstractCraftInstance>
    get() = instanceList.toTypedArray<AbstractCraftInstance>()

  /**
   * Gets all instances in manager with specified type
   *
   * @param instanceType type we need to find
   * @return array of instances
   */
  fun getInstances(instanceType: InstanceType): Array<AbstractCraftInstance?> = getInstances { craftInstance: AbstractCraftInstance? -> craftInstance!!.type == instanceType }

  private fun getInstances(predicate: Predicate<AbstractCraftInstance?>): Array<AbstractCraftInstance?> {
    val instances: MutableList<AbstractCraftInstance?> = ArrayList()
    for (instance in this.instances) {
      if (predicate.test(instance)) {
        instances.add(instance)
      }
    }
    return instances.toTypedArray<AbstractCraftInstance?>()
  }

  private val anyLobby: AbstractCraftInstance?
    get() = getAnyInstanceByType(InstanceType.LOBBY)

  /**
   * Gets the first instance of the specified type found.
   *
   * @param type type we need to find
   * @return instance
   */
  fun getAnyInstanceByType(type: InstanceType): AbstractCraftInstance? = getInstance { craftInstance: AbstractCraftInstance? -> craftInstance!!.type == type }

  /**
   * Gets the instance at the specified uuid if it is not null.
   *
   * @param id uuid of instance
   * @return instance
   */
  fun getInstanceById(id: UUID): AbstractCraftInstance? = getInstance { craftInstance: AbstractCraftInstance? -> id == craftInstance!!.instanceUniqueId }

  /**
   * Gets the instance where the entity is
   *
   * @param entity entity instance
   * @return instance
   */
  fun getPlayerInstance(entity: Entity): AbstractCraftInstance? = getInstance(entity.instance)

  private fun getInstance(find: Instance): AbstractCraftInstance? = getInstance { craftInstance: AbstractCraftInstance? -> craftInstance!!.instance === find }

  /**
   * Gets an instance from the manager by predicate
   *
   * @param predicate predicate
   * @return instance
   */
  fun getInstance(predicate: Predicate<AbstractCraftInstance>): AbstractCraftInstance? {
    for (instance in instances) {
      if (predicate.test(instance)) {
        return instance
      }
    }
    return null
  }

  /**
   * Detach an instance from a manager
   *
   * @param craftInstance instance
   */
  fun removeInstance(craftInstance: AbstractCraftInstance) {
    craftInstance.detach()
  }

  /**
   * Unload instance
   *
   * @param craftInstance instance
   */
  fun unloadInstance(craftInstance: AbstractCraftInstance) {
    craftInstance.unloadInstance()
  }

  private inner class CraftInstanceController : InstanceController {
    override val manager: CraftInstancesManager
      get() = this@CraftInstancesManager

    override val defaultManager: InstanceManager
      get() = instanceManager

    override fun detached(craftInstance: AbstractCraftInstance?) {
      handleDetached(craftInstance)
    }

    override fun attached(craftInstance: AbstractCraftInstance?) {
      handleAttached(craftInstance)
    }

    private fun handleDetached(craftInstance: AbstractCraftInstance?) {
      EventDispatcher.call(InstanceDetachEvent(craftInstance!!, manager))

      LOGGER.debug("detached() instance $craftInstance")
      val players = craftInstance.onlinePlayers
      if (players.isNotEmpty()) {
        LOGGER.debug("WARN detached() called with players online: move to lobby")
        for (player in players) {
          sendStackTrace(player, "WARN: Detached with online players. You moved to lobby")
        }
        moveAllPlayersToInstance(players, anyLobby!!)
      }
      instanceList.remove(craftInstance)
    }

    private fun handleAttached(craftInstance: AbstractCraftInstance?) {
      EventDispatcher.call(InstanceAttachEvent(craftInstance!!, manager))
      instanceList.add(craftInstance)
    }
  }

  companion object {
    private val LOGGER = createLogger<CraftInstancesManager>()

    @JvmStatic
    fun get(): CraftInstancesManager = craftInstancesManager!!
  }
}
