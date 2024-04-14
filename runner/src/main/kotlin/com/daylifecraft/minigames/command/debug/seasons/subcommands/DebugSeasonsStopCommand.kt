package com.daylifecraft.minigames.command.debug.seasons.subcommands

import com.daylifecraft.common.seasons.Season
import com.daylifecraft.minigames.command.debug.seasons.sugestions.ActiveSeasonSuggestion

/** '/~ seasons stop &lt;name&gt;' command executor  */
class DebugSeasonsStopCommand :
  AbstractSeasonActivenessCommand(
    "stop",
    ActiveSeasonSuggestion(),
    Season.Activeness.FORCE_STOPPED,
    "debug.seasons.stop.success",
  )
