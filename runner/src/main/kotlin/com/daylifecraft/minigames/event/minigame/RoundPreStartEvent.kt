package com.daylifecraft.minigames.event.minigame

import com.daylifecraft.minigames.minigames.profile.RoundProfile
import com.google.gson.JsonObject
import net.minestom.server.entity.Player
import net.minestom.server.event.Event
import java.util.UUID

/**
 * Event provides information for MiniGame Handler to start the round It storages MiniGameID, List
 * of round players with their settings
 */
data class RoundPreStartEvent(
  val roundPlayerSettings: Map<Player, JsonObject>,
  val playersSpreadByTeams: Set<Set<UUID>>,
  val roundProfile: RoundProfile,
  val miniGameId: String,
  val finalGameFilters: JsonObject,
) : Event
