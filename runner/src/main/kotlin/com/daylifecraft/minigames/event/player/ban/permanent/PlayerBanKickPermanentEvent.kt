package com.daylifecraft.minigames.event.player.ban.permanent

import com.daylifecraft.minigames.event.player.ban.PlayerBanKickEvent
import net.minestom.server.entity.Player
import java.sql.Timestamp

internal class PlayerBanKickPermanentEvent(
  player: Player,
  message: String,
  reasons: List<String>,
  banGiveDate: Timestamp,
) : PlayerBanKickEvent(player, message, reasons, banGiveDate)
