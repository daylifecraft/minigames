package com.daylifecraft.minigames.listener

import com.daylifecraft.minigames.PlayerManager
import com.daylifecraft.minigames.instance.CraftInstancesManager
import com.daylifecraft.minigames.profile.punishment.PunishmentType
import net.minestom.server.event.player.PlayerChatEvent

class PlayerChatListener :
  Listener<PlayerChatEvent>(
    PlayerChatEvent::class.java,
  ) {
  override fun onCalled(event: PlayerChatEvent) {
    event.isCancelled = true
    val sender = event.player

    if (PlayerManager.checkPunishments(sender, PunishmentType.MUTE, true)) {
      return
    }

    val playerInstance =
      CraftInstancesManager.get().getPlayerInstance(sender)
        ?: // TODO 17.10.2023 skip?
        return

    playerInstance.sendInstanceChatMessage(sender, event.message)
  }
}
