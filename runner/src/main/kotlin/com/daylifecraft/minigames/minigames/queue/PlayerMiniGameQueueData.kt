package com.daylifecraft.minigames.minigames.queue

import com.daylifecraft.minigames.minigames.search.IRoundSearchProvider
import com.google.gson.JsonElement
import com.google.gson.JsonObject

/**
 * Class that provides information about player, who search the game. It contains MiniGameID and his
 * settings & filters
 */
class PlayerMiniGameQueueData
@JvmOverloads
constructor(
  val miniGameId: String,
  val roundSearchProvider: IRoundSearchProvider,
  var settings: JsonObject = JsonObject(),
  var filters: JsonObject = JsonObject(),
) {
  /**
   * Add setting
   *
   * @param settingName setting name
   * @param element setting value
   */
  fun addSetting(settingName: String, element: JsonElement) {
    settings.add(settingName, element)
  }

  /**
   * Add filter
   *
   * @param filterName filter name
   * @param element filter value
   */
  fun addFilter(filterName: String, element: JsonElement) {
    filters.add(filterName, element)
  }
}
