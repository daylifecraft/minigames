package com.daylifecraft.minigames.config

data class MiniGameSettingsConfig(
  val name: String,
  val displayNameKey: String,
  val descriptionKey: String,
  val guiBlock: String,
  val public: Boolean,
  val permission: String?,
  val minPlayers: Int,
  val maxPlayers: Int,
  val minGroupSize: Int,
  val maxGroupSize: Int,
  val gameConfig: GameConfig,
)

data class GameConfig(
  val worlds: List<GameWorldConfig>,
)

data class GameWorldConfig(
  val name: String,
  val enabled: Boolean,
  val displayNameKey: String,
  val maxPlayers: Int,
  val minPlayers: Int,
  val spawnPoints: List<SpawnPointConfig>,
)

data class SpawnPointConfig(
  val x: Double,
  val y: Double,
  val z: Double,
  val yaw: Float,
  val pitch: Float,
)
