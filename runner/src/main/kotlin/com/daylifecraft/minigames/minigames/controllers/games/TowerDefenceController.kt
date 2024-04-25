package com.daylifecraft.minigames.minigames.controllers.games

import com.daylifecraft.common.logging.building.createLogger
import com.daylifecraft.common.util.safeCastToList
import com.daylifecraft.minigames.event.minigame.RoundPreparationEvent
import com.daylifecraft.minigames.event.player.minigame.PlayerPreparationEndEvent
import com.daylifecraft.minigames.event.player.minigame.PlayerPreparationEvent
import com.daylifecraft.minigames.instance.CraftInstancesManager
import com.daylifecraft.minigames.instance.instances.games.MiniGameWorldInstance
import com.daylifecraft.minigames.minigames.controllers.AbstractMiniGameController
import com.daylifecraft.minigames.minigames.instances.games.towerdefence.TowerDefenceInstance
import com.daylifecraft.minigames.minigames.instances.games.towerdefence.TowerDefenceTeamInfo
import com.daylifecraft.minigames.minigames.profile.RoundProfile
import com.daylifecraft.minigames.minigames.profile.RoundStatus
import com.daylifecraft.minigames.minigames.settings.MiniGamesSettingManager
import com.google.gson.JsonObject
import net.minestom.server.entity.Player
import net.minestom.server.event.EventDispatcher
import java.util.UUID

class TowerDefenceController(miniGamesSettingManager: MiniGamesSettingManager) : AbstractMiniGameController("towerdefence", miniGamesSettingManager) {

  private val teamInfoByName: MutableMap<String, List<TowerDefenceTeamInfo>> = mutableMapOf()

  init {
    // Load map information
    for (map in generalGameSettings.gameConfig.getValueList("map_config")) {
      val teamsData: MutableList<TowerDefenceTeamInfo> = mutableListOf()
      for (teamConfig in map["team_config"]!!.safeCastToList<Map<String, Any?>>()) {
        teamsData.add(TowerDefenceTeamInfo.getFromConfigurationSection(teamConfig))
      }

      teamInfoByName[map["name"] as String] = teamsData
    }
  }

  private val logger = createLogger()

  override fun onPlayerPreparationEvent(playerPreparationEvent: PlayerPreparationEvent) {
    val event =
      PlayerPreparationEndEvent.createFromPreparationEvent(
        playerPreparationEvent,
        PlayerPreparationEndEvent.PreparationResult.ACTIVE_SEARCH,
        playerPreparationEvent.defaultSettings,
        playerPreparationEvent.defaultFilters,
      )
    EventDispatcher.call(event)
  }

  override fun onRoundStartEvent(roundProfile: RoundProfile?, roundPlayerSettings: Map<Player, JsonObject>, playersSpreadByTeams: Set<Set<UUID>>, finalGameFilters: JsonObject) {
//    val (worldName, newPlayersDistribution) =
//      getRandomWorldName(roundPlayerSettings.map { it.key.uuid }, playersSpreadByTeams)
//
//    if (worldName.isEmpty()) {
//      logger.debug(
//        "Cannot find the map for TowerDefence: " +
//          "All players: $roundPlayerSettings Size: ${roundPlayerSettings.size}\n" +
//          "Spread by teams: $playersSpreadByTeams Size: ${playersSpreadByTeams.size}",
//      )
//      return
//    }

    val worldName = "1v1"
    val newPlayersDistribution = roundPlayerSettings.keys.map { listOf(it.uuid) }

    val teamsInfo = teamInfoByName[worldName]

    val (finalDistribution, playersSpawnPositions) = getPlayerByTeams(teamsInfo!!, newPlayersDistribution)

    val worldInstance = MiniGameWorldInstance(
      generalGameSettings.name,
      worldName,
      playersSpawnPositions.mapValues { it.value.position },

    )

    CraftInstancesManager.get().addInstance(worldInstance)
    roundProfile!!.setRoundStatusAndUpdate(RoundStatus.PREPARING)

    val miniGameInstance =
      TowerDefenceInstance.createInstance(
        worldInstance,
        roundProfile,
        roundPlayerSettings,
        finalGameFilters,
        finalDistribution,
        playersSpawnPositions,
      )

    // Set instance methods
    worldInstance.setInstanceConsumers(miniGameInstance)

    val event = RoundPreparationEvent(miniGameInstance)
    EventDispatcher.call(event)
  }

  private fun getRandomWorldName(allPlayers: List<UUID>, playersSpreadByTeams: Set<Set<UUID>>): Pair<String, Set<Set<UUID>>> {
    val totalPlayersCount = allPlayers.size
    try {
      for (map in generalGameSettings.gameConfig.getValueList("map_config")) {
        val mapName: String = map["name"].toString()

        val maxPlayersOnMap: Int = map["players"] as Int
        val maxPlayersInTeam: Int = map["players_per_team"] as Int

        // Skip map if we have players > max or at least one team, which size > max
        if (totalPlayersCount > maxPlayersOnMap || playersSpreadByTeams.maxOf { it.size } > maxPlayersInTeam) {
          continue
        }

        val playerDistribution = getNewPlayersDistribution(playersSpreadByTeams, maxPlayersInTeam)
        val minTeamsOnMap: Int = map["teams"] as Int

        if (playerDistribution.size >= minTeamsOnMap) {
          // We found the map!
          return Pair(mapName, playerDistribution)
        }
      }
    } catch (e: Exception) {
      e.printStackTrace()
    }

    return Pair("", emptySet())
  }

  private fun getNewPlayersDistribution(playersSpreadByTeam: Set<Set<UUID>>, maxPlayersPerTeam: Int): Set<Set<UUID>> {
    val result: MutableSet<Set<UUID>> = playersSpreadByTeam.filter { it.size == maxPlayersPerTeam }.toMutableSet()

    val unsorted = playersSpreadByTeam.filter { it.size < maxPlayersPerTeam }

    var current: MutableSet<UUID> = mutableSetOf()
    for (team in unsorted) {
      if (current.size + team.size <= maxPlayersPerTeam) {
        current.addAll(team)
      }

      if (current.size == maxPlayersPerTeam) {
        result.add(current)
        current = mutableSetOf()
      }
    }

    if (current.isNotEmpty()) {
      result.add(current)
    }

    return result
  }

  private fun getPlayerByTeams(teamsInformation: List<TowerDefenceTeamInfo>, distribution: List<List<UUID>>): Pair<Map<UUID, TowerDefenceTeamInfo>, Map<UUID, TowerDefenceTeamInfo.PlayerPosition>> {
    val resultPlayerDistribution: MutableMap<UUID, TowerDefenceTeamInfo> = mutableMapOf()
    val playersSpawnPositions: MutableMap<UUID, TowerDefenceTeamInfo.PlayerPosition> = mutableMapOf()

    for (index in distribution.indices) {
      val currentDistribution = distribution[index]
      val currentTeamInfo = teamsInformation[index]

      for (positionIndex in currentDistribution.indices) {
        val currentPlayer = currentDistribution.elementAt(positionIndex)
        resultPlayerDistribution[currentPlayer] = currentTeamInfo
        playersSpawnPositions[currentPlayer] =
          currentTeamInfo.playersPositions[positionIndex]
      }
    }

    return Pair(resultPlayerDistribution, playersSpawnPositions)
  }
}
