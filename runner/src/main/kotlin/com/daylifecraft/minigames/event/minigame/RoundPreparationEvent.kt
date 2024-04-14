package com.daylifecraft.minigames.event.minigame

import com.daylifecraft.minigames.minigames.instances.AbstractMiniGameInstance
import net.minestom.server.event.Event

/**
 * This event called when round start initializing: world instance creating, round status set to
 * PREPARING Event needs to be listened by round system
 */
data class RoundPreparationEvent(val miniGameInstance: AbstractMiniGameInstance) : Event
