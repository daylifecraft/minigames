package com.daylifecraft.minigames.event.player

import net.minestom.server.entity.Player
import net.minestom.server.event.trait.PlayerEvent

/** That event called when somebody send message in group  */
class PlayerGroupMessageEvent(
  val messageText: String,
  private val sender: Player,
) : PlayerEvent {
  override fun getPlayer(): Player = sender
}
