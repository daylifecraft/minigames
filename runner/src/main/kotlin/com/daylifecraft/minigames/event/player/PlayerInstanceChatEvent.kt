package com.daylifecraft.minigames.event.player

import com.daylifecraft.minigames.instance.AbstractCraftInstance
import net.minestom.server.entity.Player
import net.minestom.server.event.trait.PlayerEvent

/**
 * Because we use our own chat and cancel the usual PlayerChatEvent -> it replaces default chat
 * event Also, chat was split by instances
 */
class PlayerInstanceChatEvent(
  private val sender: Player,
  val craftInstance: AbstractCraftInstance,
  val messageText: String,
  val recipients: List<Player>,
) : PlayerEvent {
  override fun getPlayer(): Player = sender
}
