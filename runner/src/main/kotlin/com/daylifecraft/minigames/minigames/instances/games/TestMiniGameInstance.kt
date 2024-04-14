package com.daylifecraft.minigames.minigames.instances.games

import com.daylifecraft.minigames.instance.InstancePlayerState
import com.daylifecraft.minigames.instance.instances.games.MiniGameWorldInstance
import com.daylifecraft.minigames.minigames.instances.AbstractMiniGameInstance
import com.daylifecraft.minigames.minigames.profile.RoundProfile
import com.daylifecraft.minigames.minigames.profile.RoundStatus
import com.google.gson.JsonObject
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import net.minestom.server.MinecraftServer
import net.minestom.server.entity.GameMode
import net.minestom.server.entity.Player
import net.minestom.server.event.player.PlayerBlockPlaceEvent
import net.minestom.server.event.player.PlayerDisconnectEvent
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.timer.TaskSchedule
import java.util.concurrent.atomic.AtomicInteger

class TestMiniGameInstance private constructor(
  craftInstance: MiniGameWorldInstance,
  roundProfile: RoundProfile,
  playerSettings: Map<Player, JsonObject>,
  gameFilters: JsonObject,
) : AbstractMiniGameInstance(craftInstance, roundProfile, playerSettings, gameFilters) {
  private fun initEvents() {
    addEventListener(::onBlockPlace)
    addEventListener(::onPlayerDisconnect)
  }

  override fun onAllPlayersReady() {
    super.onAllPlayersReady()

    val timer = AtomicInteger(TIMER_SECONDS)
    MinecraftServer.getSchedulerManager()
      .submitTask {
        showTimerToPlayers(timer.get())
        if (timer.decrementAndGet() == 0) {
          if (roundProfile.roundStatus == RoundStatus.PREPARING) {
            startRound()
          }
          return@submitTask TaskSchedule.stop()
        }
        TaskSchedule.seconds(1)
      }
  }

  private fun showTimerToPlayers(seconds: Int) {
    for (player in getRoundPlayerSettings().keys) {
      player.showTitle(Title.title(Component.text("Time left: $seconds"), Component.empty()))
    }
  }

  override fun startRound() {
    super.startRound()

    // Move players to own locations
    for (player in getRoundPlayerSettings().keys) {
      player.setGameMode(GameMode.CREATIVE)
      player.teleport(miniGameWorldInstance.loadPos(player.uuid)!!)
    }
  }

  private fun onBlockPlace(blockPlaceEvent: PlayerBlockPlaceEvent) {
    val material = blockPlaceEvent.block.registry().material()

    when (material) {
      Material.DIRT -> removePlayerFromRound(blockPlaceEvent.player)
      Material.OBSIDIAN -> stopRound()
      else -> Unit
    }
  }

  private fun onPlayerDisconnect(playerDisconnectEvent: PlayerDisconnectEvent) {
    removePlayerFromRound(playerDisconnectEvent.player)
  }

  override fun addSpectator(spectator: Player, target: Player?) {
    super.addSpectator(spectator, target)

    spectator.setGameMode(GameMode.SPECTATOR)
    spectator.isAutoViewable = false
  }

  override fun removeSpectator(spectator: Player) {
    super.removeSpectator(spectator)

    spectator.isAutoViewable = true
  }

  override fun setupPlayerToInstance(player: Player, instancePlayerState: InstancePlayerState) {
    instancePlayerState.gameMode = GameMode.ADVENTURE
  }

  override fun onPlayerJoinToInstance(player: Player) {
    if (getSpectatorsList().contains(player)) {
      return
    }

    val playerInventory = player.inventory

    playerInventory.setItemStack(0, ItemStack.of(Material.DIRT))
    playerInventory.setItemStack(1, ItemStack.of(Material.OBSIDIAN))
  }

  override fun onPlayerLeaveFromInstance(player: Player) {
    val playerInventory = player.inventory

    playerInventory.clear()
  }

  companion object {
    private const val TIMER_SECONDS = 30

    /**
     * Create game instance
     *
     * @param craftInstance World instance
     * @param roundProfile RoundProfile (database object)
     * @param playerSettings Player with their settings
     * @param gameFilters final game filters
     * @return creates game instance
     */
    fun createInstance(
      craftInstance: MiniGameWorldInstance,
      roundProfile: RoundProfile,
      playerSettings: Map<Player, JsonObject>,
      gameFilters: JsonObject,
    ): TestMiniGameInstance {
      val instance =
        TestMiniGameInstance(craftInstance, roundProfile, playerSettings, gameFilters)

      instance.initEvents()
      instance.registerEvents()

      return instance
    }
  }
}
