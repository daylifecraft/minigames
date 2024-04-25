package com.daylifecraft.minigames.minigames.instances.games.towerdefence

import com.daylifecraft.common.util.safeCastToList
import net.minestom.server.coordinate.Pos
import net.minestom.server.coordinate.Vec
import net.minestom.server.item.Material

class TowerDefenceTeamInfo(
  val playersPositions: List<PlayerPosition>,
  val blockType: Material?,
  val mobSpawnZone: CuboidRegion,
) {

  class PlayerPosition(
    val position: Pos,
    val towerZone: CuboidRegion,
  ) {

    companion object {
      fun getFromConfigurationSection(configData: Map<String, Any?>): PlayerPosition {
        // Load towers positions (border of 2 points)
        val towerZones = configData["towerzone"]!!.safeCastToList<Map<String, Any?>>()

        return PlayerPosition(
          Pos(
            (configData["x"] as Int).toDouble(),
            (configData["y"] as Int).toDouble(),
            (configData["z"] as Int).toDouble(),
            (configData["yaw"] as Int).toFloat(),
            (configData["pitch"] as Int).toFloat(),
          ),
          CuboidRegion(
            Vec((towerZones[0]["x"] as Int).toDouble(), 1.0, (towerZones[0]["z"] as Int).toDouble()),
            Vec((towerZones[1]["x"] as Int).toDouble(), 1.0, (towerZones[1]["z"] as Int).toDouble()),
          ),
        )
      }
    }
  }

  companion object {
    fun getFromConfigurationSection(configData: Map<String, Any?>): TowerDefenceTeamInfo {
      val playerPositions: MutableList<PlayerPosition> = mutableListOf()

      for (config in configData["players"]!!.safeCastToList<Map<String, Any?>>()) {
        playerPositions.add(PlayerPosition.getFromConfigurationSection(config))
      }

      val mobSpawnZone = configData["mobspawnzone"]!!.safeCastToList<Map<String, Any?>>()

      return TowerDefenceTeamInfo(
        playerPositions,
        Material.fromNamespaceId(configData["block"].toString().lowercase()),
        CuboidRegion(
          Vec(
            (mobSpawnZone[0]["x"] as Int).toDouble(),
            (mobSpawnZone[0].getOrDefault("y", 255) as Int).toDouble(),
            (mobSpawnZone[0]["z"] as Int).toDouble(),
          ),
          Vec(
            (mobSpawnZone[1]["x"] as Int).toDouble(),
            (mobSpawnZone[1].getOrDefault("y", 255) as Int).toDouble(),
            (mobSpawnZone[1]["z"] as Int).toDouble(),
          ),
        ),
      )
    }
  }
}
