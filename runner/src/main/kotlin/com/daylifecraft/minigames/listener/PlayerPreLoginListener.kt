package com.daylifecraft.minigames.listener

import com.daylifecraft.minigames.PlayerManager
import com.daylifecraft.minigames.profile.punishment.PunishmentType
import com.daylifecraft.minigames.text.i18n.Lang
import net.minestom.server.event.player.AsyncPlayerPreLoginEvent

class PlayerPreLoginListener :
  Listener<AsyncPlayerPreLoginEvent>(
    AsyncPlayerPreLoginEvent::class.java,
  ) {
  override fun onCalled(event: AsyncPlayerPreLoginEvent) {
    val player = event.player

    try {
      PlayerManager.loadPlayer(player)
      PlayerManager.checkPunishments(player, PunishmentType.BAN, true)
    } catch (e: Exception) {
      PlayerManager.logPlayerConnectError(player, "profileBroken", e)
      PlayerManager.kickPlayerError(
        player,
        Lang.string(Lang.DEFAULT_LANGUAGE, "profile.load-failed-kick"),
      )
    }
  }
}
