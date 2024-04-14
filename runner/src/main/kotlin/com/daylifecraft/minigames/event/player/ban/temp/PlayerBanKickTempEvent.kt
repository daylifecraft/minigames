package com.daylifecraft.minigames.event.player.ban.temp

import com.daylifecraft.minigames.event.player.ban.PlayerBanKickEvent
import net.minestom.server.entity.Player
import java.sql.Timestamp

internal class PlayerBanKickTempEvent(
  player: Player,
  message: String,
  reasons: List<String>,
  banGiveDate: Timestamp,
  val banEndDate: Timestamp,
) : PlayerBanKickEvent(player, message, reasons, banGiveDate)
