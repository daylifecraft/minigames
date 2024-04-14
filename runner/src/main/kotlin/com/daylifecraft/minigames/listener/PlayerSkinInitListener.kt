package com.daylifecraft.minigames.listener

import net.minestom.server.entity.PlayerSkin
import net.minestom.server.event.player.PlayerSkinInitEvent

class PlayerSkinInitListener : Listener<PlayerSkinInitEvent>(PlayerSkinInitEvent::class.java) {
  override fun onCalled(event: PlayerSkinInitEvent) {
    event.skin = PlayerSkin.fromUsername(event.player.username)
  }
}
