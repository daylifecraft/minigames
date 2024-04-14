package com.daylifecraft.common.text.i18n

import com.daylifecraft.common.text.PlayerText
import net.minestom.server.entity.Player

fun interface DynamicPlayerText : PlayerText {
  fun get(player: Player): PlayerText

  override fun string(player: Player): String {
    val playerText = get(player)
    return playerText.string(player)
  }
}
