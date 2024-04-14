package com.daylifecraft.minigames.listener

import com.daylifecraft.minigames.text.i18n.PlayerLanguage
import com.daylifecraft.minigames.util.sendStackTrace
import net.minestom.server.event.player.PlayerSettingsChangeEvent

class PlayerSettingsChangedListener :
  Listener<PlayerSettingsChangeEvent>(
    PlayerSettingsChangeEvent::class.java,
  ) {
  override fun onCalled(event: PlayerSettingsChangeEvent) {
    val player = event.player
    val lang = PlayerLanguage.get(player)

    sendStackTrace(player, "lang:" + lang.lang)
  }
}
