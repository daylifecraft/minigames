package com.daylifecraft.minigames.instance.instances.games

import com.daylifecraft.common.instance.InstanceType
import com.daylifecraft.minigames.instance.AbstractCraftInstance
import com.daylifecraft.minigames.instance.InstanceController
import com.daylifecraft.minigames.instance.InstancePlayerState
import com.daylifecraft.minigames.instance.InstanceUtil
import com.daylifecraft.minigames.minigames.instances.AbstractMiniGameInstance
import com.daylifecraft.minigames.util.FilesUtil.getResourcesPath
import com.daylifecraft.minigames.util.sendStackTrace
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.Player
import net.minestom.server.instance.AnvilLoader
import net.minestom.server.instance.InstanceContainer
import net.minestom.server.instance.LightingChunk
import java.util.UUID

typealias ReceiveMessageConsumer = (Player, Player) -> Boolean

private val ZERO_POSITION = Pos(0.0, 0.0, 0.0)

/** General MiniGame World instance to control world  */
open class MiniGameWorldInstance(
  private val miniGameId: String,
  private val worldName: String,
  playersSpawnPoints: Map<UUID, Pos>?,
) : AbstractCraftInstance() {
  override val type = InstanceType.MINI_GAME
  override lateinit var instance: InstanceContainer

  private val playersSpawnPoints: MutableMap<UUID, Pos> =
    HashMap(playersSpawnPoints)

  private lateinit var setupPlayerConsumer: (Player, InstancePlayerState) -> Unit

  private lateinit var onPlayerJoin: (Player) -> Unit
  private lateinit var onPlayerLeave: (Player) -> Unit

  private var abilityReceiveMessageConsumer: ReceiveMessageConsumer? = null

  override fun isMissingPlayer(player: Player): Boolean = false

  override fun getMissingPlayerPriority(player: Player): Long = 0

  override fun doRememberPos(): Boolean = true

  override fun getSpawnPos(player: Player?): Pos = playersSpawnPoints[player!!.uuid]
    ?: error("Could not find spawn point for player: ${player.username}")

  override fun attach(instanceController: InstanceController) {
    super.attach(instanceController)
    instance =
      instanceController.defaultManager.createInstanceContainer().apply {
        chunkLoader =
          AnvilLoader(
            getResourcesPath(
              "games/%s/worlds/%s".format(miniGameId, worldName),
            ),
          )
        enableAutoChunkLoad(true)

        setChunkSupplier(::LightingChunk)
        timeRate = 0
        timeUpdate = null
        time = InstanceUtil.DAY_TIME
      }
  }

  override fun setupPlayer(player: Player): InstancePlayerState {
    sendStackTrace(player)

    val playerState = InstancePlayerState()
    setupPlayerPos(playerState, player)
    setupPlayerConsumer(player, playerState)
    return playerState
  }

  override fun playerJoin(player: Player) {
    onPlayerJoin(player)
  }

  override fun playerDisconnect(player: Player) {
    // Player disconnect logic is not needed
  }

  override fun playerLeave(player: Player) {
    onPlayerLeave(player)
  }

  fun addSpawnPoint(player: UUID, spawnPoint: Pos) {
    playersSpawnPoints[player] = spawnPoint
  }

  val anySpawnPosition: Pos
    get() = playersSpawnPoints.values.stream().findAny().orElse(ZERO_POSITION)

  override fun canReceiveMessageFrom(recipient: Player, sender: Player): Boolean =
    abilityReceiveMessageConsumer?.let { canReceiveMessageFrom ->
      canReceiveMessageFrom(recipient, sender)
    } ?: super.canReceiveMessageFrom(recipient, sender)

  /**
   * Set instance consumers. They will be references to RoundInstance methods.
   *
   * @param abstractMiniGameInstance Round instance
   */
  fun setInstanceConsumers(abstractMiniGameInstance: AbstractMiniGameInstance) {
    abilityReceiveMessageConsumer = abstractMiniGameInstance::canReceiveMessageFrom
    setupPlayerConsumer = abstractMiniGameInstance::setupPlayerToInstance
    onPlayerJoin = abstractMiniGameInstance::onPlayerJoinToInstance
    onPlayerLeave = abstractMiniGameInstance::onPlayerLeaveFromInstance
  }
}
