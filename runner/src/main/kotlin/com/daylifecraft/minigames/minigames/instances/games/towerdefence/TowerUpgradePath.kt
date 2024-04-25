package com.daylifecraft.minigames.minigames.instances.games.towerdefence

import com.daylifecraft.common.logging.building.createLogger

class TowerUpgradePath(
  val upgradeItems: List<UpgradeItem>,
) {

  class UpgradeItem(
    val upgradeStrategy: UpgradeStrategy,
    private val towerName: String,
    private val towerLevel: Int = 0,
  ) {
    private var towerData: TowerData? = null

    fun getTowerData(): TowerData? {
      if (towerData != null) {
        return towerData
      }

      val founded = if (upgradeStrategy == UpgradeStrategy.NEXT_LEVEL) {
        TowerDefenseManager.get().findTowerData(
          towerName,
          towerLevel,
        )
      } else {
        TowerDefenseManager.get().findTowerData(
          towerName,
          TowerDefenseManager.get().getMinimalTowerLevel(towerName),
        )
      }
      towerData = founded

      return towerData
    }
  }

  enum class UpgradeStrategy {
    NEXT_LEVEL,
    REPLACE,
    ;

    companion object {
      fun getByStringRepresentation(string: String): UpgradeStrategy? {
        for (strategy in entries) {
          if (strategy.toString().equals(string, ignoreCase = true)) return strategy
        }

        return null
      }
    }
  }

  companion object {
    fun getByConfigSection(dataList: List<Map<String, Any?>>, towerId: String, towerLevel: Int): TowerUpgradePath? {
      val result: MutableList<UpgradeItem> = mutableListOf()

      for (map in dataList) {
        val upgradeStrategy = UpgradeStrategy.getByStringRepresentation(map["type"].toString())

        if (upgradeStrategy == null) {
          createLogger().debug("Unable to load UpgradeStrategy from string " + map["type"])
          continue
        }

        if (upgradeStrategy == UpgradeStrategy.NEXT_LEVEL) {
          result.add(UpgradeItem(upgradeStrategy, towerId, towerLevel + 1))
        } else {
          result.add(UpgradeItem(upgradeStrategy, (map["tower"] as Map<*, *>)["name"].toString()))
        }
      }

      return TowerUpgradePath(result)
    }
  }
}
