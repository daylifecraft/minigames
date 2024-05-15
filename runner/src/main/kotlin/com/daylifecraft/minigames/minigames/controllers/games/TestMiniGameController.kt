package com.daylifecraft.minigames.minigames.controllers.games

import com.daylifecraft.common.logging.building.createLogger
import com.daylifecraft.common.util.RandomUtil
import com.daylifecraft.minigames.event.minigame.RoundPreparationEvent
import com.daylifecraft.minigames.event.player.minigame.PlayerPreparationEndEvent
import com.daylifecraft.minigames.event.player.minigame.PlayerPreparationEndEvent.Companion.createFromPreparationEvent
import com.daylifecraft.minigames.event.player.minigame.PlayerPreparationEvent
import com.daylifecraft.minigames.instance.CraftInstancesManager
import com.daylifecraft.minigames.instance.instances.games.MiniGameWorldInstance
import com.daylifecraft.minigames.minigames.controllers.AbstractMiniGameController
import com.daylifecraft.minigames.minigames.instances.games.TestMiniGameInstance
import com.daylifecraft.minigames.minigames.profile.RoundProfile
import com.daylifecraft.minigames.minigames.profile.RoundStatus
import com.daylifecraft.minigames.minigames.settings.MiniGamesSettingManager
import com.google.gson.JsonObject
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.Player
import net.minestom.server.event.EventDispatcher
import java.util.UUID

class TestMiniGameController(miniGamesSettingManager: MiniGamesSettingManager) : AbstractMiniGameController("testMiniGame", miniGamesSettingManager) {
  override fun onPlayerPreparationEvent(playerPreparationEvent: PlayerPreparationEvent) {
    val event =
      createFromPreparationEvent(
        playerPreparationEvent,
        PlayerPreparationEndEvent.PreparationResult.ACTIVE_SEARCH,
        playerPreparationEvent.defaultSettings,
        playerPreparationEvent.defaultSettings,
      )
    EventDispatcher.call(event)
  }

  override fun onRoundStartEvent(
    roundProfile: RoundProfile?,
    roundPlayerSettings: Map<Player, JsonObject>,
    playersSpreadByTeams: Set<Set<UUID>>,
    finalGameFilters: JsonObject,
  ) {
    val worldName = getRandomWorldName(roundPlayerSettings.size)
    val worldInstance =
      MiniGameWorldInstance(
        generalGameSettings.name,
        worldName,
        getPlayersPositionsOnMap(
          roundPlayerSettings.keys,
          getWorldSpawnPositions(worldName),
        ),
      )
    CraftInstancesManager.get().addInstance(worldInstance)

    roundProfile!!.setRoundStatusAndUpdate(RoundStatus.PREPARING)

    val miniGameInstance =
      TestMiniGameInstance.createInstance(
        worldInstance,
        roundProfile,
        roundPlayerSettings,
        finalGameFilters,
      )

    // Set instance methods
    worldInstance.setInstanceConsumers(miniGameInstance)

    val event = RoundPreparationEvent(miniGameInstance)
    EventDispatcher.call(event)
  }

  private fun getRandomWorldName(roundPlayerCount: Int): String {
    val worlds: MutableList<String> = ArrayList()
    for (worldConfig in generalGameSettings.gameConfig.worlds) {
      if (!worldConfig.enabled) continue

      if (roundPlayerCount !in worldConfig.minPlayers..worldConfig.maxPlayers) continue

      worlds.add(worldConfig.name)
    }

    return worlds.random(RandomUtil.SHARED_SECURE_RANDOM)
  }

  private fun getWorldSpawnPositions(worldName: String): List<Pos> {
    val worldMap = generalGameSettings.gameConfig.worlds.first { it.name == worldName }

    val spawnPositions: MutableList<Pos> = ArrayList()
    for (spawnPointConfig in worldMap.spawnPoints) {
      spawnPositions.add(
        Pos(
          spawnPointConfig.x,
          spawnPointConfig.y,
          spawnPointConfig.z,
          spawnPointConfig.yaw,
          spawnPointConfig.pitch,
        ),
      )
    }

    return spawnPositions
  }

  companion object {

    private val LOGGER = createLogger<TestMiniGameController>()

    private fun getPlayersPositionsOnMap(players: Set<Player>, positionsOnMap: List<Pos>): Map<UUID, Pos> {
      if (players.size > positionsOnMap.size) {
        LOGGER.debug("There are more players in the MiniGame than the specified positions on the map")
      }

      val resultPositions: MutableMap<UUID, Pos> = HashMap()
      for (index in positionsOnMap.indices) {
        if (players.size < index + 1) {
          break
        }

        resultPositions[players.toList()[index].uuid] = positionsOnMap[index]
      }

      return resultPositions
    }
  }
}
