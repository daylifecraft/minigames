package com.daylifecraft.minigames.minigames.instances.games.towerdefence

import com.daylifecraft.common.config.ConfigFile
import com.daylifecraft.minigames.Init
import com.daylifecraft.minigames.minigames.instances.games.towerdefence.monsters.MonsterData
import com.daylifecraft.minigames.minigames.settings.MiniGamesSettingManager
import com.daylifecraft.minigames.util.FilesUtil
import net.minestom.server.instance.AnvilLoader
import net.minestom.server.instance.Chunk
import net.minestom.server.instance.Instance
import net.minestom.server.instance.InstanceManager
import net.minestom.server.instance.LightingChunk
import java.util.concurrent.CompletableFuture

class TowerDefenseManager(
  private val gamesSettingManager: MiniGamesSettingManager,
  private val instanceManager: InstanceManager,
) {

  private val towersConfigSection: ConfigFile = gamesSettingManager
    .getGeneralGameSettings(TOWER_DEFENSE_MINI_GAME_ID)!!.gameConfig.getConfigSection("tower_config")

  private val monstersConfigSection: List<Map<String, Any?>> = gamesSettingManager
    .getGeneralGameSettings(TOWER_DEFENSE_MINI_GAME_ID)!!.gameConfig.getValueList("monster_config")

  private var towersData: List<TowerData> = TowerData.parseTowersList(towersConfigSection)

  private val monstersData: List<MonsterData> = MonsterData.parseMonstersList(monstersConfigSection)

  private val globalTowersWorldPath: String = towersConfigSection.getString("world")!!

  val defaultTowerDefenseInstance: Instance = instanceManager.createInstanceContainer().apply {
    chunkLoader =
      AnvilLoader(
        FilesUtil.getResourcesPath(
          "games/%s/worlds/%s".format(TOWER_DEFENSE_MINI_GAME_ID, globalTowersWorldPath),
        ),
      )
    setChunkSupplier(::LightingChunk)
  }

  val towerCostBuybackPercent: Int = towersConfigSection.getInt("buyback")!!

  fun load() {
    for (towerData in towersData) {
      val futures: MutableList<CompletableFuture<Chunk>> = mutableListOf()
      towerData.iterateThrowTowerLayers().forEach {
        futures.add(defaultTowerDefenseInstance.loadChunk(it.chunkX(), it.chunkZ()))
      }

      CompletableFuture.allOf(*futures.toTypedArray()).thenRun {
        towerData.calculateTowerHeight(defaultTowerDefenseInstance)
      }
    }
  }

  /**
   * Find tower with specific ID and level
   * @return founded tower or null
   */
  fun findTowerData(towerId: String, towerLevel: Int): TowerData? {
    for (towerData in getLoadedTowersData()) {
      if (towerData.isEqual(towerId, towerLevel)) return towerData
    }

    return null
  }

  fun getLoadedTowersData(): List<TowerData> = towersData

  fun getLoadedTowersIds(): List<String> = getLoadedTowersData().map(TowerData::towerId).toList()

  fun getLoadedMonsters(): List<MonsterData> = monstersData

  fun getLoadedMonstersIds(): List<String> = getLoadedMonsters().map(MonsterData::monsterId).toList()

  fun findMonsterData(monsterId: String): MonsterData? {
    for (monsterData in getLoadedMonsters()) {
      if (monsterData.monsterId == monsterId) return monsterData
    }

    return null
  }

  fun getMinimalTowerLevel(towerId: String): Int = towersData.filter { it.towerId == towerId }.minOfOrNull { it.level } ?: -1

  companion object {
    private const val TOWER_DEFENSE_MINI_GAME_ID = "towerdefence"

    fun get(): TowerDefenseManager = Init.towerDefenseManager!!
  }
}
