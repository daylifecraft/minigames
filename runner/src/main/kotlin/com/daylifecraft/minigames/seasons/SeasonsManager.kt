package com.daylifecraft.minigames.seasons

import com.daylifecraft.common.config.ConfigFile
import com.daylifecraft.common.seasons.Season
import com.daylifecraft.common.seasons.SeasonsList
import com.daylifecraft.minigames.config.ConfigManager

/** Singleton which can load and hold seasons list loaded from default config  */
object SeasonsManager {
  @JvmStatic
  var configSeasonsList: SeasonsList? = null
    private set

  fun load() {
    configSeasonsList = fromConfig(ConfigManager.mainConfig!!, "seasons")
  }

  /**
   * Creates seasons list from yaml config file.
   *
   * @param file config file where list is stored.
   * @param listPath path to list node in config.
   * @return created seasons list.
   */
  private fun fromConfig(file: ConfigFile, listPath: String): SeasonsList {
    val valueList = file.getValueList(listPath)

    val seasons: MutableList<Season> = ArrayList()
    var priority = valueList.size
    for (seasonValues in valueList) {
      seasons.add(Season(seasonValues, priority))
      priority--
    }

    return SeasonsList(seasons)
  }
}
