package com.daylifecraft.minigames.gui.listeners

import com.daylifecraft.minigames.gui.GuiManager
import com.daylifecraft.minigames.listener.Listener
import net.minestom.server.event.inventory.InventoryCloseEvent

class InventoryCloseListener :
  Listener<InventoryCloseEvent>(
    InventoryCloseEvent::class.java,
  ) {
  override fun onCalled(event: InventoryCloseEvent) {
    // noCloseCurrently is true because this event calls before Minestom closing
    GuiManager.get().close(event.player, true)
  }
}
