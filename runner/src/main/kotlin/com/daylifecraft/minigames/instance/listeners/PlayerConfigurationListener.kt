package com.daylifecraft.minigames.instance.listeners

import com.daylifecraft.minigames.instance.CraftInstancesManager
import com.daylifecraft.minigames.listener.Listener
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent

class PlayerConfigurationListener :
  Listener<AsyncPlayerConfigurationEvent>(
    AsyncPlayerConfigurationEvent::class.java,
  ) {
  override fun onCalled(event: AsyncPlayerConfigurationEvent) {
    val craftInstancesManager = CraftInstancesManager.get()
    val player = event.player

    event.spawningInstance = craftInstancesManager.getLoginInstanceAndPrepare(player)
  }
}
