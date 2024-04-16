package com.daylifecraft.minigames

import com.daylifecraft.common.util.extensions.minestom.addListener
import com.daylifecraft.minigames.PlayerManager.getPlayerUuid
import com.daylifecraft.minigames.fakeplayer.FakePlayer
import com.daylifecraft.minigames.fakeplayer.FakePlayerOption
import net.minestom.server.MinecraftServer
import net.minestom.server.entity.Entity
import net.minestom.server.entity.Player
import net.minestom.server.event.EventFilter
import net.minestom.server.event.EventNode
import net.minestom.server.event.player.PlayerSpawnEvent
import net.minestom.server.instance.Instance
import java.util.UUID

object UtilsForTesting {
  private val DEFAULT_FAKE_PLAYER_OPTIONS: FakePlayerOption =
    FakePlayerOption().setRegistered(true).setInTabList(true)

  private const val DEFAULT_WAITING_TIMEOUT_MS: Long = 100

  private val spawnInstancesMap = mutableMapOf<UUID, Instance>()
  private val spawnedPlayers = mutableSetOf<UUID>()

  fun loadOnStartup() {
    val eventNode = EventNode.type("spawn-listener", EventFilter.PLAYER)
    eventNode.setPriority(Int.MAX_VALUE)
    eventNode.addListener { event: PlayerSpawnEvent ->
      spawnedPlayers.add(event.player.uuid)
    }

    MinecraftServer.getGlobalEventHandler().addChild(eventNode)
  }

  fun initFakePlayer(
    playerName: String,
    uuid: UUID = getPlayerUuid(playerName),
    options: FakePlayerOption = DEFAULT_FAKE_PLAYER_OPTIONS,
    onSpawnCallback: ((FakePlayer) -> Unit)? = null,
  ): Player {
    FakePlayer.initPlayer(uuid, playerName, options, onSpawnCallback)
    return Entity.getEntity(uuid) as Player
  }

  fun initFakePlayer(
    playerName: String,
    spawnInstance: Instance,
    uuid: UUID = getPlayerUuid(playerName),
    options: FakePlayerOption = DEFAULT_FAKE_PLAYER_OPTIONS,
  ): Player {
    spawnInstancesMap[uuid] = spawnInstance
    return initFakePlayer(
      playerName,
      uuid,
      options,
    ) { fakePlayer: FakePlayer ->
      if (fakePlayer.instance !== spawnInstance) {
        fakePlayer.setInstance(spawnInstance)
      }
    }
  }

  fun waitUntilPlayerJoin(player: Player) {
    val joinInstance = spawnInstancesMap.getOrDefault(player.uuid, null)
    if (joinInstance == null) {
      while (player.instance == null) {
        Thread.sleep(DEFAULT_WAITING_TIMEOUT_MS)
      }
    } else {
      while (player.instance !== joinInstance) {
        Thread.sleep(DEFAULT_WAITING_TIMEOUT_MS)
      }
    }

    while (!isPlayerSpawned(player)) {
      Thread.sleep(DEFAULT_WAITING_TIMEOUT_MS)
    }

    spawnInstancesMap.remove(player.uuid)
    spawnedPlayers.remove(player.uuid)
  }

  fun waitUntilPlayerJoin(vararg players: Player) {
    for (player in players) {
      waitUntilPlayerJoin(player)
    }
  }

  private fun isPlayerSpawned(player: Player): Boolean =
    spawnedPlayers.contains(player.uuid)
}
