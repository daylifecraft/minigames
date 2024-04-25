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
        playerPreparationEvent.defaultFilters,
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
    for (worldMap in generalGameSettings.gameConfig.getValueList("worlds")) {
      val mapEnabled = worldMap["enabled"] as Boolean

      if (!mapEnabled) continue

      val minPlayers = worldMap["minPlayers"] as Int
      val maxPlayers = worldMap["maxPlayers"] as Int

      if (roundPlayerCount !in minPlayers..maxPlayers) continue

      worlds.add((worldMap["name"] as String))
    }

    return worlds.random(RandomUtil.SHARED_SECURE_RANDOM)
  }

  private fun getWorldSpawnPositions(worldName: String?): List<Pos> {
    val worldMap =
      generalGameSettings.gameConfig.getValueList("worlds")
        .first { objectMap -> objectMap["name"] == worldName }

    if (worldMap.isEmpty()) {
      return emptyList()
    }

    val spawnPositions: MutableList<Pos> = ArrayList()
    for (pointObject in worldMap["spawnPoints"] as List<*>) {
      val pointMap = (pointObject as Map<*, *>)

      spawnPositions.add(
        Pos(
          (pointMap["x"] as Number).toDouble(),
          (pointMap["y"] as Number).toDouble(),
          (pointMap["z"] as Number).toDouble(),
          (pointMap["facingX"] as Number).toFloat(),
          (pointMap["facingZ"] as Number).toFloat(),
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
