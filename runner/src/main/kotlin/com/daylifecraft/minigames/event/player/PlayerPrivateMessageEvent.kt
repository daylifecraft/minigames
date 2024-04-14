package com.daylifecraft.minigames.event.player

import net.minestom.server.entity.Player
import net.minestom.server.event.trait.PlayerEvent

/** That event called when somebody send private message another player  */
class PlayerPrivateMessageEvent(
  private val sender: Player,
  @JvmField val messageText: String,
  val recipient: Player,
) : PlayerEvent {
  override fun getPlayer(): Player = sender
}
