package com.daylifecraft.minigames.instance.instances.lobby

import com.daylifecraft.common.gui.player.inventory.PlayerInventoryGui
import com.daylifecraft.common.instance.InstanceType
import com.daylifecraft.common.util.extensions.minestom.scheduleTask
import com.daylifecraft.common.util.posOf
import com.daylifecraft.minigames.PermissionManager
import com.daylifecraft.minigames.PlayerManager
import com.daylifecraft.minigames.database.DatabaseManager
import com.daylifecraft.minigames.instance.AbstractCraftInstance
import com.daylifecraft.minigames.instance.InstanceController
import com.daylifecraft.minigames.instance.InstancePlayerState
import com.daylifecraft.minigames.instance.InstanceUtil
import com.daylifecraft.minigames.instance.InstanceUtil.createCollisionTeam
import com.daylifecraft.minigames.instance.instances.lobby.gui.GlobalGui
import com.daylifecraft.minigames.util.FilesUtil.getResourcesPath
import com.daylifecraft.minigames.util.sendStackTrace
import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.GameMode
import net.minestom.server.entity.Player
import net.minestom.server.entity.damage.EntityDamage
import net.minestom.server.event.entity.EntityDamageEvent
import net.minestom.server.event.item.ItemDropEvent
import net.minestom.server.event.player.PlayerBlockBreakEvent
import net.minestom.server.event.player.PlayerBlockPlaceEvent
import net.minestom.server.event.player.PlayerDisconnectEvent
import net.minestom.server.event.trait.CancellableEvent
import net.minestom.server.instance.AnvilLoader
import net.minestom.server.instance.InstanceContainer
import net.minestom.server.instance.LightingChunk
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.network.packet.server.play.TeamsPacket
import net.minestom.server.scoreboard.Team
import net.minestom.server.timer.TaskSchedule
import java.time.Duration
import java.util.UUID

private val LOBBY_SPAWN_POSITION = posOf(x = 48.0, y = 24.0, z = -109.0)
private const val LOBBY_TEAM = "LobbyTeam"
private const val TELEPORT_TO_SPAWN_Y_THRESHOLD = -60

class LobbyInstance : AbstractCraftInstance() {
  override val type: InstanceType = InstanceType.LOBBY
  override lateinit var instance: InstanceContainer
  private var team: Team? = null

  private val playersGuis = mutableMapOf<UUID, PlayerInventoryGui>()

  override fun getSpawnPos(player: Player?): Pos = LOBBY_SPAWN_POSITION

  override fun isMissingPlayer(player: Player): Boolean = false

  override fun getMissingPlayerPriority(player: Player): Long = 0

  override fun setupPlayer(player: Player): InstancePlayerState {
    sendStackTrace(player)

    val playerState = InstancePlayerState()
    setupPlayerPos(playerState, player)
    playerState.gameMode = GameMode.ADVENTURE
    return playerState
  }

  override fun playerLeave(player: Player) {
    sendStackTrace(player)
    player.team = null
    PlayerManager.setPlayerHide(player, false)

    removeInventoryGuiOf(player)
  }

  override fun playerJoin(player: Player) {
    sendStackTrace(player)
    player.team = team
    player.isAllowFlying = PermissionManager.hasPermission(player, "lobby.fly-allowed")

    createInventoryGuiFor(player)

    PlayerManager.setPlayerHide(
      player,
      DatabaseManager.getPlayerProfile(player)!!.settings!!.hidePlayers,
    )

    // TODO 10.07.2023 delete or I18n
    player.sendMessage("Hello in lobby! TODO delete")
  }

  override fun playerDisconnect(player: Player) {
    // Player disconnect logic is not needed
  }

  override fun attach(instanceController: InstanceController) {
    super.attach(instanceController)
    instance =
      instanceController.defaultManager.createInstanceContainer().apply {
        chunkLoader = AnvilLoader(getResourcesPath("worlds/lobby"))
        enableAutoChunkLoad(true)
        setChunkSupplier(::LightingChunk)
        timeRate = 0
        timeUpdate = null
        time = InstanceUtil.DAY_TIME
      }

    // NEVER for collision from player
    team = createCollisionTeam(LOBBY_TEAM, TeamsPacket.CollisionRule.NEVER)

    initEvents()
    startPlayersRescuerTask()

    registerEvents()
  }

  private fun createInventoryGuiFor(player: Player) {
    val inventoryGui = PlayerInventoryGui(player).apply {
      setItem(0, ItemStack.of(Material.NETHER_STAR)) {
        onRightClick {
          GlobalGui.showGuiToPlayer(player)
        }
      }
    }

    inventoryGui.attachEvents(parent = events)
    playersGuis[player.uuid] = inventoryGui
  }

  private fun startPlayersRescuerTask() {
    MinecraftServer.getSchedulerManager().scheduleTask(
      delay = TaskSchedule.immediate(),
      repeat = TaskSchedule.duration(Duration.ofSeconds(1)),
    ) {
      for (player in instance.players) {
        if (player.instance === instance &&
          player.position.blockY() < TELEPORT_TO_SPAWN_Y_THRESHOLD
        ) {
          player.teleport(getSpawnPos(player))
        }
      }
    }
  }

  private fun initEvents() {
    addEventListener<ItemDropEvent>(::cancelEvent)
    addEventListener<PlayerBlockBreakEvent>(::cancelEvent)
    addEventListener<PlayerBlockPlaceEvent>(::cancelEvent)
    addEventListener<EntityDamageEvent>(::eventCancelPvp)
    addEventListener<PlayerDisconnectEvent> {
      removeInventoryGuiOf(it.player)
    }
  }

  private fun cancelEvent(event: CancellableEvent) {
    event.isCancelled = true
  }

  private fun eventCancelPvp(entityDamageEvent: EntityDamageEvent) {
    // If damage to Player
    if (entityDamageEvent.entity.entityType === EntityType.PLAYER) {
      entityDamageEvent.isCancelled = true
      return
    }

    // if player attempt damage entity
    if (entityDamageEvent.damage is EntityDamage &&
      (entityDamageEvent.damage.source?.entityType === EntityType.PLAYER)
    ) {
      entityDamageEvent.isCancelled = true
    }
  }

  private fun removeInventoryGuiOf(player: Player) {
    playersGuis[player.uuid]?.detachEvents()
    playersGuis.remove(player.uuid)
  }
}
