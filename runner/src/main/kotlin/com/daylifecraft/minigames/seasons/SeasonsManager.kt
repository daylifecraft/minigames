package com.daylifecraft.minigames.seasons

import com.daylifecraft.common.seasons.Season
import com.daylifecraft.common.seasons.SeasonDate
import com.daylifecraft.common.seasons.SeasonsList
import com.daylifecraft.minigames.config.ConfigManager
import com.daylifecraft.minigames.config.SeasonConfig

/** Singleton which can load and hold seasons list loaded from default config  */
object SeasonsManager {

  lateinit var configSeasonsList: SeasonsList
    private set

  fun load() {
    configSeasonsList = fromConfig(ConfigManager.mainConfig.seasons)
  }

  private fun fromConfig(seasonConfigs: List<SeasonConfig>): SeasonsList {
    val seasons: MutableList<Season> = ArrayList()
    var priority = seasons.size
    for (seasonValues in seasonConfigs) {
      seasons.add(
        Season(
          seasonValues.name,
          seasonValues.displayName,
          SeasonDate(seasonValues.startDate),
          SeasonDate(seasonValues.endDate),
          priority,
        )
      )
      priority--
    }

    return SeasonsList(seasons)
  }
}
