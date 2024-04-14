package com.daylifecraft.minigames.instance.listeners

import com.daylifecraft.minigames.instance.CraftInstancesManager
import com.daylifecraft.minigames.listener.Listener
import net.minestom.server.event.player.PlayerDisconnectEvent

class PlayerDisconnectListener :
  Listener<PlayerDisconnectEvent>(
    PlayerDisconnectEvent::class.java,
  ) {
  override fun onCalled(event: PlayerDisconnectEvent) {
    val craftInstancesManager = CraftInstancesManager.get()
    val player = event.player
    val instance = player.instance

    craftInstancesManager.notifyPlayerDisconnect(instance, player)
  }
}
