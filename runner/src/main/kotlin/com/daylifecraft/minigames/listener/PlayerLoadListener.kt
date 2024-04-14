package com.daylifecraft.minigames.listener

import com.daylifecraft.minigames.PlayerManager
import com.daylifecraft.minigames.event.player.LoadPlayerEvent
import com.daylifecraft.minigames.text.i18n.Lang

class PlayerLoadListener : Listener<LoadPlayerEvent>(LoadPlayerEvent::class.java) {
  override fun onCalled(event: LoadPlayerEvent) {
    val profile = event.playerProfile
    val player = event.player
    val permissions = profile.permissions

    try {
      // Apply permissions to player
      PlayerManager.applyPermissions(player, permissions)
    } catch (e: Exception) {
      PlayerManager.logPlayerConnectError(player, "profileBroken", e)
      PlayerManager.kickPlayerError(
        player,
        Lang.string(Lang.DEFAULT_LANGUAGE, "profile.load-failed-kick"),
      )
    }
  }
}
