package com.daylifecraft.minigames.gui.listeners

import com.daylifecraft.minigames.gui.GuiManager
import com.daylifecraft.minigames.listener.Listener
import net.minestom.server.event.player.PlayerTickEvent

class TickListener : Listener<PlayerTickEvent>(PlayerTickEvent::class.java) {
  override fun onCalled(event: PlayerTickEvent) {
    GuiManager.get().tick(event.player)
  }
}
