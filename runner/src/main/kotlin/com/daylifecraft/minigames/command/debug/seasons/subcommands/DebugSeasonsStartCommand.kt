package com.daylifecraft.minigames.command.debug.seasons.subcommands

import com.daylifecraft.common.seasons.Season
import com.daylifecraft.common.util.extensions.minestom.currentArgumentValue
import com.daylifecraft.minigames.seasons.SeasonsManager
import net.minestom.server.command.builder.suggestion.SuggestionEntry

class DebugSeasonsStartCommand :
  AbstractSeasonActivenessCommand(
    command = "start",
    nameSuggestionCallback = { _, _, suggestion ->
      SeasonsManager.configSeasonsList.inactiveSeasons
        .filter { season: Season -> season.name.startsWith(suggestion.currentArgumentValue) }
        .forEach { season: Season ->
          suggestion.addEntry(
            SuggestionEntry(
              season.name,
            ),
          )
        }
    },
    activeness = Season.Activeness.FORCE_ACTIVE,
    successKey = "debug.seasons.start.success",
  )
