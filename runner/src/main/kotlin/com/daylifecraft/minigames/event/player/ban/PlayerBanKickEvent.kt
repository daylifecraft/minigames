package com.daylifecraft.minigames.event.player.ban

import net.minestom.server.entity.Player
import net.minestom.server.event.trait.PlayerEvent
import java.sql.Timestamp

// TODO watch at this unused hierarchy of classes
open class PlayerBanKickEvent(
  private val player: Player,
  var message: String,
  val reasons: List<String>,
  val banGiveDate: Timestamp,
) : PlayerEvent {
  override fun getPlayer(): Player = player
}
