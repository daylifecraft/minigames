package com.daylifecraft.common.seasons

/** Represents system of seasons with ability to get active seasons ordered by their priorities  */
class SeasonsList(val allSeasons: List<Season>) {
  private val seasonsByNames: MutableMap<String, Season> = HashMap()
  private var activeSeasonsCache: List<Season>? = null
  private val cacheLock = Any()

  init {
    for (season in allSeasons) {
      require(!seasonsByNames.containsKey(season.name)) { "Names of seasons in list should be unique" }
      seasonsByNames[season.name] = season

      season.attachedList = this
    }
  }

  fun getSeasonByName(name: String): Season? = seasonsByNames[name]

  /**
   * Returns list of active seasons sorted by their priority. Note that this method returns cache if
   * possible.
   */
  val activeSeasonsPrioritized: List<Season>
    get() {
      synchronized(cacheLock) {
        return activeSeasonsCache ?: allSeasons
          .filter(Season::isActive)
          .sortedByDescending { it.priority }
          .also { activeSeasonsCache = it }
      }
    }

  /** Returns list of inactive seasons. Note that this method generates new list every call.  */
  val inactiveSeasons: List<Season>
    get() = allSeasons.filter { !it.isActive }

  /** Drops active seasons cache. This used when some season change its priority or activeness.  */
  fun dropCaches() {
    synchronized(cacheLock) {
      activeSeasonsCache = null
    }
  }
}
