package com.daylifecraft.minigames.event.player

import com.daylifecraft.minigames.profile.player.PlayerProfile
import net.minestom.server.entity.Player
import net.minestom.server.event.trait.PlayerEvent

class LoadPlayerEvent(
  private val player: Player,
  val playerProfile: PlayerProfile,
) : PlayerEvent {
  override fun getPlayer(): Player = player
}
