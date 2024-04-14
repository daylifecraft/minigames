package com.daylifecraft.minigames.event.player.minigame

import com.daylifecraft.minigames.minigames.instances.AbstractMiniGameInstance
import net.minestom.server.entity.Player
import net.minestom.server.event.trait.PlayerEvent

/**
 * This event called when player leave from MiniGame It needs to be listened by Round system to
 * remove lock from player
 */
class PlayerRoundLeaveEvent(
  val miniGameInstance: AbstractMiniGameInstance,
  private val player: Player,
) : PlayerEvent {
  override fun getPlayer(): Player = player
}
