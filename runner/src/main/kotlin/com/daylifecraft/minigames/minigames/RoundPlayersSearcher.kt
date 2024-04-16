package com.daylifecraft.minigames.minigames

import com.daylifecraft.common.util.FilterUtils.getResultFilters
import com.daylifecraft.common.util.Range
import com.daylifecraft.common.util.extensions.minestom.scheduleTask
import com.daylifecraft.minigames.config.ConfigManager.mainConfig
import com.daylifecraft.minigames.minigames.queue.MiniGameQueueElement
import com.daylifecraft.minigames.minigames.settings.GeneralGameSettings
import com.daylifecraft.minigames.minigames.settings.MiniGamesSettingManager
import com.google.gson.JsonObject
import net.minestom.server.MinecraftServer
import net.minestom.server.timer.ExecutionType
import net.minestom.server.timer.TaskSchedule
import java.util.UUID
import java.util.stream.Stream

/**
 * The class is responsible for the logic of the usual game search. Selection of players, selection
 * of filters and transfer of players further The most important logic is the search for players
 */
class RoundPlayersSearcher {
  /** Called on server startup to start search cycle  */
  fun setup() {
    MinecraftServer.getSchedulerManager()
      .scheduleTask(
        delay = TaskSchedule.nextTick(),
        repeat = TaskSchedule.millis(MIN_SEARCH_TIME_MILLIS),
        executionType = ExecutionType.TICK_START,
        task = ::searchCycle,
      )
  }

  private fun searchCycle() {
    val playersSearchQueue =
      PlayerMiniGameManager.playersSearchQueue
        .filter {
          it.isNotStartingGame && it.timeElapsedInSearch > MIN_SEARCH_TIME_MILLIS
        }

    for (miniGameSetting in MiniGamesSettingManager.get()!!.allLoadedMiniGamesSettings) {
      val playersPool =
        playersSearchQueue.filter { queueElement: MiniGameQueueElement ->
          queueElement.miniGameId == miniGameSetting.name
        }

      if (getTotalPlayersCount(playersPool) < miniGameSetting.playersCount.minValue) {
        continue
      }

      val foundedElements =
        findMaxPlayersCombination(playersPool, miniGameSetting.playersCount)

      if (miniGameSetting.playersCount.isInBorder(getTotalPlayersCount(foundedElements))) {
        startRound(foundedElements, miniGameSetting)
      }
    }
  }

  companion object {
    private val EMPTY_JSON_OBJECT = JsonObject()

    private val MIN_SEARCH_TIME_MILLIS = mainConfig.getInt("roundSearchTime")!!.toLong()

    fun findMaxPlayersCombination(
      miniGameQueueElements: List<MiniGameQueueElement>,
      playersBorder: Range<Int>,
    ): List<MiniGameQueueElement> {
      val emptyFilters =
        miniGameQueueElements.filter { miniGameQueueElement ->
          EMPTY_JSON_OBJECT == miniGameQueueElement.filters
        }

      val emptyFiltersPlayersCount = getTotalPlayersCount(emptyFilters)
      if (emptyFiltersPlayersCount >= playersBorder.maxValue) {
        return clampElementsListToBorder(emptyFilters, playersBorder)
      }

      val required = playersBorder.maxValue - emptyFiltersPlayersCount

      val nonEmptyFilters: MutableList<MiniGameQueueElement> = ArrayList(miniGameQueueElements)
      nonEmptyFilters.removeAll(emptyFilters)

      var maxPlayersCombinationCount = -1
      var maxPlayersCombination: List<MiniGameQueueElement> = ArrayList()

      for (i in nonEmptyFilters.indices) {
        val currentMiniGameElement = nonEmptyFilters[i]

        val currentElements: MutableList<MiniGameQueueElement> = mutableListOf(currentMiniGameElement)
        var playersCount = currentMiniGameElement.totalPlayersCount
        var currentFilters = currentMiniGameElement.filters

        for (j in nonEmptyFilters.indices) {
          if (i == j) {
            continue
          }

          val tempElement = nonEmptyFilters[j]
          val tempFilters =
            getResultFilters(currentFilters, tempElement.filters)
          val tempPlayersCount = tempElement.totalPlayersCount

          if (EMPTY_JSON_OBJECT == tempFilters) {
            continue
          }

          currentFilters = tempFilters
          playersCount += tempPlayersCount
          currentElements.add(tempElement)

          if (playersCount >= required) {
            return clampElementsListToBorder(
              Stream.concat(currentElements.stream(), emptyFilters.stream()).toList(),
              playersBorder,
            )
          }
        }

        if (playersCount > maxPlayersCombinationCount) {
          maxPlayersCombination = currentElements
          maxPlayersCombinationCount = playersCount
        }
      }

      return clampElementsListToBorder(
        emptyFilters + maxPlayersCombination,
        playersBorder,
      )
    }

    private fun getTotalPlayersCount(miniGameQueueElements: List<MiniGameQueueElement>): Int = miniGameQueueElements.stream()
      .map { obj: MiniGameQueueElement -> obj.totalPlayersCount }
      .reduce { a: Int, b: Int -> Integer.sum(a, b) }
      .orElse(0)

    fun clampElementsListToBorder(
      queueElements: List<MiniGameQueueElement>,
      playersBorder: Range<Int>,
    ): List<MiniGameQueueElement> {
      // Sort to start from the biggest groups of players
      val miniGameQueueElements: MutableList<MiniGameQueueElement> = ArrayList(queueElements)
      miniGameQueueElements.sortWith(
        Comparator.comparingInt { obj: MiniGameQueueElement -> obj.totalPlayersCount }
          .reversed(),
      )

      val resultList: MutableList<MiniGameQueueElement> = ArrayList()
      var count = 0
      for (miniGameQueueElement in miniGameQueueElements) {
        if (count + miniGameQueueElement.totalPlayersCount > playersBorder.maxValue) {
          continue
        }

        count += miniGameQueueElement.totalPlayersCount
        resultList.add(miniGameQueueElement)
      }

      return resultList
    }

    private fun startRound(foundedElements: List<MiniGameQueueElement>, generalGameSettings: GeneralGameSettings) {
      val playersWithSettings: MutableMap<UUID, JsonObject> = HashMap()
      val playersSpreadByTeams: MutableSet<Set<UUID>> = HashSet()
      val finalFilters =
        getResultFilters(foundedElements.map { it.filters })

      for (foundedElement in foundedElements) {
        playersWithSettings.putAll(foundedElement.playersWithSettings)

        playersSpreadByTeams.add(HashSet(foundedElement.playersWithSettings.keys))
      }

      MiniGameStartController.startNewRound(
        generalGameSettings,
        playersWithSettings,
        playersSpreadByTeams,
        finalFilters,
      )

      foundedElements.forEach { element: MiniGameQueueElement -> element.setStartingGame(true) }
    }
  }
}
