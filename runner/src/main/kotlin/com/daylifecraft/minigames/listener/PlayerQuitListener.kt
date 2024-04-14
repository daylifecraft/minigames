package com.daylifecraft.minigames.listener

import com.daylifecraft.minigames.command.confirm.ConfirmCommand
import com.daylifecraft.minigames.gui.GuiManager
import com.daylifecraft.minigames.minigames.PlayerMiniGameManager
import net.minestom.server.event.player.PlayerDisconnectEvent

class PlayerQuitListener :
  Listener<PlayerDisconnectEvent>(
    PlayerDisconnectEvent::class.java,
  ) {
  override fun onCalled(event: PlayerDisconnectEvent) {
    val player = event.player

    ConfirmCommand.removeSender(player)

    // gui system

    // noCloseCurrently is true because player is offline...
    GuiManager.get().close(player, true)

    PlayerMiniGameManager.onPlayerQuit(player)
  }
}
