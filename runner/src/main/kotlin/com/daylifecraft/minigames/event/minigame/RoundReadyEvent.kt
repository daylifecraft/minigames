package com.daylifecraft.minigames.event.minigame

import com.daylifecraft.minigames.minigames.instances.AbstractMiniGameInstance
import net.minestom.server.event.Event

/**
 * This event called when all players ready (all players moved to MiniGame world instance and
 * teleported to their locations)
 */
data class RoundReadyEvent(val miniGameInstance: AbstractMiniGameInstance) : Event
